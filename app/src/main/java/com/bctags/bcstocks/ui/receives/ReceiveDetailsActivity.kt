package com.bctags.bcstocks.ui.receives

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityReceiveDetailsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.BranchData
import com.bctags.bcstocks.io.response.Carrier
import com.bctags.bcstocks.io.response.ItemGetOneReceive
import com.bctags.bcstocks.io.response.PurchaseOrderRec
import com.bctags.bcstocks.io.response.ReceiveGetOneData
import com.bctags.bcstocks.io.response.SupplierRec
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.ui.receives.adapter.ReceiveDetailsAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiveDetailsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityReceiveDetailsBinding
    private lateinit var adapter: ReceiveDetailsAdapter
    private val utils = Utils()
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()

    private var receive: ReceiveGetOneData =
        ReceiveGetOneData(
            0, 0, "", "", "", "",
            BranchData(0, ""), Carrier(""),
            PurchaseOrderRec("", SupplierRec("")), "", "", mutableListOf()
        )
    private lateinit var numberOrder: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val extras = intent.extras
        if (extras != null) {
            receive.id = extras.getInt("RECEIVE")
        }
        lifecycleScope.launch {
            getReceive()
        }
    }

    private fun getReceive() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.receiveGetOne(GetOne(receive.id)),
                onSuccess = { response ->
                    receive = response.data
                    checkTypeReceive()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, "Server error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun checkTypeReceive() {
        if (receive.orderType.contains("po")) {
            getPo(receive.purchaseOrderId.toString())
        } else {
            getTransfer(receive.purchaseOrderId)
        }
    }

    private fun getPo(id: String) {
        val pag = Pagination(1, 100)
        val poFilter = mutableListOf(Filter("id", "eq", mutableListOf(id)))
        val poRequestBody = FilterRequest(poFilter, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPurchaseOrder(poRequestBody),
                onSuccess = { response ->
                    numberOrder = response.list[0].number
                    initUI()
                },
                onError = { error ->
                    Toast.makeText(applicationContext, "Server error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getTransfer(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getTransferOrder(GetOne(id)),
                onSuccess = { response ->
                    numberOrder = response.data.number
                    initUI()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, "Server error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initUI() {
        binding.tvNumber.text = buildString {
            append("Receive ")
            append(receive.number)
        }
        binding.tvInvoice.text = receive.invoice
        binding.tvPurchaseOrder.text = numberOrder
//        if (receive.orderType == "po") {
//            binding.llSupplier.visibility = View.VISIBLE
//            binding.tvSupplier.text = receive.purchaseOrder.supplier.name
//        }
        binding.tvDate.text = utils.dateFormatter(receive.createdAt)
        initRecyclerView(receive.Items)
    }

    private fun initRecyclerView(list: MutableList<ItemGetOneReceive>) {
        adapter = ReceiveDetailsAdapter(
            list = list
        )
        binding.recyclerItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerItems.adapter = adapter
    }


}