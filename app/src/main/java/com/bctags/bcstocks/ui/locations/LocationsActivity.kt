package com.bctags.bcstocks.ui.locations

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityInventoryBinding
import com.bctags.bcstocks.databinding.ActivityLocationsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.LocationBarcode
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.FiltersRequest
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.ItemLocationsRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.locationChanges
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.inventory.adapter.InventoryAdapter
import com.bctags.bcstocks.ui.locations.adapter.ItemLocationAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.bctags.bcstocks.util.Utils
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityLocationsBinding
    private lateinit var adapter: ItemLocationAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson= Gson()
    private val dropDown= DropDown()
    private var barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder

    var mapItem: HashMap<String, String> = HashMap()
    var mapLocations: HashMap<String, String> = HashMap()
    private var location: LocationBarcode = LocationBarcode(0,"","")
    private var itemId=0
    private var branchId=0
    val SERVER_ERROR = "Server error, try later"

    var itemsList: MutableList<ItemBox> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()
        getLocations()
        getItems()
    }

    fun close() {
        barcodeDecoder.close()
    }
    fun open() {
        barcodeDecoder.open(this)
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                //todo: ask barcode and locations relation
                // barcodeEntity.barcodeData==location.name
                if( barcodeEntity.barcodeData!=""){
                    binding.btnSaveChange.visibility = View.VISIBLE
                }else{
                    Toast.makeText(applicationContext, "Location doesn't match. Try again.", Toast.LENGTH_LONG).show()
                }
                stop()
            } else {
                messageDialog.showDialog(
                    this@LocationsActivity,
                    R.layout.dialog_error,
                    "Error reading barcode. Scan again "
                ) { }
                //Toast.makeText(applicationContext, "Scanning error", Toast.LENGTH_LONG).show()
                Log.i("BARCODE", "FAILED")
            }
        }
    }
    fun start() {
//        open()
    }
    fun stop() {
        close()
        barcodeDecoder.stopScan()
    }
    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnScanBarCode.setOnClickListener {
            open()
        }
        binding.btnSaveChange.setOnClickListener{
            changeItemsLocations()
        }
        binding.btnByLocations.setOnClickListener{
            val intent = Intent(this, LocationsItemsActivity::class.java)
            startActivity(intent)
        }

    }
    private fun changeItemsLocations(){
        itemsList.removeAll { it.quantity == 0 }
        if(location.id!=0){
            if (itemsList.isNotEmpty()) {
                val moveList: MutableList<ItemLocationsRequest> = mutableListOf()
                itemsList.forEach {
                    moveList.add(ItemLocationsRequest(it.quantity, it.itemId, location.id))
                }
                moveItemsLocation(locationChanges(moveList))
            } else {
                messageDialog.showDialog(
                    this@LocationsActivity,
                    R.layout.dialog_error,
                    "Check your quantities."
                ) { }
            }
        } else {
            messageDialog.showDialog(
                this@LocationsActivity,
                R.layout.dialog_error,
                "Must select a location."
            ) { }
        }
    }

    private fun moveItemsLocation(requestBody:locationChanges){
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.changeLocations(requestBody),
                onSuccess = { response ->
                    messageDialog.showDialog(
                        this@LocationsActivity,
                        R.layout.dialog_success,
                        "Saved",
                    ) { mainMenu() }
                },
                onError = { error ->
                    Log.i("error",error.toString())
                    messageDialog.showDialog(
                        this@LocationsActivity,
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

    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters,pag)

        val list : MutableList<String> = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocationsList(requestBody),
                onSuccess = { response ->
                    val locationResponse: LocationResponse? = response
                    locationResponse?.list?.forEach { i ->
                        list.add(i.name + " " + i.Branch.name)
                        mapLocations[i.name + " " + i.Branch.name] = i.id.toString();
                    }
                    val autoComplete: AutoCompleteTextView = findViewById(R.id.locationsList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapLocations,
                        this@LocationsActivity,
                        ::updateLocations
                    )
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun updateLocations(id: String, text: String) {
        location.id = id.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocation(GetOne(location.id)),
                onSuccess = { response ->
                    Log.i("responselocation",response.data.toString())
                    location.name = response.data.name
                    //location.barcode = response.list[0].barcode
                },
                onError = { error ->
                    Log.i("responselocation",error.toString())
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }

    }
    private fun getItems() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        val requestBody = FilterRequest(filters,pag)

        val list: MutableList<String> = mutableListOf()
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getItems(requestBody),
                onSuccess = { response ->
                    val dataResponse = response.data
                    dataResponse?.forEach { i ->
                        list.add(i.item + " " + i.description)
                        mapItem[i.item + " " + i.description] = i.id.toString();
                    }
                    val autoComplete: AutoCompleteTextView = findViewById(R.id.inventoryList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapItem,
                        this@LocationsActivity,
                        ::updateItem
                    )
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun updateItem(id: String, text: String) {
        itemId = id.toInt()
        if(itemId!=0){
            searchInventory()
        }
    }
    private fun searchInventory() {
        val pag = Pagination(1, 1000)
        var filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("itemId", "eq", mutableListOf(itemId.toString())))
        val requestBody = FilterRequest(filters,pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Log.i("error",gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun initRecyclerView(list: MutableList<InventoryData>) {
        adapter = ItemLocationAdapter(
            list = list,
            onClickListener = { ItemBox -> changeSelectedTotal(ItemBox) },
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }
    private fun changeSelectedTotal(itemBox: ItemBox){
        val existingItem = itemsList.find { it.itemId == itemBox.itemId }
        if (existingItem != null) {
            existingItem.quantity = itemBox.quantity
        } else {
            itemsList.add(itemBox)
        }
    }





}