package com.bctags.bcstocks.ui.inventory

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
import com.bctags.bcstocks.databinding.ActivityCountInventoryBinding
import com.bctags.bcstocks.databinding.ActivityInventoryCountBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryCount
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.CountLocation
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.ui.inventory.adapterInventoryCount.CounterAdapter
import com.bctags.bcstocks.ui.receives.adapter.ItemsReceiveAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CountInventoryActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityCountInventoryBinding
    private lateinit var adapter: CounterAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    val gson = Gson()
    val tools = EPCTools()
    var selectedLocations: CountLocation = CountLocation(0,"")

    val DURACION: Long = 2500;
    private var isScanning = true
    var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    val epcsList: MutableList<String> = mutableListOf()
    var isInventory: Boolean = false
    var hashUpcs: MutableMap<String, Int> = mutableMapOf()
    private var filters:MutableList<Filter> = mutableListOf()
    val SERVER_ERROR = "Server error, try later"

    private var listInventory:MutableList<InventoryCount> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCountInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
//            val selectedLocationsType = object : TypeToken<MutableList<CountLocation>>() {}.type
//            selectedLocations =  gson.fromJson(intent.getStringExtra("LOCATIONS_SELECTED"), selectedLocationsType)
            selectedLocations = gson.fromJson(intent.getStringExtra("LOCATIONS_SELECTED"), CountLocation::class.java)
            Log.i("location",selectedLocations.id.toString())
            getInventory()
        }
        scannerGif()
        initListeners()
        readTag()

    }
    private fun readTag() {
        val result: Boolean = rfid.init();
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
                val uhftagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
                if (uhftagInfo != null) {
                    epcsList.add(uhftagInfo.epc.toString())
                    Log.i("EPC", uhftagInfo.epc.toString())
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
        val btnText = "Scan"
        binding.tvScan.text = btnText
        val list = epcsList.distinct() as MutableList<String>
        Log.i("epcsList",list.toString())
        countUpcs(list)
    }
    private fun countUpcs(epcsList: MutableList<String>) {
        epcsList.forEach { i ->
            val upc = tools.getGTIN(i).toString()
            if (hashUpcs.isEmpty() || !hashUpcs.containsKey(upc)) {
                hashUpcs[upc] = 1
            } else {
                hashUpcs[upc] = hashUpcs[upc]!! + 1
            }
        }
        checkReceivesUpcs()
    }

    private fun checkReceivesUpcs() {
        Log.i("hashUpcs",hashUpcs.toString())
        listInventory.forEach {
            if (hashUpcs.containsKey(it.Item.upc)) {
                it.founded = hashUpcs[it.Item.upc]?.toInt() ?: 0
            }
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = CounterAdapter(
            list = listInventory,
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }


    private fun getInventory() {
        val pag = Pagination(1, 10000)
        filters.add(Filter("locationId", "eq", mutableListOf(selectedLocations.id.toString())))
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(FilterRequest(filters,pag)),
                onSuccess = { response ->
                    initInventory(response.data)
                },
                onError = { error ->
                    Log.i("error",gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initInventory(data: MutableList<InventoryData>) {
        data.forEach { i ->
            listInventory.add(InventoryCount(i.id,i.quantity,0,i.Item,i.Location))
        }
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

    private fun initListeners() {
        binding.tvScan.setOnClickListener {
            if (isScanning) {
                stopInventory()
            } else {
                val btnText = "Stop"
                binding.tvScan.text = btnText
                isScanning = true
                listInventory.clear()
                hashUpcs.clear()
                getInventory()
                initRecyclerView()
                readTag()
            }
        }
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}