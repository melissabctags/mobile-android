package com.bctags.bcstocks.ui.transfer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityTransferDetailsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.TransferOrderData
import com.bctags.bcstocks.io.response.TransferOrderItemExtra
import com.bctags.bcstocks.model.GetOne
import com.bctags.bcstocks.ui.MainMenuActivity
import com.bctags.bcstocks.ui.transfer.adapter.TransferDetailsAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class TransferDetailsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTransferDetailsBinding
    private lateinit var adapter: TransferDetailsAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson = Gson()
    private val dropDown = DropDown()
    private val utils = Utils()
    val tools = EPCTools()

    private var isScanning = false
    private var triggerEnabled = true
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private val epcsList: MutableList<String> = mutableListOf()
    private val upcsList: MutableList<String> = mutableListOf()


    private val SERVER_ERROR = "Server error, try later"

    private var transferOrderId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            transferOrderId = extras.getInt("TRANSFER_ID")
            getTransfer(transferOrderId)
        }
        initListeners()
    }

    private fun getTransfer(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getTransferOrder(GetOne(id)),
                onSuccess = { response ->
                    useTransfer(response.data)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


    private var itemsList: MutableList<TransferOrderItemExtra> = mutableListOf()
    private fun useTransfer(data: TransferOrderData) {
        binding.tvNumber.text = data.number
        binding.tvBranch.text = data.destinationBranchName
        binding.tvDate.text = utils.dateFormatter(data.createdAt)
        binding.tvStatus.text = data.status
        data.items.forEach { i ->
            itemsList.add(
                TransferOrderItemExtra(
                    i.itemId,
                    i.itemNumber,
                    i.itemDescription,
                    i.locationName,
                    i.quantity,
                    i.itemUpc,
                    0
                )
            )
        }
        if(data.status=="sent"||data.status=="received"){
            binding.btnScan.visibility = View.GONE
            binding.tvScanned.visibility = View.GONE
        }
        initRecyclerView(itemsList,data.status)
    }

    private fun initRecyclerView(items: MutableList<TransferOrderItemExtra>,status:String) {
        adapter = TransferDetailsAdapter(
            list = items,
            onClickListener = { TransferOrderItemExtra -> something(TransferOrderItemExtra) },
            status
        )
        binding.recyclerList.layoutManager = LinearLayoutManager(this)
        binding.recyclerList.adapter = adapter
    }

    private fun something(transferData: TransferOrderItemExtra) {

    }


    private fun initListeners() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnScan.setOnClickListener {
            initRead()
            binding.tvScanned.visibility = View.VISIBLE
        }
        binding.btnClose.setOnClickListener {
            approveTransferOrder()
        }
        binding.btnCancel.setOnClickListener {

        }
    }

    private fun approveTransferOrder() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.approveTransferOrder(GetOne(transferOrderId)),
                onSuccess = { response ->
                    messageDialog.showDialog(
                        this@TransferDetailsActivity,
                        R.layout.dialog_success,
                        "Saved",
                    ) { mainTransferOrders() }
                },
                onError = { error ->
                    Log.i("error",error.toString())
                    messageDialog.showDialog(
                        this@TransferDetailsActivity,
                        R.layout.dialog_error,
                        "An error occurred, try again later $error"
                    ) { }
                }
            )
        }
    }
    private fun mainTransferOrders() {
        val intent = Intent(this, TransferActivity::class.java)
        startActivity(intent)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294 && triggerEnabled) {
            initRead()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun initRead() {
        lifecycleScope.launch() {
            try {
                if (isScanning) {
                    stopInventory()
                } else {
                    //itemList.clear()
                    //initRecyclerView()
                    val btnText = "Stop"
                    binding.btnScan.text = btnText
                    lockScanButton()
                    isScanning = true
                    upcsList.clear()
                    readTag()
                }
            } catch (e: Exception) {
                Log.e("initRead ", "Error initRead: ${e.message}")
            } finally {

            }
        }
    }

    private fun lockScanButton() {
        binding.btnScan.isEnabled = false
        triggerEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnScan.isEnabled = true
            triggerEnabled = true
        }, 1000)
    }

    private fun readTag() {
        lifecycleScope.launch(newSingleThreadContext("readTagTagsReader")) {
            try {
                withContext(Dispatchers.IO) {
                    val result: Boolean = rfid.init()
                    if (!result) {
                        Log.i("DIDN'T WORK", "DIDN'T WORK")
                        rfid.stopInventory()
                        rfid.free()
                    } else {
                        if (rfid.startInventoryTag()) {
                            Log.i("WORKS", "WORKS")
                            isScanning = true
                            tagsReader()
                        } else {
                            rfid.stopInventory()
                            rfid.free()
                        }
                    }

                }
            } catch (e: Exception) {
                Log.e("readTag", "Error en la lectura de etiquetas: ${e.message}")
            } finally {

            }
        }
    }

    var totalTags = 0;
    private fun tagsReader() {
        lifecycleScope.launch(newSingleThreadContext("tagsReaderTagsReader")) {
            try {
                while (isScanning) {
                    val uhfTagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
                    if (uhfTagInfo != null) {
                        epcsList.add(uhfTagInfo.epc.toString())
                        Log.i("EPC", uhfTagInfo.epc.toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("tagsReader", "Error en la lectura de etiquetas: ${e.message}")
            } finally {

            }
        }
    }

    private fun stopInventory() {
        lifecycleScope.launch {
            try {
                isScanning = false
                rfid.stopInventory()
                rfid.free()
                val btnText = "Read tags"
                lockScanButton()
                binding.btnScan.text = btnText
                if (epcsList.isNotEmpty()) {
                    val list = epcsList.distinct() as MutableList<String>
                    Log.i("stopInventory", list.toString())
                    totalTags= epcsList.count()
                    getUpcs(list)
                } else {
                    messageDialog.showDialog(
                        this@TransferDetailsActivity,
                        R.layout.dialog_error,
                        "An error occurred.\nTry again."
                    ) { }
                }
            } catch (e: Exception) {
                Log.e("stopInventory", "Error: ${e.message}")
            } finally {

            }
        }
    }

    private var hashUpcs: MutableMap<String, Int> = mutableMapOf()
    private fun getUpcs(epcsList: MutableList<String>) {
        lifecycleScope.launch(newSingleThreadContext("getUpcsTagReader")) {
            epcsList.forEach { i ->
                try {
                    val upc = tools.getGTIN(i).toString()
                    hashUpcs[upc] = (hashUpcs[upc] ?: 0) + 1
                    upcsList.add(upc)
                } catch (e: Exception) {
                    Log.e("Error", "Error Scanner: ${e.message}")
                }
            }
            if (upcsList.isNotEmpty()) {
                val list = upcsList.distinct() as MutableList<String>
                Log.i("UPC", list.toString())
                Log.i("hashUpcs", hashUpcs.toString())
                Log.i("itemsList", itemsList.toString())
                setScannedTotals()
            }
        }
    }

    private fun setScannedTotals() {
        lifecycleScope.launch {

            itemsList.forEach { i ->
                Log.i("i", hashUpcs[i.itemUpc].toString())
                val newScanned: Int = hashUpcs[i.itemUpc]?.toInt() ?: 0
                i.scanned = newScanned
            }
            adapter.notifyDataSetChanged()
            val text:String = "Tags read: $totalTags"
            binding.tvTotalTags.text = text
            binding.btnCancel.visibility = View.VISIBLE

            val error = itemsList.any { it.scanned < it.quantity }

            if (error) {
                binding.btnClose.visibility = View.INVISIBLE
            } else {
                binding.btnClose.visibility = View.VISIBLE
            }

            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())

        }
    }


}