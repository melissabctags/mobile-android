package com.bctags.bcstocks.ui.inventory

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityInventoryBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.ui.inventory.adapter.InventoryAdapter
import com.bctags.bcstocks.ui.receives.adapter.ReceivesAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class InventoryActivity : DrawerBaseActivity() {

    private lateinit var binding: ActivityInventoryBinding
    private lateinit var adapter: InventoryAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    private val gson= Gson()
    private val dropDown= DropDown()

    private var pagination = TempPagination(1, 0, 2, 0)
    private var firstRound = true
    private var filters:MutableList<Filter> = mutableListOf()
    private var branchId=0

    var mapItem: HashMap<String, String> = HashMap()
    var mapLocations: HashMap<String, String> = HashMap()
    private var locationId=0
    private var itemId=0
    val SERVER_ERROR = "Server error, try later"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()
        getLocations()
        getItems()
        getInventoryList()
    }
    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination = utils.prevPagination(pagination)
            binding.tvPage.text = "Page ${pagination.currentPage.toString()}"
            getInventoryList()
        }
    }
    private fun NextPagination() {
        if (pagination.nextPage != 0) {
            pagination = utils.NextPagination(pagination)
            binding.tvPage.text = "Page " + pagination.currentPage.toString()
            getInventoryList()
        }
    }

    private fun getInventoryList() {
        val pag = Pagination(pagination.currentPage, pagination.pageSize)
        val requestBody = FilterRequest(filters,pag)
        Log.i("REQUEST",gson.toJson(requestBody))
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventoryList(requestBody),
                onSuccess = { response ->
                    Log.i("RESPONSE",gson.toJson(response))
                    if (firstRound) {
                        pagination = utils.ínitPagination(response.pagination.totals, pagination)
                        firstRound = false
                    }
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
        adapter = InventoryAdapter(
            list = list,
            onClickListener = { InventoryData -> viewItem(InventoryData) },
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }
    private fun viewItem(item: InventoryData){

    }


    private fun expandCardView() {
        if (binding.cvFormSearch.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.cvFormSearch.visibility = View.GONE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_down_black)
        } else {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.cvFormSearch.visibility = View.VISIBLE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_up_black)
        }
    }

    private fun initListeners() {
        binding.btnPrev.setOnClickListener {
            prevPagination()
        }
        binding.btnNext.setOnClickListener {
            NextPagination()
        }
        binding.llSearch.setOnClickListener {
            expandCardView()
        }
        binding.btnSearch.setOnClickListener {
            searchItems()
        }
        binding.btnReset.setOnClickListener {
            resetForm()
        }
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun searchItems() {
        filters.clear()
        firstRound = true

        if (itemId != 0) {
            filters.add(Filter("itemId", "eq", mutableListOf(itemId.toString())))
        }

        if (locationId != 0) {
            filters.add(Filter("locationId", "eq", mutableListOf(locationId.toString())))
        }
        if(filters.isNotEmpty()){
            getInventoryList()
        }else{
            filters = mutableListOf()
            getInventoryList()
        }
    }

    private fun resetForm() {
        pagination = TempPagination(1, 0, 2, 0)
        binding.inventoryList.text.clear()
        binding.locationsList.text.clear()
        itemId = 0
        locationId = 0
        filters.clear()
        filters = mutableListOf()
        firstRound = true
        getInventoryList()
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
                        this@InventoryActivity,
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
        locationId = id.toInt()
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
                        this@InventoryActivity,
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
    }




}