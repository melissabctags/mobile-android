package com.bctags.bcstocks.ui.receives

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityNewReceiveBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.BranchData
import com.bctags.bcstocks.io.response.CarrierData
import com.bctags.bcstocks.io.response.PurchaseOrderData
import com.bctags.bcstocks.io.response.PurchaseOrderResponse
import com.bctags.bcstocks.io.response.SupplierData
import com.bctags.bcstocks.io.response.TransferData
import com.bctags.bcstocks.io.response.TransferOrderData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewReceiveActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityNewReceiveBinding
    private val messageDialog = MessageDialog()
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val dropDown = DropDown()

    val mapPurchaseOrders: HashMap<String, String> = HashMap()
    val mapCarriers: HashMap<String, String> = HashMap()

    var newReceive: ReceiveNew = ReceiveNew(0, 0, "", mutableListOf(), "", "")
    var purchaseOrder: PurchaseOrderData = PurchaseOrderData(
        0,
        "",
        0,
        0,
        "",
        "",
        "",
        BranchData(0, ""),
        mutableListOf(),
        SupplierData(0, "")
    )
    val SERVER_ERROR = "Server error, try later"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getPurchaseOrderList()
        getCarrierList()
        initUI()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294) {
            checkForm()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initUI() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnScan.setOnClickListener {
            checkForm()
        }
        binding.btnNewHistorial.setOnClickListener {
            toHistory()
        }
    }

    private fun toHistory() {
        val intent = Intent(this, HistorialReceivesActivity::class.java)
        startActivity(intent)
    }

    private fun checkForm() {
        if (newReceive.carrierId != 0 && newReceive.purchaseOrderId != 0) {
            if(newReceive.orderType.contains("purchaseOrder")){
                searchPo(newReceive.purchaseOrderId.toString())
            }else{
                getTransfer(newReceive.purchaseOrderId)
            }
            //scanActivity()
        } else {
            messageDialog.showDialog(
                this@NewReceiveActivity,
                R.layout.dialog_error,
                "Purchase order and Carried must be selected "
            ) { }
        }
    }

    private fun scanActivity() {
        val intent = Intent(this, ScannerReceiveActivity::class.java)
        newReceive.comments = binding.etComments.text.toString()
        newReceive.invoice = binding.etInvoice.text.toString()
        val gson = Gson()
        if(newReceive.orderType.contains("purchaseOrder")){
            intent.putExtra("PURCHASE_ORDER", gson.toJson(purchaseOrder))
        }else{
            intent.putExtra("PURCHASE_ORDER", gson.toJson(transferOrder))
        }
        intent.putExtra("RECEIVE", gson.toJson(newReceive))
        //intent.putExtra("PURCHASE_ORDER", gson.toJson(purchaseOrder))
        startActivity(intent)
    }
    private var transferOrder: TransferOrderData = TransferOrderData(0,"","","","","",
        mutableListOf()
    )
    private fun getTransfer(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getTransferOrder(GetOne(id)),
                onSuccess = { response ->
                    Log.i("getTransfer",response.data.toString())
                    transferOrder = response.data
                    scanActivity()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun searchPo(id: String) {
        val pag = Pagination(1, 100)
        val poFilter = mutableListOf(Filter("id", "eq", mutableListOf(id)))
        val poRequestBody = FilterRequest(poFilter, pag)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPurchaseOrder(poRequestBody),
                onSuccess = { response ->
                    Log.i("purchaseOrder",response.list[0].toString())
                    purchaseOrder = (response.list[0])
                    scanActivity()
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getCarrierList() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getCarrierList(),
                onSuccess = { response ->
                    useCarriers(response.list)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useCarriers(carrierResponse: MutableList<CarrierData>) {
        val list: MutableList<String> = mutableListOf()
        carrierResponse.forEach { i ->
            list.add(i.name)
            mapCarriers[i.name] = i.id.toString();
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.carrierList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapCarriers,
            this@NewReceiveActivity,
            ::updateCarriers
        )
    }

    var poList: MutableList<PurchaseOrderData> = mutableListOf()
    private fun getPurchaseOrderList() {
        val pag = Pagination(1, 100)
        val poFilter = mutableListOf(Filter("status", "or", mutableListOf("sent", "in_process")))
        val poRequestBody = FilterRequest(poFilter, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPurchaseOrder(poRequestBody),
                onSuccess = { response ->
                    // usePurchaseOrderList(response.list)
                    poList = response.list
                    getTransfers()
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getTransfers() {
        val pag = Pagination(1, 100)
        val filters = mutableListOf(Filter("status", "eq", mutableListOf("sent")))
        val requestBody = FilterRequest(filters, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getTransferOrders(requestBody),
                onSuccess = { response ->
                    //useTransfer(response)
                    usePurchaseOrderList(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    //    private fun usePurchaseOrderList(purchaseOrderResponse: MutableList<PurchaseOrderData>,) {
//        val list: MutableList<String> = mutableListOf()
//        purchaseOrderResponse.forEach { po ->
//            list.add(po.number)
//            mapPurchaseOrders[po.number] = po.id.toString();
//        }
//        val autoComplete: AutoCompleteTextView = findViewById(R.id.purchaseOrderList)
//        dropDown.listArrange(list,autoComplete,mapPurchaseOrders,this@NewReceiveActivity,::updatePo)
//    }
    private fun usePurchaseOrderList(transferList: MutableList<TransferData>) {
        val list: MutableList<String> = mutableListOf()
        transferList.forEach { po ->
            list.add(po.number)
            mapPurchaseOrders[po.number] = po.id.toString();
        }
        poList.forEach { po ->
            list.add(po.number)
            mapPurchaseOrders[po.number] = po.id.toString();
        }
        Log.i("pos", list.toString())
        val autoComplete: AutoCompleteTextView = findViewById(R.id.purchaseOrderList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapPurchaseOrders,
            this@NewReceiveActivity,
            ::updatePo
        )
    }

    private fun updateCarriers(id: String, text: String) {
        newReceive.carrierId = id.toInt()
    }

    private fun updatePo(id: String, text: String) {
        newReceive.purchaseOrderId = id.toInt()
        if(text.contains("TO")){
            newReceive.orderType="transferOrder"
        }else{
            newReceive.orderType="purchaseOrder"
            searchPoSupplier(id)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun searchPoSupplier(id: String) {
        val pag = Pagination(1, 100)
        val poFilter = mutableListOf(Filter("id", "eq", mutableListOf(id)))
        val poRequestBody = FilterRequest(poFilter, pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPurchaseOrder(poRequestBody),
                onSuccess = { response ->
                    usePoSupplier(response)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun usePoSupplier(poResponse: PurchaseOrderResponse) {
        binding.tvPoSupplier.visibility = View.VISIBLE
        val text = "Supplier: " + poResponse.list[0].Supplier.name
        binding.tvPoSupplier.text = text
        purchaseOrder = poResponse.list[0]
    }

















}