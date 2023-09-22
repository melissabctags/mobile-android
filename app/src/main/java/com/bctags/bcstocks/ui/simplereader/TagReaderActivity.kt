package com.bctags.bcstocks.ui.simplereader

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
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
import com.bctags.bcstocks.ui.receives.NewReceiveActivity
import com.bctags.bcstocks.ui.receives.adapter.ItemsReceiveAdapter
import com.bctags.bcstocks.ui.simplereader.adapter.TagReaderAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.EPCTools
import com.bumptech.glide.Glide
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TagReaderActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTagReaderBinding
    private lateinit var adapter: TagReaderAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    val tools = EPCTools()

    private val DURACION: Long = 2500;
    private var isScanning = false
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private val epcsList: MutableList<String> = mutableListOf()
    private val upcsList: MutableList<String> = mutableListOf()
    private var isInventory: Boolean = false
    val SERVER_ERROR = "Server error, try later"
    private var itemList:MutableList<ItemData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()

    }
    private fun initListeners() {
        binding.tvScan.setOnClickListener {
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

        binding.ivGoBack.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }
    private fun readTag() {
        var result: Boolean = rfid.init();
        if (!result) {
            Log.i("DIDN'T WORK", "DIDN'T WORK")
            rfid.stopInventory()
            rfid.free()
        }
        if (rfid.startInventoryTag()) {
            Log.i("WORKS", "WORKS")
            isInventory = true
            tagsReader()
        } else {
            stopInventory()
        }
    }
    private fun tagsReader() {
        CoroutineScope(Dispatchers.Default).launch {
            while (isInventory) {
                val uhftagInfo: UHFTAGInfo? = rfid.readTagFromBuffer();
                Log.i("EPC", uhftagInfo.toString())
                if (uhftagInfo != null) {
                    epcsList.add(uhftagInfo.epc.toString())
                } else {
                    delay(300)
                }
            }
        }
    }
    private fun stopInventory() {
        isScanning = false
        isInventory = false
        rfid.stopInventory()
        rfid.free()
        val btnText = "Read tags"
        binding.tvScan.text = btnText
        val list = epcsList.distinct() as MutableList<String>
        Log.i("EPC",epcsList.toString())
        getUpcs(list)
    }
    private fun getUpcs(epcsList: MutableList<String>) {
        epcsList.forEach { i ->
            val upc = tools.getGTIN(i).toString()
            upcsList.add(upc)
        }
        val list = upcsList.distinct() as MutableList<String>
        Log.i("UPC",upcsList.toString())
        getItemsRead(list)
    }

    private fun getItemsRead(list: MutableList<String>) {
        val pag = Pagination(1, 1000)
        var filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("upc", "or", upcsList))
        val requestBody = FilterRequest(filters,pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getItems(requestBody),
                onSuccess = { response ->
                    itemList = response.data
                    initRecyclerView()
                },
                onError = { error ->
                   // Log.i("error",gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
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