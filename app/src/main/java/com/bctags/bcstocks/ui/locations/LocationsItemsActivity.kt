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
import com.bctags.bcstocks.databinding.ActivityLocationsItemsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.LocationBarcode
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.ItemLocationsRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.locationChanges
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.locations.adapter.ByLocationAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.google.gson.Gson
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationsItemsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityLocationsItemsBinding
    private lateinit var adapter: ByLocationAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson= Gson()
    private val dropDown= DropDown()
    private var barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder


    var mapLocations: HashMap<String, String> = HashMap()
    private var destinationLocation: LocationBarcode = LocationBarcode(0,"","")
    private var originId=0

    val SERVER_ERROR = "Server error, try later"

    var itemsList: MutableList<ItemBox> = mutableListOf()
    private var branchId=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityLocationsItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()
        getLocations()
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
                    messageDialog.showDialog(
                        this@LocationsItemsActivity,
                        R.layout.dialog_error,
                        "Location doesn't match. Try again."
                    ) {  stop() }
                }
            } else {
                messageDialog.showDialog(
                    this@LocationsItemsActivity,
                    R.layout.dialog_error,
                    "Error reading barcode. Scan again "
                ) { }
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
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnScanBarCode.setOnClickListener {
            open()
        }
        binding.btnSaveChange.setOnClickListener{
            changeItemsLocations()
        }
        binding.btnByItems.setOnClickListener{
            val intent = Intent(this, LocationsActivity::class.java)
            startActivity(intent)
        }
    }
    private fun changeItemsLocations(){
        itemsList.removeAll { it.quantity == 0 }
        if (itemsList.isNotEmpty()) {
            val moveList: MutableList<ItemLocationsRequest> = mutableListOf()
            itemsList.forEach {
                moveList.add(ItemLocationsRequest(it.quantity, it.itemId, destinationLocation.id))
            }
            moveItemsLocation(locationChanges(moveList))
        }else{
            messageDialog.showDialog(
                this@LocationsItemsActivity,
                R.layout.dialog_error,
                "Check your quantities"
            ) { }
        }
    }
    private fun moveItemsLocation(requestBody:locationChanges){
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.changeLocations(requestBody),
                onSuccess = { response ->
                    messageDialog.showDialog(
                        this@LocationsItemsActivity,
                        R.layout.dialog_success,
                        "Saved",
                    ) { mainMenu() }
                },
                onError = { error ->
                    Log.i("error",error.toString())
                    messageDialog.showDialog(
                        this@LocationsItemsActivity,
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
        val list : MutableList<String> = mutableListOf()
        locationResponse.forEach { i ->
            list.add(i.name + " " + i.Branch.name)
            mapLocations[i.name + " " + i.Branch.name] = i.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.originList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapLocations,
            this@LocationsItemsActivity,
            ::updateOrigin
        )
        val autoCompleteDestiny: AutoCompleteTextView = findViewById(R.id.destinationList)
        dropDown.listArrange(
            list,
            autoCompleteDestiny,
            mapLocations,
            this@LocationsItemsActivity,
            ::updateDestination
        )
    }

    private fun updateOrigin(id: String, text: String) {
        originId = id.toInt()
        searchInventory()
        binding.tvListTitle.text = buildString {
            append(binding.tvListTitle.text.toString())
            append(" ")
            append(text)
        }
    }
    private fun updateDestination(id: String, text: String) {
        destinationLocation.id = id.toInt()
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocation(GetOne(destinationLocation.id)),
                onSuccess = { response ->
                    destinationLocation.name = response.data.name
                    //location.barcode = response.list[0].barcode
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun searchInventory() {
        val pag = Pagination(1, 1000)
        var filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("locationId", "eq", mutableListOf(originId.toString())))
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
        adapter = ByLocationAdapter(
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