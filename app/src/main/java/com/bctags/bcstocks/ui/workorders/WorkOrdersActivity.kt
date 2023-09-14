package com.bctags.bcstocks.ui.workorders

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityOrdersBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.ActionWorkOrder
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.model.WorkOrderStatus
import com.bctags.bcstocks.ui.shipping.ShippingActivity
import com.bctags.bcstocks.ui.workorders.adapter.WorkOrdersAdapter
import com.bctags.bcstocks.ui.workorders.packing.PackingActivity
import com.bctags.bcstocks.ui.workorders.picking.PickingListActivity
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkOrdersActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private lateinit var adapter: WorkOrdersAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    val gson= Gson()

    val SERVER_ERROR = "Server error, try later"
    var firstRound = true
    var filters = mutableListOf(Filter("", "", mutableListOf("")))
    var pagination = TempPagination(1, 0, 2, 0)

    var workOrdersPref:String= "{}"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWorkOrdersStatus()
        getWorkOrders()
        initUI()
    }

    private fun initWorkOrdersStatus(){
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("WORK_ORDERS")) {
            workOrdersPref = sharedPreferences.getString("WORK_ORDERS", "{}").toString()
            Log.i("WORK_ORDERS",workOrdersPref)
        } else {
            sharedPreferences.edit().putString("WORK_ORDERS", "{}").apply()
        }
//        var workOrderStatus = mutableListOf<WorkOrderStatus>()
//        workOrderStatus.add(WorkOrderStatus(116, "pick", 170))
//        sharedPreferences.edit().putString("WORK_ORDERS", gson.toJson(workOrderStatus)).apply()
    }

    private fun initUI() {
        binding.btnPrev.setOnClickListener {
            prevPagination()
        }
        binding.btnNext.setOnClickListener {
            NextPagination()
        }
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination=utils.prevPagination(pagination)
            binding.tvPage.text = "Page ${pagination.currentPage.toString()}"
            getWorkOrders()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun NextPagination() {
        if (pagination.nextPage != 0) {
            pagination=utils.NextPagination(pagination)
            binding.tvPage.text = "Page " + pagination.currentPage.toString()
            getWorkOrders()
        }
    }
    private fun getWorkOrders() {
        val pag = Pagination(pagination.currentPage, pagination.pageSize)
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.workOrderList(requestBody),
                onSuccess = { response ->
                    if (firstRound) {
                        pagination=utils.ínitPagination(response.pagination.totals,pagination)
                        firstRound = false
                    }
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }

    }
    private fun initRecyclerView(list: MutableList<WorkOrderData>) {
        adapter = WorkOrdersAdapter(
            list = list,
            onclickListener = { WorkOrderData -> viewWorkOrder(WorkOrderData) },
            onSecondClickListener = { ActionWorkOrder -> actionWorkOrder(ActionWorkOrder) },
            workordersString = workOrdersPref
        )
        binding.recyclerWorkOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerWorkOrders.adapter = adapter
    }

    fun viewWorkOrder(workOrderData: WorkOrderData) {3
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val gson = Gson()
        intent.putExtra("WORK_ORDER", gson.toJson(workOrderData))
        startActivity(intent)
    }

    fun actionWorkOrder(actionWorkOrder: ActionWorkOrder) {
        Log.i("actionWorkOrder",actionWorkOrder.toString())
        val intent: Intent = when (actionWorkOrder.partialStatus.moduleName) {
            "pick" -> {
                Intent(this, PickingListActivity::class.java)
            }
            "pack" -> {
                Intent(this, PackingActivity::class.java)
            }
            "shipping" -> {
                Intent(this, ShippingActivity::class.java)
            }
            "ship" -> {
                Intent(this, ShippingActivity::class.java)
            }
            else -> {
                Intent(this, PickingListActivity::class.java)
            }
        }
        intent.putExtra("WORK_ORDER_ID", actionWorkOrder.workOrder.id)
        intent.putExtra("PARTIAL_ID", actionWorkOrder.partialStatus.partialId)
        startActivity(intent)
    }


}