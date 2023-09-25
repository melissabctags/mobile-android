package com.bctags.bcstocks.ui.workorders.picking

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityPickingItemBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.PickingItem
import com.bctags.bcstocks.model.PickingRequest
import com.bctags.bcstocks.ui.workorders.picking.adapter.LocationPickAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.InputFilterMinMax
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PickingItemActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityPickingItemBinding
    private lateinit var adapter: LocationPickAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()

    var barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder


    val SERVER_ERROR = "Server error, try later"

    private val itemData:ItemData= ItemData(0,"","","",false,0,false,0,1.1,0,"")
    private var itemWorkOrder: ItemWorkOrder= ItemWorkOrder(0,0,"","",itemData)
    private var partialId: Int =0
    private var workOrderId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickingItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val gson = Gson()
        itemWorkOrder = gson.fromJson(intent.getStringExtra("ITEM"), ItemWorkOrder::class.java)
        val extras = intent.extras
        if (extras != null) {
            workOrderId = extras.getInt("WORK_ORDER_ID")
            partialId = extras.getInt("PARTIAL_ID")
            Log.i("partialId", partialId.toString())
            Log.i("workOrderId", workOrderId.toString())
            initUI()
            getLocations()
        }
        initListeners()
    }
    fun close() {
        barcodeDecoder.close()
    }
    fun open(dialog:Dialog,location:LocationData) {
        barcodeDecoder.open(this)
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                //todo: ask barcode and locations relation
                // barcodeEntity.barcodeData==location.name
                if( barcodeEntity.barcodeData!=""){
                    var btnSaveChange: MaterialButton = dialog.findViewById(R.id.btnSaveChange)
                    btnSaveChange.visibility = View.VISIBLE
                }else{
                    Toast.makeText(applicationContext, "Location doesn't match. Try again.", Toast.LENGTH_LONG).show()
                }
                stop()
            } else {
                Toast.makeText(applicationContext, "Scanning error", Toast.LENGTH_LONG).show()
                Log.i("BARCODE", "FAILED")
            }
        }
    }
    fun start(dialog:Dialog,location:LocationData) {
        open(dialog,location)
    }
    fun stop() {
        close()
        barcodeDecoder.stopScan()
    }
    @SuppressLint("SetTextI18n")
    fun selectItem(inventoryData: InventoryData) {
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_picking_item)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvItemDescription: TextView = dialog.findViewById(R.id.tvItemDescription)
        val tvLocation: TextView = dialog.findViewById(R.id.tvLocation)
        val tvQuantity: TextView = dialog.findViewById(R.id.tvQuantity)
        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)

        if(inventoryData.quantity>itemWorkOrder.quantity){
            etQuantity.filters = arrayOf<InputFilter>(InputFilterMinMax("1", itemWorkOrder.quantity.toString()))
        }else{
            etQuantity.filters = arrayOf<InputFilter>(InputFilterMinMax("1", inventoryData.quantity.toString()))
        }

        tvItemDescription.text= inventoryData.Item.item+" - "+inventoryData.Item.description
        tvLocation.text= inventoryData.Location.name+" - "+inventoryData.Branch.name
        tvQuantity.text= inventoryData.quantity.toString()

        val btnScanBarCode: MaterialButton = dialog.findViewById(R.id.btnScanBarCode)
        btnScanBarCode.setOnClickListener {
            start(dialog,inventoryData.Location)
        }

        val btnSaveChange: MaterialButton = dialog.findViewById(R.id.btnSaveChange)
        btnSaveChange.setOnClickListener {
            pickInventory(dialog,inventoryData)
        }
        dialog.show()
    }
    @SuppressLint("SetTextI18n")
    private fun pickInventory(dialog: Dialog, inventoryData: InventoryData) {
        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)
        if(etQuantity.text.toString().isNullOrEmpty() || etQuantity.text.toString()=="0"){
            Toast.makeText(applicationContext, "Must enter a quantity to pick", Toast.LENGTH_LONG).show()
        }else{
            val pickingItem = PickingItem(partialId,inventoryData.itemId,inventoryData.id,etQuantity.text.toString().toInt())
            val list:MutableList<PickingItem> = mutableListOf()
            list.add(pickingItem)
            saveItemPicked(list)
            getLocations()
            val myNewInt: Int = etQuantity.text.toString().toInt()
            binding.tvQty.text = (itemWorkOrder.quantity + myNewInt).toString()
            dialog.hide()
        }

    }
    private fun saveItemPicked(list: MutableList<PickingItem>) {
       val requestBody = PickingRequest(list)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.pickingItem(requestBody),
                onSuccess = { response ->
                    Toast.makeText(applicationContext, "SAVED", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun initRecyclerView(list: MutableList<InventoryData>) {
        adapter = LocationPickAdapter(
            list = list,
            onClickListener = { InventoryData -> selectItem(InventoryData) }
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }
    private fun getLocations() {
        val pag = Pagination(1, 100)
        val filter = mutableListOf(Filter("itemId", "eq", mutableListOf(itemWorkOrder.Item.id.toString())))
        val requestBody = FilterRequest(filter,pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getInventory(requestBody),
                onSuccess = { response ->
                    initRecyclerView(response.data)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initUI() {
        binding.tvItem.text = itemWorkOrder.Item.description
        binding.tvOrder.text = itemWorkOrder.quantity.toString()
        binding.tvQty.text = itemWorkOrder.quantity.toString()
    }
    private fun initListeners() {
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnDone.setOnClickListener{
               backToMainPicking()
        }
    }

    private fun backToMainPicking() {
        val intent = Intent(this, PickingListActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
        startActivity(intent)
    }


}

