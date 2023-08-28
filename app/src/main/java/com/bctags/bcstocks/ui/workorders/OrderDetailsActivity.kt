package com.bctags.bcstocks.ui.workorders

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityOrderDetailsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.ui.workorders.adapter.WorkOrderDetailsAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson

class OrderDetailsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityOrderDetailsBinding
    private lateinit var adapter: WorkOrderDetailsAdapter

    private lateinit var workOrder: WorkOrderData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gson = Gson()
        workOrder = gson.fromJson(intent.getStringExtra("WORK_ORDER"), WorkOrderData::class.java)
        initUI()
        initListener()
    }
    private fun initListener() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun initUI() {
        binding.tvOrderNumber.text = "Work order ${workOrder.number}"
        binding.tvClient.text = workOrder.Client.name
        binding.tvPoReference.text = workOrder.poReference
        binding.tvPo.text = workOrder.po
        binding.tvDeliveryDate.text = workOrder.dateOrderPlaced
        initRecyclerView(workOrder.Items)
    }
    private fun initRecyclerView(list: MutableList<ItemWorkOrder>) {
        adapter = WorkOrderDetailsAdapter(
            list = list
        )
        binding.recyclerWorkOrderItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerWorkOrderItems.adapter = adapter
    }


}