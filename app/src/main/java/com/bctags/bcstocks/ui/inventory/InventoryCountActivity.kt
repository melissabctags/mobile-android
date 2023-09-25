package com.bctags.bcstocks.ui.inventory

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityInventoryCountBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.model.CountLocation
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.ui.inventory.adapterInventoryCount.CountLocationsAdapter
import com.bctags.bcstocks.ui.locations.adapter.ItemLocationAdapter
import com.bctags.bcstocks.ui.workorders.OrderDetailsActivity
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InventoryCountActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityInventoryCountBinding
    private lateinit var adapter: CountLocationsAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val dropDown= DropDown()

    val SERVER_ERROR = "Server error, try later"
    private var branchId=0
    var mapLocations: HashMap<String, String> = HashMap()
    var selectedLocations: MutableList<CountLocation> = mutableListOf()
    var tempLocation: CountLocation = CountLocation(0,"")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInventoryCountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        getLocations()
        initListeners()
       // initRecyclerView()
    }
    private fun initListeners() {
        binding.btnAdd.setOnClickListener{
            addLocationToList()
        }
        binding.btnNext.setOnClickListener{
            goInventoryCount()
        }
    }
    private fun goInventoryCount() {
        val intent = Intent(this, CountInventoryActivity::class.java)
        val gson = Gson()
        intent.putExtra("LOCATIONS_SELECTED", gson.toJson(tempLocation))
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
                    val autoComplete: AutoCompleteTextView = findViewById(com.bctags.bcstocks.R.id.locationsList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapLocations,
                        this@InventoryCountActivity,
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
        tempLocation.id = id.toInt()
        tempLocation.name = text
        binding.btnNext.visibility= View.VISIBLE;
    }
    private fun addLocationToList() {
        Log.i("tempLocation",tempLocation.toString())
        Log.i("selectedLocations",selectedLocations.toString())
        val existingItem = selectedLocations.find { it.id == tempLocation.id }
        if (existingItem == null) {
            selectedLocations.add(CountLocation(tempLocation.id,tempLocation.name))
            adapter.notifyDataSetChanged()
        }
    }
    private fun initRecyclerView() {
        adapter = CountLocationsAdapter(
            list = selectedLocations,
        ) { CountLocation, position -> deleteSelected(CountLocation, position) }
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }
    private fun deleteSelected(countLocation: CountLocation,position:Int) {
        selectedLocations.removeAll { it.id == countLocation.id }
        adapter.notifyItemRemoved(position)
    }







}











