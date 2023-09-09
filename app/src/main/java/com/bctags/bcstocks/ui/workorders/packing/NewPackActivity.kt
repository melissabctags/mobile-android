package com.bctags.bcstocks.ui.workorders.packing

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
import com.bctags.bcstocks.databinding.ActivityNewPackBinding
import com.bctags.bcstocks.databinding.ActivityPackingBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.CarrierResponse
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.FiltersRequest
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.NewPack
import com.bctags.bcstocks.model.Packages
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.WorkOrder
import com.bctags.bcstocks.ui.workorders.packing.adapter.NewPackAdapter
import com.bctags.bcstocks.ui.workorders.packing.adapter.PackAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class NewPackActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityNewPackBinding
    private lateinit var adapter: NewPackAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    private val dropDown = DropDown()


    val SERVER_ERROR = "Server error, try later"
    var workOrdersPref:String= "[{}]"
    private val client: ClientData = ClientData(0,"","","","","","","","","","","","","")
    private val branch: Branch = Branch(0,0,"","","","","","","","")
    private var workOrder: WorkOrderData? = WorkOrderData(0,0,"",0,0,0,"","","","","","","","","","","","","",0,"","","",client,branch,mutableListOf())
    private var partialId: Int=0
    private var workOrderId: Int = 0

    val mapBoxes: HashMap<String, String> = HashMap()
    var selectedBox=0
    var itemsList: MutableList<ItemBox> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPackBinding.inflate(layoutInflater)
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
        getBoxesList()
    }

    fun saveValuesToPack(itemBox: ItemBox){
        val existingItem = itemsList.find { it.itemId == itemBox.itemId }
        if (existingItem != null) {
            existingItem.quantity = itemBox.quantity
        } else {
            itemsList.add(itemBox)
        }
        Log.i("itemsList",itemsList.toString())
    }
    private fun getBoxesList() {
        val requestBody = FilterRequestPagination(Pagination(1,1000))
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getBoxesList(requestBody),
                onSuccess = { response ->
                    val boxResponse = response.data
                    var list: MutableList<String> = mutableListOf()
                    boxResponse?.forEach { i ->
                        list.add(i.name)
                        mapBoxes[i.name] = i.id.toString();
                    }
                    val autoComplete: AutoCompleteTextView = findViewById(R.id.boxesList)
                    dropDown.listArrange(
                        list,
                        autoComplete,
                        mapBoxes,
                        this@NewPackActivity,
                        ::updateBoxes
                    )
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
    private fun updateBoxes(id: String, text: String) {
        selectedBox = id.toInt()
    }
    private fun initRecyclerView(list: MutableList<ItemWorkOrder>) {
        var listPacked = mutableListOf<PackedData>()
        getPacked { packedList ->
            adapter = if (packedList != null) {
                NewPackAdapter(
                    list,
                    partialId,
                    packedList.toMutableList(),
                    onClickListener = { ItemBox -> saveValuesToPack(ItemBox) })
            } else {
                NewPackAdapter(
                    list,
                    partialId,
                    listPacked,
                    onClickListener = { ItemBox -> saveValuesToPack(ItemBox) })
            }
            binding.recyclerList.layoutManager = LinearLayoutManager(this)
            binding.recyclerList.adapter = adapter
        }
    }
    private fun getPacked(callback: (List<PackedData>?) -> Unit) {
        val listFilters= mutableListOf<Filter>()
        listFilters.add(Filter("fillOrderId","eq", mutableListOf(partialId.toString())))

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPacked(FiltersRequest(listFilters)),
                onSuccess = { response ->
                    val packedList = response.data
                    callback(packedList)
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
        binding.btnFinish.setOnClickListener {
            savePack()
        }
    }
    private fun savePack() {
        if (binding.etBoxesQuantity.text.toString().isEmpty() || selectedBox == 0) {
            Toast.makeText(
                applicationContext,
                "Please add a Type of package and a Quantity",
                Toast.LENGTH_LONG
            ).show()
        } else {
            itemsList.removeIf { it.quantity == 0 }

            if (itemsList.isNullOrEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please add quantities to pack",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val newPackage: MutableList<Packages> = mutableListOf()
                val totalBoxes = binding.etBoxesQuantity.text.toString()
                if (totalBoxes.isNotEmpty() && totalBoxes != "0") {
                    newPackage.add(
                        Packages(
                            selectedBox,
                            UUID.randomUUID().toString(),
                            totalBoxes.toInt(),
                            itemsList
                        )
                    )
                }
                val requestBody = NewPack(partialId, newPackage)
                Log.i("NEW-PACK", requestBody.toString())
                sendNewPack(requestBody)
            }
        }
    }
    private fun sendNewPack(newPack: NewPack) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.saveNewPackage(newPack),
                onSuccess = { response ->
                    Toast.makeText(applicationContext, "DATA SAVED", Toast.LENGTH_LONG).show()
                    goMainPacking()
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    private fun goMainPacking(){
        val intent = Intent(this, PackingActivity::class.java)
        intent.putExtra("WORK_ORDER_ID", workOrderId)
        intent.putExtra("PARTIAL_ID", partialId)
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
    private fun initUI() {
        binding.tvOrderNum.text = "Work order ${workOrder?.number}"
        binding.tvClient.text = workOrder?.Client?.name ?: ""
        binding.tvPoReference.text = workOrder?.poReference ?: ""
        binding.tvPo.text = workOrder?.po ?: ""
        binding.tvDeliveryDate.text = workOrder?.dateOrderPlaced ?: ""
        binding.tvCreateDate.text = workOrder?.let { utils.dateFormatter(it.createdAt) }
    }


}