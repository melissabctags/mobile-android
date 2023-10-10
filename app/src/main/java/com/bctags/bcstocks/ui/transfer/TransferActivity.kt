package com.bctags.bcstocks.ui.transfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityTransferBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.LocationBarcode
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.ItemLocationsRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.locationChanges
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.transfer.adapter.ItemBranchAdapter
import com.bctags.bcstocks.ui.transfer.adapter.ItemBranchViewHolder
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class TransferActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTransferBinding
    private lateinit var adapter: ItemBranchAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson = Gson()
    private val dropDown = DropDown()
    val tools = EPCTools()

    var mapItem: HashMap<String, String> = HashMap()
    var mapLocations: HashMap<String, String> = HashMap()
    private var location: LocationBarcode = LocationBarcode(0, "", "")
    private var itemId = 0
    private var branchId = 0
    val SERVER_ERROR = "Server error, try later"

    var itemsList: MutableList<ItemBox> = mutableListOf()

    private var isScanning = false
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private val epcsList: MutableList<String> = mutableListOf()
    private val upcsList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()
        getLocations()
        getItems()
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294) {
            initRead()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveChange.setOnClickListener {
            changeItemsLocations()
            Log.i("itemsList", itemsList.toString())
        }
    }

    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "ne", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocationsList(requestBody),
                onSuccess = { response ->
                    initLocations(response.list)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initLocations(locationResponse: MutableList<LocationData>) {
        val list: MutableList<String> = mutableListOf()
        locationResponse.forEach { i ->
            list.add(i.name + " " + i.Branch.name)
            mapLocations[i.name + " " + i.Branch.name] = i.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.locationsList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapLocations,
            this@TransferActivity,
            ::updateLocations
        )
    }

    private fun updateLocations(id: String, text: String) {
        location.id = id.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocation(GetOne(location.id)),
                onSuccess = { response ->
                    Log.i("responselocation", response.data.toString())
                    location.name = response.data.name
                    //location.barcode = response.list[0].barcode
                },
                onError = { error ->
                    Log.i("responselocation", error.toString())
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }

    }

    private fun getItems() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getItems(requestBody),
                onSuccess = { response ->
                    initItems(response.data)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initItems(data: MutableList<ItemData>) {
        val list: MutableList<String> = mutableListOf()
        val dataResponse = data
        dataResponse.forEach { i ->
            list.add(i.item + " " + i.description)
            mapItem[i.item + " " + i.description] = i.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.inventoryList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapItem,
            this@TransferActivity,
            ::updateItem
        )
    }

    private fun updateItem(id: String, text: String) {
        itemId = id.toInt()
        if (itemId != 0) {
            searchInventory()
        }
    }

    private fun searchInventory() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("itemId", "eq", mutableListOf(itemId.toString())))
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Log.i("error", gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initRecyclerView(list: MutableList<InventoryData>) {
        adapter = ItemBranchAdapter(
            list = list,
            onClickListener = { ItemBox -> changeSelectedTotal(ItemBox) },
            onClickListenerScan = { InventoryData, position -> scanItems(InventoryData, position) },
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private var rfidContext = newSingleThreadContext("RFIDThread")
    private var currentUpc = ""
    private var currentId = 0
    private var currentPosition = 0
    private fun scanItems(item: InventoryData, position: Int) {
        currentUpc = item.Item.upc
        currentPosition = position
        currentId = item.id

    }
    private fun initRead(){
        lifecycleScope.launch {
            if (isScanning) {
                stopInventory()
            } else {
                binding.cvScanning.visibility = View.VISIBLE
                scannerGif()
                isScanning = true
                epcsList.clear()
                upcsList.clear()
                readTag()
            }
        }
    }

    private var stopThread = false
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
        withContext(Dispatchers.Main) {
            val list = epcsList.distinct() as MutableList<String>
            getUpcs(list)
        }
    }

    private fun getUpcs(epcsList: MutableList<String>) {
        runOnUiThread {
            epcsList.forEach { i ->
                val upc = tools.getGTIN(i).toString()
                upcsList.add(upc)
            }
            upcsList.removeAll { it != currentUpc }
            val totalElements: Int = upcsList.size
            val viewHolder =
                binding.recyclerList.findViewHolderForAdapterPosition(currentPosition) as ItemBranchViewHolder
            val totalString = totalElements.toString()
            viewHolder.binding.etSelectedQty.setText(totalString)
            changeSelectedTotal(ItemBox(currentId, totalString.toInt()))
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
        }, 1500)
    }

    private fun changeSelectedTotal(itemBox: ItemBox) {
        val existingItem = itemsList.find { it.itemId == itemBox.itemId }
        if (existingItem != null) {
            existingItem.quantity = itemBox.quantity
        } else {
            itemsList.add(itemBox)
        }
        Log.i("itemsList",itemsList.toString())
    }

    private fun changeItemsLocations() {
        itemsList.removeAll { it.quantity == 0 }
        if (location.id != 0) {
            if (itemsList.isNotEmpty()) {
                val moveList: MutableList<ItemLocationsRequest> = mutableListOf()
                itemsList.forEach {
                    moveList.add(ItemLocationsRequest(it.quantity, it.itemId, location.id))
                }
                moveItemsLocation(locationChanges(moveList))
            } else {
                messageDialog.showDialog(
                    this@TransferActivity,
                    R.layout.dialog_error,
                    "Check your quantities."
                ) { }
            }
        } else {
            messageDialog.showDialog(
                this@TransferActivity,
                R.layout.dialog_error,
                "Must select a location."
            ) { }
        }
    }

    private fun moveItemsLocation(requestBody: locationChanges) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.changeLocations(requestBody),
                onSuccess = { response ->
                    messageDialog.showDialog(
                        this@TransferActivity,
                        R.layout.dialog_success,
                        "Saved",
                    ) { mainMenu() }
                },
                onError = { error ->
                    Log.i("error", error.toString())
                    messageDialog.showDialog(
                        this@TransferActivity,
                        R.layout.dialog_error,
                        "An error occurred, try again later $error"
                    ) { }
                }
            )
        }
    }

    private fun mainMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
    }


}