package com.bctags.bcstocks.ui.workorders.packing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityPackingBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FiltersRequest
import com.bctags.bcstocks.model.WorkOrder
import com.bctags.bcstocks.model.WorkOrderStatus
import com.bctags.bcstocks.ui.shipping.ShippingActivity
import com.bctags.bcstocks.ui.workorders.packing.adapter.PackAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackingActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityPackingBinding
    private lateinit var adapter: PackAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    private val gson = Gson()

    val SERVER_ERROR = "Server error, try later"
    var workOrdersPref: String = "{}"
    private val client: ClientData =
        ClientData(0, "", "", "", "", "", "", "", "", "", "", "", "", "")
    private val branch: Branch = Branch(0, 0, "", "", "", "", "", "", "", "")
    private var workOrder: WorkOrderData? = WorkOrderData(
        0,
        0,
        "",
        0,
        0,
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0,
        "",
        "",
        "",
        client,
        branch,
        mutableListOf()
    )
    private var partialId: Int = 0
    private var workOrderId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("WORK_ORDERS")) {
            workOrdersPref = sharedPreferences.getString("WORK_ORDERS", "{}").toString()
            // Log.i("WORK_ORDERS pack",workOrdersPref)
        } else {
            sharedPreferences.edit().putString("WORK_ORDERS", "{}").apply()
        }
        val extras = intent.extras
        if (extras != null) {
            workOrderId = extras.getInt("WORK_ORDER_ID")
            partialId = extras.getInt("PARTIAL_ID")
            getWorkOrder(workOrderId)
        }
        initListeners()
    }

    private fun initRecyclerView(list: MutableList<ItemWorkOrder>) {
        var listPacked = mutableListOf<PackedData>()
        getPacked { packedList ->
            adapter = if (packedList != null) {
                PackAdapter(list, partialId, packedList.toMutableList())
            } else {
                PackAdapter(list, partialId, listPacked)
            }
            binding.recyclerList.layoutManager = LinearLayoutManager(this)
            binding.recyclerList.adapter = adapter
        }
    }

    private fun getPacked(callback: (List<PackedData>?) -> Unit) {
        val listFilters = mutableListOf<Filter>()
        listFilters.add(Filter("fillOrderId", "eq", mutableListOf(partialId.toString())))

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPacked(FiltersRequest(listFilters)),
                onSuccess = { response ->
                    callback(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.llAccordeon.setOnClickListener {
            expandCardView()
        }
        binding.btnFinish.setOnClickListener {
            finishPacking()
        }
        binding.btnNewPack.setOnClickListener {
            openNewPack()
        }
        binding.btnPacked.setOnClickListener {
            openPackedList()
        }
    }

    private fun openPackedList() {
        val intent = Intent(this, PackedActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }

    private fun finishPacking() {
        var workOrderStatus =
            gson.fromJson(workOrdersPref, Array<WorkOrderStatus>::class.java).asList()
                .toMutableList()
        workOrderStatus.removeAll { it.id == workOrderId && it.partialId == partialId }
        workOrderStatus.add(WorkOrderStatus(workOrderId, "ship", partialId))

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("WORK_ORDERS", gson.toJson(workOrderStatus)).apply()

        Log.i("WORK_ORDERS", workOrderStatus.toString())

        utils.getChangeStatus(partialId, "shipping", this)

        val intent = Intent(this, ShippingActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }

    private fun openNewPack() {
        val intent = Intent(this, NewPackActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }

    private fun expandCardView() {
        if (binding.llOrderDetails.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.llOrderDetails.visibility = View.GONE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_down_black)
        } else {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.llOrderDetails.visibility = View.VISIBLE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_up_black)
        }
    }

    private fun getWorkOrder(id: Int) {
        val requestBody = WorkOrder(id)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getWorkOrder(requestBody),
                onSuccess = { response ->
                    initWorkOrder(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initWorkOrder(data: WorkOrderData) {
        workOrder = data
        initRecyclerView(data.Items)
        initUI()
    }


    private fun initUI() {
        binding.tvOrderNum.text = buildString {
            append("Work order ")
            append(workOrder?.number)
        }
        binding.tvClient.text = workOrder?.Client?.name ?: ""
        binding.tvPoReference.text = workOrder?.poReference ?: ""
        binding.tvPo.text = workOrder?.po ?: ""
        binding.tvDeliveryDate.text = workOrder?.dateOrderPlaced ?: ""
        binding.tvCreateDate.text = workOrder?.let { utils.dateFormatter(it.createdAt) }
    }


}