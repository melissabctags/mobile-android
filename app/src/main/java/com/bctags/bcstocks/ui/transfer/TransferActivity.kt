package com.bctags.bcstocks.ui.transfer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityTransferBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.LocationBarcode
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.ItemLocationsRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.locationChanges
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.locations.LocationsItemsActivity
import com.bctags.bcstocks.ui.locations.adapter.ItemLocationAdapter
import com.bctags.bcstocks.ui.transfer.adapter.ItemBranchAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTransferBinding
    private lateinit var adapter: ItemBranchAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson= Gson()
    private val dropDown= DropDown()

    var mapItem: HashMap<String, String> = HashMap()
    var mapLocations: HashMap<String, String> = HashMap()
    private var location: LocationBarcode = LocationBarcode(0,"","")
    private var itemId=0
    private var branchId=0
    val SERVER_ERROR = "Server error, try later"

    var itemsList: MutableList<ItemBox> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()
        getLocations()
        getItems()
    }
    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveChange.setOnClickListener{
            //changeItemsLocations()
            Log.i("itemsList",itemsList.toString())
        }
    }
    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "ne", mutableListOf(branchId.toString())))
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
                        this@TransferActivity,
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
                        this@TransferActivity,
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
        adapter = ItemBranchAdapter(
            list = list,
            onClickListener = { ItemBox -> changeSelectedTotal(ItemBox) },
            onClickListenerScan = { InventoryData -> scanItems(InventoryData) },
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }
    private fun scanItems(item:InventoryData){

    }
    private fun changeSelectedTotal(itemBox: ItemBox){
        val existingItem = itemsList.find { it.itemId == itemBox.itemId }
        if (existingItem != null) {
            existingItem.quantity = itemBox.quantity
        } else {
            itemsList.add(itemBox)
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

    private fun moveItemsLocation(requestBody: locationChanges){
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
                    Log.i("error",error.toString())
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