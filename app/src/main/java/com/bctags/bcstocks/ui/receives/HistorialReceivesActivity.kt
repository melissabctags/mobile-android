package com.bctags.bcstocks.ui.receives

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityHistorialReceivesBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.CarrierResponse
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.ui.receives.adapter.ReceivesAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistorialReceivesActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityHistorialReceivesBinding

    private val apiClient = ApiClient().apiService
    val dropDown = DropDown()
    val apiCall = ApiCall()
    val SERVER_ERROR = "Server error, try later"
    private lateinit var adapter: ReceivesAdapter

    val mapCarriers: HashMap<String, String> = HashMap()
    val mapSuppliers: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialReceivesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCarrierList()
        initUI()
        getReceivesList()

    }

    private fun getReceivesList() {
        val pag = Pagination(1, 100)
        val filter = mutableListOf(Filter("", "", mutableListOf("")))
        val requestBody = FilterRequest(filter, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.receiveList(requestBody),
                onSuccess = { response ->
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Log.i("ERROR",error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
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

    fun onReceiveSelected(receive:ReceiveData){

    }

    private fun initUI() {
        binding.btnHistorialNew.setOnClickListener {
            toNewReceive()
        }

    }

    private fun toNewReceive() {
        val intent = Intent(this, HistorialReceivesActivity::class.java)
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
                        mapCarriers[i.name] = i.id.toString();
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
        //newReceive.carrierId = id.toInt()
    }



}