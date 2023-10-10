package com.bctags.bcstocks.ui.receives

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityReceiveDetailsBinding
import com.bctags.bcstocks.io.response.ItemReceive
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.ui.receives.adapter.ReceiveDetailsAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson

class ReceiveDetailsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityReceiveDetailsBinding
    private lateinit var adapter: ReceiveDetailsAdapter
    private val utils = Utils()

    private lateinit var receive: ReceiveData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiveDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val gson = Gson()
        receive = gson.fromJson(intent.getStringExtra("RECEIVE"), ReceiveData::class.java)
        initUI()
    }

    private fun initUI() {
        binding.tvNumber.text = buildString {
            append("Receive ")
            append(receive.number)
        }
        binding.tvInvoice.text = receive.invoice
        binding.tvPurchaseOrder.text = receive.purchaseOrder.number
        binding.tvSupplier.text = receive.purchaseOrder.supplier.name
        binding.tvDate.text = utils.dateFormatter(receive.createdAt)
        initRecyclerView(receive.Items)
    }

    private fun initRecyclerView(list: MutableList<ItemReceive>) {
        adapter = ReceiveDetailsAdapter(
            list = list
        )
        binding.recyclerItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerItems.adapter = adapter
    }


}