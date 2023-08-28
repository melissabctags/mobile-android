package com.bctags.bcstocks.ui.receives

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityHistorialReceivesBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.CarrierResponse
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.io.response.SupplierResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.ui.receives.adapter.ReceivesAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistorialReceivesActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityHistorialReceivesBinding
    private lateinit var adapter: ReceivesAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()

    private val dropDown = DropDown()
//    private val utils = Utils()

    val SERVER_ERROR = "Server error, try later"


    var mapCarriers: HashMap<String, String> = HashMap()
    var mapSuppliers: HashMap<String, String> = HashMap()
    var pagination = TempPagination(1, 0, 2, 0)
    var firstRound = true
    var filters = mutableListOf(Filter("", "", mutableListOf("")))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialReceivesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCarrierList()
        getSupplierList()
        initUI()
        getReceivesList()

    }

    private fun getReceivesList() {
        val pag = Pagination(pagination.currentPage, pagination.pageSize)
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.receiveList(requestBody),
                onSuccess = { response ->
                    Log.i("RESPONSE", response.pagination.toString())
                    if (firstRound) {
                       // ínitPagination(response.pagination.totals)
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
    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination=utils.prevPagination(pagination)
            binding.tvPage.text = "Page ${pagination.currentPage.toString()}"
            getReceivesList()
        }
    }
    private fun NextPagination() {
        if (pagination.nextPage != 0) {
            pagination=utils.NextPagination(pagination)
            binding.tvPage.text = "Page " + pagination.currentPage.toString()
            getReceivesList()
        }
    }

    private fun initRecyclerView(receiveList: MutableList<ReceiveData>) {
        adapter = ReceivesAdapter(
            receivesList = receiveList,
            onclickListener = { ReceiveData -> onReceiveSelected(ReceiveData) }
        )
        binding.recyclerReceives.layoutManager = LinearLayoutManager(this)
        binding.recyclerReceives.adapter = adapter
    }

    fun onReceiveSelected(receive: ReceiveData) {
        //TODO
    }

    private fun initUI() {
        binding.btnHistorialNew.setOnClickListener {
            toNewReceive()
        }
        binding.btnPrev.setOnClickListener {
            prevPagination()
        }
        binding.btnNext.setOnClickListener {
            NextPagination()
        }

        binding.tvSearch.setOnClickListener {
            expandCardView()
        }
        binding.cvFormSearch.setOnClickListener {

        }
    }

    private fun expandCardView() {
        if (binding.cvFormSearch.visibility == View.VISIBLE) {
            // The transition of the hiddenView is carried out by the TransitionManager class.
            // Here we use an object of the AutoTransition Class to create a default transition
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            }
            binding.cvFormSearch.visibility = View.GONE
            //arrow.setImageResource(R.drawable.ic_baseline_expand_more_24)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            }
            binding.cvFormSearch.visibility = View.VISIBLE
            // arrow.setImageResource(R.drawable.ic_baseline_expand_less_24)
        }
    }


    private fun toNewReceive() {
        val intent = Intent(this, NewReceiveActivity::class.java)
        startActivity(intent)
    }

    private fun getCarrierList() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getCarrierList(),
                onSuccess = { response ->
                    val carrierResponse: CarrierResponse? = response
                    var list: MutableList<String> = mutableListOf()
                    carrierResponse?.list?.forEach { i ->
                        list.add(i.name)
                        mapCarriers[i.name] = i.id.toString()
                    }
                    val autoComplete: AutoCompleteTextView = findViewById(R.id.carrierList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapCarriers,
                        this@HistorialReceivesActivity,
                        ::updateCarriers
                    )
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateCarriers(id: String, text: String) {
        //TODO
    }

    private fun getSupplierList() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.supplierList(),
                onSuccess = { response ->
                    val supplierResponse: SupplierResponse? = response
                    var list: MutableList<String> = mutableListOf()
                    supplierResponse?.list?.forEach { i ->
                        list.add(i.name)
                        mapSuppliers[i.name] = i.id.toString();
                    }
                    val autoComplete: AutoCompleteTextView = findViewById(R.id.supplierList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapSuppliers,
                        this@HistorialReceivesActivity,
                        ::updateSupplier
                    )
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateSupplier(id: String, text: String) {
        //TODO
    }


}