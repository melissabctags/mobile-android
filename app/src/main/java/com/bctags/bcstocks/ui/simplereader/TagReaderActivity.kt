package com.bctags.bcstocks.ui.simplereader

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.simplereader.adapter.TagReaderAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bumptech.glide.Glide
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
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
    private var triggerEnabled = true
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private val epcsList: MutableList<String> = mutableListOf()
    private val upcsList: MutableList<String> = mutableListOf()
    val SERVER_ERROR = "Server error, try later"
    private var itemList: MutableList<ItemData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294 && triggerEnabled) {
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

    private fun initRead() {
        lifecycleScope.launch() {
            try {
                if (isScanning) {
                    stopInventory()
                } else {
                    itemList.clear()
                    initRecyclerView()
                    val btnText = "Stop"
                    binding.tvScan.text = btnText
                    lockScanButton()
                    isScanning = true
                    upcsList.clear()
                    readTag()
                }
            } catch (e: Exception) {
                Log.e("initRead ", "Error initRead: ${e.message}")
            } finally {

            }
        }
    }
    private fun lockScanButton(){
        binding.tvScan.isEnabled = false
        triggerEnabled=false
        Handler(Looper.getMainLooper()).postDelayed({
            binding.tvScan.isEnabled = true
            triggerEnabled=true
        }, 1000)
    }

    private fun readTag() {
        lifecycleScope.launch(newSingleThreadContext("readTagTagsReader")) {
            try {
                withContext(Dispatchers.IO) {
                    val result: Boolean = rfid.init()
                    if (!result) {
                        Log.i("DIDN'T WORK", "DIDN'T WORK")
                        rfid.stopInventory()
                        rfid.free()
                    }else{
                        if (rfid.startInventoryTag()) {
                            Log.i("WORKS", "WORKS")
                            isScanning = true
                            tagsReader()
                        } else {
                            rfid.stopInventory()
                            rfid.free()
                        }
                    }

                }
            } catch (e: Exception) {
                Log.e("readTag", "Error en la lectura de etiquetas: ${e.message}")
            } finally {

            }
        }
    }

    private fun tagsReader() {
        lifecycleScope.launch(newSingleThreadContext("tagsReaderTagsReader")) {
            try {
                while (isScanning) {
                    val uhfTagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
                    if (uhfTagInfo != null) {
                        epcsList.add(uhfTagInfo.epc.toString())
                        Log.i("EPC", uhfTagInfo.epc.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("tagsReader", "Error en la lectura de etiquetas: ${e.message}")
            } finally {

            }
        }
    }

    private fun stopInventory() {
        lifecycleScope.launch {
            try {
                isScanning = false
                rfid.stopInventory()
                rfid.free()
                val btnText = "Read tags"
                lockScanButton()
                binding.tvScan.text = btnText
                if (epcsList.isNotEmpty()) {
                    val list = epcsList.distinct() as MutableList<String>
                    Log.i("stopInventory", list.toString())
                    getUpcs(list)
                } else {
                    messageDialog.showDialog(
                        this@TagReaderActivity,
                        R.layout.dialog_error,
                        "An error occurred.\nTry again."
                    ) { }
                }
            } catch (e: Exception) {
                Log.e("stopInventory", "Error: ${e.message}")
            } finally {

            }
        }
    }


    private fun getUpcs(epcsList: MutableList<String>) {
        lifecycleScope.launch(newSingleThreadContext("getUpcsTagReader")) {
            epcsList.forEach { i ->
                try {
                    val upc = tools.getGTIN(i).toString()
                    upcsList.add(upc)
                } catch (e: Exception) {
                    Log.e("Error", "Error Scanner: ${e.message}")
                }
            }
            if (upcsList.isNotEmpty()) {
                val list = upcsList.distinct() as MutableList<String>
                Log.i("UPC", list.toString())
                getItemsRead(list)
            }
        }
    }

    private fun getItemsRead(list: MutableList<String>) {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("upc", "or", list))
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
        if (itemList.isEmpty()) {
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