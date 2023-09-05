package com.bctags.bcstocks.ui.workorders.picking

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityPickingListBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.WorkOrder
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

    val SERVER_ERROR = "Server error, try later"
    private val client: ClientData =ClientData(0,"","","","","","","","","","","","","")
    private val branch:Branch= Branch(0,0,"","","","","","","","")
    private var workOrder: WorkOrderData? = WorkOrderData(0,0,"",0,0,0,"","","","","","","","","","","","","",0,"","","",client,branch,mutableListOf())
    private var partialId: Int=0
    private var workOrderId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            workOrderId = extras.getInt("WORK_ORDER_ID")
            partialId = extras.getInt("PARTIAL_ID")
            getWorkOrder(workOrderId)
            if(partialId==0){
                createPartial(workOrderId)
            }
        }
        initListeners()
    }

    private fun initRecyclerView(list: MutableList<ItemWorkOrder>) {
        adapter = PickingItemsAdapter(
            list = list,
            onClickListener = { ItemWorkOrder -> viewItemLocations(ItemWorkOrder) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    fun viewItemLocations(itemWorkOrder: ItemWorkOrder) {
        val intent = Intent(this, PickingItemActivity::class.java)
        val gson = Gson()
        intent.putExtra("ITEM", gson.toJson(itemWorkOrder))
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }

    private fun createPartial(id: Int) {
        val requestBody = WorkOrder(id)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.newPartial(requestBody),
                onSuccess = { response ->
                    partialId = response.data.id
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.llAccordeon.setOnClickListener {
            expandCardView()
        }

    }

    private fun initUI() {
        binding.tvOrderNum.text = "Work order ${workOrder?.number}"
        binding.tvClient.text = workOrder?.Client?.name ?: ""
        binding.tvPoReference.text = workOrder?.poReference ?: ""
        binding.tvPo.text = workOrder?.po ?: ""
        binding.tvDeliveryDate.text = workOrder?.dateOrderPlaced ?: ""
    }

    private fun expandCardView() {
        if (binding.llOrderDetails.visibility == View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            }
            binding.llOrderDetails.visibility = View.GONE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_down_black)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            }
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
                    workOrder = response.data
                    initRecyclerView(response.data.Items)
                    initUI()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }

    }


}