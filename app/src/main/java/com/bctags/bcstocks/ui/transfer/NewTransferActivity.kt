package com.bctags.bcstocks.ui.transfer

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityNewTransferBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.NewTransfer
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempInventoryData
import com.bctags.bcstocks.model.TransferItemRequest
import com.bctags.bcstocks.ui.transfer.adapter.NewTransferAdapter
import com.bctags.bcstocks.ui.transfer.adapter.TransferItemAdapter
import com.bctags.bcstocks.ui.transfer.adapter.TransferLocationAdapter
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewTransferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewTransferBinding
    private lateinit var adapter: NewTransferAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson = Gson()
    private val dropDown = DropDown()
    val tools = EPCTools()

    private var branchId = 0
    private val mapBranches: HashMap<String, String> = HashMap()
    private var selectedBranch = 0
    private var mapLocations: HashMap<String, String> = HashMap()
    private var mapItems: HashMap<String, String> = HashMap()
    private var itemsList: MutableList<TempInventoryData> = mutableListOf()
    private val SERVER_ERROR = "Server error, try later"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        getBranches()
        getLocations()
        getItems()
        initRecyclerView()
        initListeners()
    }

    private fun initRecyclerView() {
        adapter = NewTransferAdapter(
            list = mutableListOf(),
            onClickListener = { TempInventoryData, Int -> onDeleteItem(TempInventoryData, Int) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun onDeleteItem(item: TempInventoryData, position: Int) {
        itemsList.removeIf { it.inventory.id == item.inventory.id }
        adapter.notifyDataSetChanged()
    }

    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnLocation.setOnClickListener {
            openSelect(binding.llLocation, binding.llItem)
        }
        binding.btnItem.setOnClickListener {
            openSelect(binding.llItem, binding.llLocation)
        }
        binding.btnSave.setOnClickListener {
            checkNewTransfer()
        }
    }

    private fun checkNewTransfer() {
        if(selectedBranch!=0){
            getNewTransfer()
        }else{
            messageDialog.showDialog(
                this@NewTransferActivity,
                R.layout.dialog_error,
                "Select a branch."
            ) { }
        }
    }

    private fun getNewTransfer() {
        val list: MutableList<TransferItemRequest> = mutableListOf()
        itemsList.forEach { i ->
            list.add(TransferItemRequest(i.inventory.id,i.quantity))
        }
        saveNewTransfer(NewTransfer(selectedBranch,list))
    }

    private fun saveNewTransfer(transfer: NewTransfer) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.createTransfer(transfer),
                onSuccess = { response ->
                    Toast.makeText(applicationContext, "Saved", Toast.LENGTH_LONG).show()
                    mainTransfer()
                },
                onError = { error ->
                    Log.i("error", gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
    private fun mainTransfer() {
        val intent = Intent(this, TransferActivity::class.java)
        startActivity(intent)
    }

    private fun openSelect(open: LinearLayout, close: LinearLayout) {
        if (open.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            open.visibility = View.GONE
        } else {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            open.visibility = View.VISIBLE
            close.visibility = View.GONE
        }
    }

    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters, pag)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocationsList(requestBody),
                onSuccess = { response ->
                    useLocations(response.list)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useLocations(locations: MutableList<LocationData>) {
        val list: MutableList<String> = mutableListOf()
        locations.forEach { item ->
            list.add(item.name)
            mapLocations[item.name] = item.id.toString();
        }
        dropDown.listArrange(
            list,
            binding.locationList,
            mapLocations,
            this,
            ::updateLocations
        )
    }

    private fun updateLocations(id: String, text: String) {
        inventoryByLocation(id,text)
    }

    private fun inventoryByLocation(id:String, text: String) {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("locationId", "eq", mutableListOf(id)))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    openDialog(
                        text,
                        "Items in ",
                        TransferLocationAdapter(
                            list = response.data,
                            onClickListener = { TempInventoryData ->
                                changeSelectedTotal(
                                    TempInventoryData
                                )
                            })
                    )
                },
                onError = { error ->
                    Log.i("error", gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun openDialog(
        text: String,
        sign: String,
        adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>
    ) {
        val dialogLocation = Dialog(this)
        dialogLocation.setContentView(R.layout.dialog_transfer_select_location)
        dialogLocation.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val recycler: RecyclerView = dialogLocation.findViewById(R.id.recyclerLocationList)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val title: TextView = dialogLocation.findViewById(R.id.tvMainTitle)
        title.text = buildString {
            append(sign)
            append(text)
        }
        val btnSave: MaterialButton = dialogLocation.findViewById(R.id.btnSaveLocation)
        btnSave.setOnClickListener {
            updateMainRecyclerView()
            dialogLocation.hide()
        }
        dialogLocation.show()
    }

    private fun changeSelectedTotal(item: TempInventoryData) {
        Log.i("ADD_ELEMENT", item.toString())
        val existingItem = itemsList.find { it.inventory.id == item.inventory.id }
        if (existingItem != null) {
            existingItem.quantity = item.quantity
        } else {
            itemsList.add(item)
        }
        itemsList.removeIf { it.quantity == 0 }
    }

    private fun updateMainRecyclerView() {
        adapter = NewTransferAdapter(
            list = itemsList,
            onClickListener = { TempInventoryData, Int -> onDeleteItem(TempInventoryData, Int) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun getBranches() {
        val pag = Pagination(1, 1000)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getBranches(FilterRequestPagination(pag)),
                onSuccess = { response ->
                    useBranches(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useBranches(response: List<Branch>) {
        val list: MutableList<String> = mutableListOf()
        response.forEach { item ->
            list.add(item.name)
            mapBranches[item.name] = item.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.branchesList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapBranches,
            this@NewTransferActivity,
            ::updateBranch
        )
    }

    private fun updateBranch(id: String, text: String) {
        selectedBranch = id.toInt()
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

    private fun initItems(dataResponse: MutableList<ItemData>) {
        val list: MutableList<String> = mutableListOf()
        dataResponse.forEach { i ->
            list.add(i.item + " " + i.description)
            mapItems[i.item + " " + i.description] = i.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.itemList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapItems,
            this@NewTransferActivity,
            ::updateItem
        )
    }

    private fun updateItem(id: String, text: String) {
        if (id.toInt() != 0) {
            getItemInventory(id, text)
        }
    }

    private fun getItemInventory(id: String, text: String) {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("itemId", "eq", mutableListOf(id)))
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    openDialog(
                        text,
                        "Select ",
                        TransferItemAdapter(
                            list = response.data,
                            onClickListener = { TempInventoryData ->
                                changeSelectedTotal(
                                    TempInventoryData
                                )
                            })
                    )
                },
                onError = { error ->
                    Log.i("error", gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


}