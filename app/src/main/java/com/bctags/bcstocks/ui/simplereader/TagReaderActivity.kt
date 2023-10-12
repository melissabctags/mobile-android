package com.bctags.bcstocks.ui.simplereader

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityTagReaderBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.WorkOrder
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.simplereader.adapter.TagReaderAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bumptech.glide.Glide
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class TagReaderActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTagReaderBinding
    private lateinit var adapter: TagReaderAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    val tools = EPCTools()

    private val messageDialog = MessageDialog()
    private val DURACION: Long = 2500;

    private var isScanning = false
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private val epcsList: MutableList<String> = mutableListOf()
    private val upcsList: MutableList<String> = mutableListOf()
    val SERVER_ERROR = "Server error, try later"
    private var itemList:MutableList<ItemData> = mutableListOf()
    private var stopThread = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()


    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294) {
            initRead()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    private fun initListeners() {
        binding.tvScan.setOnClickListener {
            initRead()
        }
        binding.llHeader.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initRead(){
        lifecycleScope.launch {
            if (isScanning) {
                stopInventory()
            } else {
                itemList.clear()
                initRecyclerView()
                val btnText = "Stop reading"
                binding.tvScan.text = btnText
                isScanning = true
                upcsList.clear()
                readTag()
            }
        }
    }

    private var rfidContext = newSingleThreadContext("RFIDThread")
    private fun readTag() {
        lifecycleScope.launch(rfidContext) {
            withContext(Dispatchers.IO) {
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DIDN'T WORK", "DIDN'T WORK")
                    rfid.stopInventory()
                    rfid.free()
                }
                if (rfid.startInventoryTag()) {
                    Log.i("WORKS", "WORKS")
                    isScanning = true
                    tagsReader()
                } else {
                    rfid.stopInventory()
                    rfid.free()
                   // stopInventory()
                }
            }
        }

    }

    private fun tagsReader() {
        lifecycleScope.launch(rfidContext) {
            while (isScanning && !stopThread) {
                val uhfTagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
                if (uhfTagInfo != null) {
                    epcsList.add(uhfTagInfo.epc.toString())
                    Log.i("EPC", uhfTagInfo.epc.toString())
                }
            }
        }
    }

    private suspend fun stopInventory() {
        isScanning = false
        stopThread = true
        rfid.stopInventory()
        rfid.free()
        val btnText = "Read tags"
        // Puedes actualizar la interfaz de usuario en el hilo principal
        withContext(Dispatchers.Main) {
            binding.tvScan.text = btnText
            val list = epcsList.distinct() as MutableList<String>
            getUpcs(list)
        }
    }


    private fun getUpcs(epcsList: MutableList<String>) {
        epcsList.forEach { i ->
            val upc = tools.getGTIN(i).toString()
            upcsList.add(upc)
        }
        val list = upcsList.distinct() as MutableList<String>
        Log.i("UPC",list.toString())
        getItemsRead(list)
    }

    private fun getItemsRead(list: MutableList<String>) {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("upc", "or", upcsList))
        val requestBody = FilterRequest(filters, pag)
        lifecycleScope.launch {
            apiCall.performApiCall(
                apiClient.getItems(requestBody),
                onSuccess = { response ->
                    useItems(response.data)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }

    }

    private fun useItems(data: MutableList<ItemData>) {
        itemList = data
        initRecyclerView()
        if(itemList.isEmpty()){
            messageDialog.showDialog(
                this@TagReaderActivity,
                R.layout.dialog_error,
                "UPC not found.\n ${upcsList.toString()} "
            ) { }
        }
    }

    private fun initRecyclerView() {
        adapter = TagReaderAdapter(itemList)
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun scannerGif() {
        val logo = findViewById<ImageView>(R.id.ivScanning)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Glide.with(this).asGif().load(R.drawable.scan_gif).into(logo)
        @Suppress("DEPRECATION")
        Handler().postDelayed(Runnable {
            binding.cvScanning.visibility = View.GONE;
        }, DURACION)
    }



}


// private fun updatePo(id: String, text: String) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val purchaseOrder = searchPoSupplier(id)
//        withContext(Dispatchers.Main) {
//            val supplierText = "Supplier: ${purchaseOrder?.Supplier?.name ?: "N/A"}"
//            binding.tvPoSupplier.text = supplierText
//        }
//    }
//}
//
//private suspend fun searchPoSupplier(id: String): PurchaseOrder? {
//    val pag = Pagination(1, 100)
//    val poFilter = mutableListOf(Filter("id", "eq", mutableListOf(id)))
//    val poRequestBody = FilterRequest(poFilter, pag)
//
//    return apiCall.performApiCall(apiClient.getPurchaseOrder(poRequestBody))
//        .onSuccess { response ->
//            return response.list.firstOrNull()
//        }
//        .onError { error ->
//            Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
//            return null
//        }
//}

//    private fun readTag() {
//        Thread {
//            val result: Boolean = rfid.init();
//            if (!result) {
//                Log.i("DIDN'T WORK", "DIDN'T WORK")
//                rfid.stopInventory()
//                rfid.free()
//            }
//            if (rfid.startInventoryTag()) {
//                Log.i("WORKS", "WORKS")
//                isInventory = true
//                tagsReader()
//            } else {
//                stopInventory()
//            }
//        }.start()
//    }
//
//
//    private fun tagsReader() {
//        while (isInventory && !stopThread) {
//            val uhfTagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
//            if (uhfTagInfo != null) {
//                epcsList.add(uhfTagInfo.epc.toString())
//                Log.i("EPC", uhfTagInfo.epc.toString())
//            } else {
//                try {
//                    Thread.sleep(300)
//                } catch (e: InterruptedException) {
//                    // Manejar la interrupción si es necesario
//                }
//            }
//        }
//    }
//
//    private fun stopInventory() {
//        isScanning = false
//        isInventory = false
//        stopThread = true  // Esto indica al hilo que debe finalizar
//        rfid.stopInventory()
//        rfid.free()
//        val btnText = "Read tags"
//        binding.tvScan.text = btnText
//        val list = epcsList.distinct() as MutableList<String>
//        getUpcs(list)
//    }