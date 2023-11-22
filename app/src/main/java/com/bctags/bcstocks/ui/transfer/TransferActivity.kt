package com.bctags.bcstocks.ui.transfer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.databinding.ActivityTransferBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.TransferData
import com.bctags.bcstocks.io.response.TransferResponse
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.io.response.WorkOrderResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.ui.transfer.adapter.TransferAdapter
import com.bctags.bcstocks.ui.transfer.adapter.TransferLocationAdapter
import com.bctags.bcstocks.ui.workorders.adapter.WorkOrdersAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTransferBinding
    private lateinit var adapter: TransferAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson = Gson()
    private val dropDown = DropDown()
    private val utils = Utils()
    val tools = EPCTools()


    val SERVER_ERROR = "Server error, try later"
    var firstRound = true
    private var branchId = 0
    var filters = mutableListOf(Filter("", "", mutableListOf("")))
    var pagination = TempPagination(1, 0, 2, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)

        initListeners()

        getTransfers()

    }
    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination = utils.prevPagination(pagination)
            binding.tvPage.text = buildString {
                append("Page ")
                append(pagination.currentPage.toString())
            }
            getTransfers()
        }
    }

    private fun NextPagination() {
        if (pagination.nextPage != 0) {
            pagination = utils.NextPagination(pagination)
            binding.tvPage.text = buildString {
                append("Page ")
                append(pagination.currentPage.toString())
            }
            getTransfers()
        }
    }

    private fun getTransfers() {
        val pag = Pagination(pagination.currentPage, pagination.pageSize)
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getTransfer(requestBody),
                onSuccess = { response ->
                    useTransfer(response)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun useTransfer(response: TransferResponse) {
        if (firstRound) {
            pagination = utils.ínitPagination(response.pagination.totals, pagination)
            firstRound = false
        }
        initRecyclerView(response.data)
    }

    private fun initRecyclerView(list: MutableList<TransferData>) {
        adapter = TransferAdapter(
            list = list,
            onClickListener = { TransferData -> checkTransfer(TransferData) },
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun checkTransfer(transferData: TransferData) {

    }

    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnNewTransfer.setOnClickListener {
            newTransfer()
        }
        binding.btnPrev.setOnClickListener {
            prevPagination()
        }
        binding.btnNext.setOnClickListener {
            NextPagination()
        }
    }

    private fun newTransfer() {
        val intent = Intent(this, NewTransferActivity::class.java)
        startActivity(intent)
    }


}