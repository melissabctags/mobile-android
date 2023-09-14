package com.bctags.bcstocks.ui.shipping

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityPackingBinding
import com.bctags.bcstocks.databinding.ActivityShippingBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.CarrierData
import com.bctags.bcstocks.io.response.CarrierResponse
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.FillOrderData
import com.bctags.bcstocks.io.response.FillOrderResponse
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FiltersRequest
import com.bctags.bcstocks.model.ShipRequest
import com.bctags.bcstocks.model.WorkOrder
import com.bctags.bcstocks.model.WorkOrderStatus
import com.bctags.bcstocks.ui.workorders.WorkOrdersActivity
import com.bctags.bcstocks.ui.workorders.packing.adapter.PackAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShippingActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityShippingBinding
    private lateinit var adapter: PackAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    private val gson= Gson()
    private val dropDown= DropDown()

    val SERVER_ERROR = "Server error, try later"
    var workOrdersPref:String= "{}"
    private val client: ClientData = ClientData(0,"","","","","","","","","","","","","")
    private val branch: Branch = Branch(0,0,"","","","","","","","")
    private var workOrder: WorkOrderData? = WorkOrderData(0,0,"",0,0,0,"","","","","","","","","","","","","",0,"","","",client,branch,mutableListOf())
    private var partialId: Int=0
    private var workOrderId: Int = 0
    private var carrier: CarrierData = CarrierData(0,"")
    val mapCarriers: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("WORK_ORDERS")) {
            workOrdersPref = sharedPreferences.getString("WORK_ORDERS", "{}").toString()
            Log.i("WORK_ORDERS pack",workOrdersPref)
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
        getCarrierList()
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
                        this@ShippingActivity,
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
        carrier.id = id.toInt()
        carrier.name = text
    }
    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.llAccordeon.setOnClickListener {
            expandCardView()
        }
        binding.btnFinish.setOnClickListener {
            saveShipping()
        }
    }
    private fun saveShipping(){
        if(binding.etTrackingNumber.text.toString().isNotEmpty() && carrier.id!=0){
            getPartial { fillOrders ->
                if (fillOrders != null) {
                    sendShipping(ShipRequest(partialId,fillOrders[0].id,carrier.id,carrier.name,binding.etTrackingNumber.text.toString()))
                }
            }
        }else {
            Toast.makeText(applicationContext, "Please input a tracking number and carrier", Toast.LENGTH_LONG).show()
        }
    }
    private fun getPartial(callback: (List<FillOrderData>?) -> Unit) {
        val listFilters= mutableListOf<Filter>()
        listFilters.add(Filter("id","eq", mutableListOf(partialId.toString())))

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPartial(FiltersRequest(listFilters)),
                onSuccess = { response ->
                    val fillOrders = response.data
                    callback(fillOrders)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun sendShipping(data:ShipRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.setShip(data),
                onSuccess = { response ->
                    Toast.makeText(applicationContext, "Saved", Toast.LENGTH_LONG).show()
                    finishShipping()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun finishShipping() {
        var workOrderStatus = gson.fromJson(workOrdersPref, Array<WorkOrderStatus>::class.java).asList().toMutableList()
        workOrderStatus.removeAll { it.id == workOrderId && it.partialId == partialId }

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("WORK_ORDERS", gson.toJson(workOrderStatus)).apply()

        utils.getChangeStatus(partialId,"done",this)

        val intent = Intent(this, WorkOrdersActivity::class.java)
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
                    workOrder = response.data
                    initUI()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun initUI() {
        binding.tvOrderNum.text = "Work order ${workOrder?.number}"
        binding.tvClient.text = workOrder?.Client?.name ?: ""
        binding.tvPoReference.text = workOrder?.poReference ?: ""
        binding.tvPo.text = workOrder?.po ?: ""
        binding.tvDeliveryDate.text = workOrder?.dateOrderPlaced ?: ""
        binding.tvCreateDate.text = workOrder?.let { utils.dateFormatter(it.createdAt) }
    }



}