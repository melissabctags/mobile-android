package com.bctags.bcstocks.ui.workorders.picking

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityPickingItemBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.ui.workorders.picking.adapter.LocationPickAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PickingItemActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityPickingItemBinding
    private lateinit var adapter: LocationPickAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()

    val SERVER_ERROR = "Server error, try later"

    private val itemData:ItemData= ItemData(0,"","","",false,0,false,0,1.1,0,"")
    private var itemWorkOrder: ItemWorkOrder= ItemWorkOrder(0,0,"","",itemData)
    private var partialId: Int =0
    private var workOrderId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickingItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gson = Gson()
        itemWorkOrder = gson.fromJson(intent.getStringExtra("ITEM"), ItemWorkOrder::class.java)
        val extras = intent.extras
        if (extras != null) {
            workOrderId = extras.getInt("WORK_ORDER_ID")
            partialId = extras.getInt("PARTIAL_ID")
            initUI()
            getLocations()
        }
        initListeners()

    }

    private fun initRecyclerView(list: MutableList<InventoryData>) {
        adapter = LocationPickAdapter(
            list = list,
            onClickListener = { InventoryData -> selectItem(InventoryData) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    fun selectItem(InventoryData: InventoryData) {

    }

    private fun getLocations() {
        val pag = Pagination(1, 100)
        val filter = mutableListOf(Filter("itemId", "eq", mutableListOf(itemWorkOrder.Item.id.toString())))
        val requestBody = FilterRequest(filter,pag)

        Log.i("filters",requestBody.toString())

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initUI() {
        binding.tvItem.text = itemWorkOrder.Item.description
        binding.tvOrder.text = itemWorkOrder.quantity.toString()
        binding.tvQty.text = itemWorkOrder.quantity.toString()
    }
    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }



}