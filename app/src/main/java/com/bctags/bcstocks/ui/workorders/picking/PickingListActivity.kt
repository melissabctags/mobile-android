package com.bctags.bcstocks.ui.workorders.picking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityPickingListBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PartialResponse
import com.bctags.bcstocks.io.response.PickedData
import com.bctags.bcstocks.io.response.PickedItem
import com.bctags.bcstocks.io.response.PickedWorkOrderData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.WorkOrder
import com.bctags.bcstocks.model.WorkOrderNewPartial
import com.bctags.bcstocks.model.WorkOrderStatus
import com.bctags.bcstocks.ui.workorders.packing.PackingActivity
import com.bctags.bcstocks.ui.workorders.picking.adapter.PickingItemsAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PickingListActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityPickingListBinding
    private lateinit var adapter: PickingItemsAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    val gson = Gson()

    val SERVER_ERROR = "Server error, try later"
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

    var workOrdersPref: String = "{}"
    var pickedList: List<PickedItem> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickingListBinding.inflate(layoutInflater)
        setContentView(binding.root)
// val scope =
        CoroutineScope(Dispatchers.Default).launch {
            val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
            if (sharedPreferences.contains("WORK_ORDERS")) {
                workOrdersPref = sharedPreferences.getString("WORK_ORDERS", "{}").toString()
                Log.i("WORK_ORDERS Pickinlist", workOrdersPref)
            } else {
                sharedPreferences.edit().putString("WORK_ORDERS", "{}").apply()
            }

            val extras = intent.extras
            if (extras != null) {
                workOrderId = extras.getInt("WORK_ORDER_ID")
                partialId = extras.getInt("PARTIAL_ID")
                getPicked(workOrderId)
                if (partialId == 0) {
                    createPartial(workOrderId)
                }
            }
            initListeners()
        }
    }
    private fun getPicked(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPickedWorkOrder(WorkOrder(id)),
                onSuccess = { response ->
                    pickedList = response.data.picked
                    getWorkOrder(workOrderId)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initRecyclerView(list: MutableList<ItemWorkOrder>) {
        adapter = PickingItemsAdapter(
            list = list,
            onClickListener = { ItemWorkOrder -> viewItemLocations(ItemWorkOrder) },
            pickedItems = pickedList
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    fun viewItemLocations(itemWorkOrder: ItemWorkOrder) {
        val intent = Intent(this, PickingItemActivity::class.java)
        intent.putExtra("ITEM", gson.toJson(itemWorkOrder))
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }

    private fun createPartial(id: Int) {
        val requestBody = WorkOrderNewPartial(id)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.newPartial(requestBody),
                onSuccess = { response ->
                    initPartial(response)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initPartial(response: PartialResponse) {
        partialId = response.data.id
        var workOrderStatus = mutableListOf<WorkOrderStatus>()
        if (workOrdersPref.isNotEmpty() && workOrdersPref != "{}") {
            workOrderStatus = gson.fromJson(workOrdersPref, Array<WorkOrderStatus>::class.java).toMutableList()
            workOrderStatus.add(WorkOrderStatus(workOrderId, "pick", response.data.id))
        } else {
            workOrderStatus.add(WorkOrderStatus(workOrderId, "pick", response.data.id))
        }
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("WORK_ORDERS", gson.toJson(workOrderStatus)).apply()
    }

    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.llAccordeon.setOnClickListener {
            expandCardView()
        }
        binding.btnFinish.setOnClickListener {
            finishPicking()
        }
    }

    private fun finishPicking() {

        val workOrderStatus = gson.fromJson(workOrdersPref, Array<WorkOrderStatus>::class.java).asList().toMutableList()
        workOrderStatus.removeAll { it.id == workOrderId && it.partialId == partialId }
        workOrderStatus.add(WorkOrderStatus(workOrderId, "pack", partialId))

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("WORK_ORDERS", gson.toJson(workOrderStatus)).apply()

        Log.i("WORK_ORDERS", workOrderStatus.toString())

        utils.getChangeStatus(partialId, "packing", this)

        val intent = Intent(this, PackingActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
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
                    useWorkOrder(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useWorkOrder(data: WorkOrderData) {
        workOrder = data
        initRecyclerView(data.Items)
        initUI()
    }


}