package com.bctags.bcstocks.ui.transfer

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
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
import com.bctags.bcstocks.io.response.LocationBarcode
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempInventoryData
import com.bctags.bcstocks.ui.transfer.adapter.NewTransferAdapter
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
    val mapBranches: HashMap<String, String> = HashMap()
    var selectedBranch = 0
    var originId = 0
    var mapLocations: HashMap<String, String> = HashMap()
    private var location: LocationBarcode = LocationBarcode(0, "", "")

    var itemsList: MutableList<TempInventoryData> = mutableListOf()

    private val SERVER_ERROR = "Server error, try later"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)
        getBranches()
        getLocations()
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
            openLocation()
        }
        binding.btnItem.setOnClickListener {
            openItems()
        }
    }

    private fun openLocation() {
        if (binding.llLocation.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.llLocation.visibility = View.GONE
        } else {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.llLocation.visibility = View.VISIBLE
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
        originId = id.toInt()
        inventoryByLocation(text)
    }

    private fun inventoryByLocation(text: String) {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("locationId", "eq", mutableListOf(originId.toString())))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    openLocationDialog(response.data, text)
                },
                onError = { error ->
                    Log.i("error", gson.toJson(error))
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun openLocationDialog(inventory: MutableList<InventoryData>, text: String) {
        val dialogLocation = Dialog(this)
        dialogLocation.setContentView(R.layout.dialog_transfer_select_location)
        dialogLocation.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val adapter = TransferLocationAdapter(
            list = inventory,
            onClickListener = { TempInventoryData -> changeSelectedTotal(TempInventoryData) },
        )
        val recycler: RecyclerView = dialogLocation.findViewById(R.id.recyclerLocationList)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        val title: TextView = dialogLocation.findViewById(R.id.tvMainTitle)
        title.text = buildString {
            append("Select items in ")
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
        Log.i("ADD_ELEMENT",item.toString())
        val existingItem = itemsList.find { it.inventory.itemId == item.inventory.itemId }
        if (existingItem != null) {
            existingItem.quantity = item.quantity
        } else {
            itemsList.add(item)
        }
        itemsList.removeIf { it.quantity==0 }
    }

    //    BYLOCATION
    private fun updateMainRecyclerView() {
        adapter = NewTransferAdapter(
            list = itemsList,
            onClickListener = { TempInventoryData, Int -> onDeleteItem(TempInventoryData, Int) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun openItems() {
        //var dialog = Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_transfer_select_item)

//        val tfLocationList: AutoCompleteTextView = dialog.findViewById(R.id.tfLocationList)
//        dropDown.listArrangeWithId(
//            locationList,
//            tfLocationList,
//            mapLocation,
//            this,
//            item.itemId.toString(),
//            ::updateLocation
//        )
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        val tvItemDescription: TextView = dialog.findViewById(R.id.tvItemDescription)
//        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)
//        val btnStatus: MaterialButton = dialog.findViewById(R.id.btnStatus)
//        val llStatus: LinearLayout = dialog.findViewById(R.id.llStatus)
//
//        if (item.quantity == 0) {
//            llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffe600"))
//            checkButtonReceiveItemMaterialButton(
//                btnStatus,
//                "#ffe600",
//                R.color.black,
//                R.drawable.ic_exclamation
//            )
//        } else {
//            if ((item.receivedQuantity + item.quantity) >= item.orderQuantity) {
//                llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#20c95e"))
//                checkButtonReceiveItemMaterialButton(
//                    btnStatus,
//                    "#20c95e",
//                    R.color.white,
//                    R.drawable.ic_done
//                )
//            } else {
//                llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ff7700"))
//                checkButtonReceiveItemMaterialButton(
//                    btnStatus,
//                    "#ff7700",
//                    R.color.black,
//                    R.drawable.ic_exclamation
//                )
//            }
//        }
//        tvItemDescription.text = item.description
//        etQuantity.setText(item.quantity.toString())
//
//        if (item.locationId != 0) {
//            val key = mapLocation.entries.find { it.value == item.locationId.toString() }?.key
//            tfLocationList.setText(key)
//        }
//
//        val btnSaveChange: MaterialButton = dialog.findViewById(R.id.btnSaveChange)
//        btnSaveChange.setOnClickListener {
//            updateItemList(item, dialog)
//        }

        dialog.show()
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


}