package com.bctags.bcstocks.ui.workorders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityHistorialReceivesBinding
import com.bctags.bcstocks.databinding.ActivityOrdersBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.ui.receives.ScannerReceiveActivity
import com.bctags.bcstocks.ui.receives.adapter.ReceivesAdapter
import com.bctags.bcstocks.ui.workorders.adapter.WorkOrdersAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
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

    val SERVER_ERROR = "Server error, try later"
    var firstRound = true
    var filters = mutableListOf(Filter("", "", mutableListOf("")))
    var pagination = TempPagination(1, 0, 2, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getWorkOrders()
        initUI()
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

    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination=utils.prevPagination(pagination)
            binding.tvPage.text = "Page ${pagination.currentPage.toString()}"
            getWorkOrders()
        }
    }

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
            onSecondClickListener = { WorkOrderData -> pickWorkOrder(WorkOrderData) }
        )
        binding.recyclerWorkOrders.layoutManager = LinearLayoutManager(this)
        binding.recyclerWorkOrders.adapter = adapter
    }

    fun viewWorkOrder(workOrderData: WorkOrderData) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val gson = Gson()
        intent.putExtra("WORK_ORDER", gson.toJson(workOrderData))
        startActivity(intent)
    }

    fun pickWorkOrder(workOrderData: WorkOrderData) {
        Toast.makeText(applicationContext, "PICK", Toast.LENGTH_LONG).show()
    }


}