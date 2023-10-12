package com.bctags.bcstocks.ui.receives


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityScannerReceiveBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.BranchData
import com.bctags.bcstocks.io.response.LocationData
import com.bctags.bcstocks.io.response.PurchaseOrderData
import com.bctags.bcstocks.io.response.SupplierData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.ItemNewReceive
import com.bctags.bcstocks.model.ItemsNewReceiveTempo
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.ui.receives.adapter.ItemsReceiveAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext


class ScannerReceiveActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityScannerReceiveBinding
    private lateinit var adapter: ItemsReceiveAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val dropDown = DropDown()
    private val tools = EPCTools()
    private val messageDialog = MessageDialog()
    private val gson = Gson()

    private var newReceive: ReceiveNew = ReceiveNew(0, 0, "", mutableListOf(), "")
    private var purchaseOrder: PurchaseOrderData = PurchaseOrderData(
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
    val DURACION: Long = 2500;
    val SERVER_ERROR = "Server error, try later"

    private val mapLocation: HashMap<String, String> = HashMap()
    private var locationList: MutableList<String> = mutableListOf()
    private var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    private var branchId = 0
    private var isScanning = true
    private val epcsList: MutableList<String> = mutableListOf()
    private var receiveItemsList: MutableList<ItemsNewReceiveTempo> = mutableListOf()
    private var hashUpcs: MutableMap<String, Int> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)
        initListeners()

        newReceive = gson.fromJson(intent.getStringExtra("RECEIVE"), ReceiveNew::class.java)
        purchaseOrder =
            gson.fromJson(intent.getStringExtra("PURCHASE_ORDER"), PurchaseOrderData::class.java)
        getLocations()
        scannerGif()

        lifecycleScope.launch {
            initItemsList()
            initRecyclerView()
            readTag()
        }

    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 294) {
            initRead()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters: MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters, pag)

        lifecycleScope.launch(Dispatchers.IO) {
            apiCall.performApiCall(
                apiClient.getLocationsList(requestBody),
                onSuccess = { response ->
                    setLocations(response.list)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setLocations(locations: MutableList<LocationData>) {
        lifecycleScope.launch(Dispatchers.Main) {
            locations.forEach { i ->
                locationList.add(i.name + " " + i.Branch.name)
                mapLocation[i.name + " " + i.Branch.name] = i.id.toString();
            }
        }
    }

    private fun initItemsList() {
        lifecycleScope.launch(Dispatchers.Default) {
            (purchaseOrder.ItemsPo).forEach {
                val itemReceiving = ItemsNewReceiveTempo(
                    it.Item.id,
                    0,
                    it.quantity,
                    0,
                    it.Item.description,
                    it.Item.upc,
                    it.receivedQuantity,
                    0,
                    0
                )
                receiveItemsList.add(itemReceiving)
            }
        }
    }

    private fun initListeners() {
        binding.tvScan.setOnClickListener {
            initRead()
        }
        binding.llHeader.setOnClickListener {
            val intent = Intent(this, NewReceiveActivity::class.java)
            startActivity(intent)
        }
        binding.btnSaveReceive.setOnClickListener {
            saveNewReceive()
        }
    }
    private fun initRead(){
        lifecycleScope.launch {
            if (isScanning) {
                stopInventory()
            } else {
                val btnText = "Stop reading"
                binding.tvScan.text = btnText
                isScanning = true
                receiveItemsList.clear()
                hashUpcs.clear()
                initItemsList()
                initRecyclerView()
                readTag()
            }
        }
    }


    private fun readTag() {
        lifecycleScope.launch(newSingleThreadContext("readTagReceive")) {
            withContext(Dispatchers.IO) {
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DIDN'T WORK", "DIDN'T WORK")
                    rfid.stopInventory()
                    rfid.free()
                }else{
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
        }
    }

    private fun tagsReader() {
        lifecycleScope.launch(newSingleThreadContext("tagsReaderReceive")) {
            while (isScanning) {
                val uhfTagInfo: UHFTAGInfo? = rfid.readTagFromBuffer()
                if (uhfTagInfo != null) {
                    epcsList.add(uhfTagInfo.epc.toString())
                    Log.i("EPC", uhfTagInfo.epc.toString())
                }
            }
        }
    }

    private suspend fun stopInventory() {
        isScanning = false
        rfid.stopInventory()
        rfid.free()
        val btnText = "Scan"
        withContext(Dispatchers.Main) {
            binding.tvScan.text = btnText
            if (epcsList.isNotEmpty()) {
                val list = epcsList.distinct() as MutableList<String>
                countUpcs(list)
            } else {
                messageDialog.showDialog(
                    this@ScannerReceiveActivity,
                    R.layout.dialog_error,
                    "An error occurred.\nTry again."
                ) { }
            }
        }
    }

    private fun countUpcs(epcsList: MutableList<String>) {
        lifecycleScope.launch {
            epcsList.forEach { i ->
                val upc = tools.getGTIN(i).toString()
                if (hashUpcs.isEmpty() || !hashUpcs.containsKey(upc)) {
                    hashUpcs[upc] = 1
                } else {
                    hashUpcs[upc] = hashUpcs[upc]!! + 1
                }
            }
            checkReceivesUpcs()
        }
    }

    private fun checkReceivesUpcs() {
        lifecycleScope.launch {
            receiveItemsList.forEach {
                if (hashUpcs.containsKey(it.upc)) {
                    it.quantity = hashUpcs[it.upc]?.toInt() ?: 0
                }
            }
            initRecyclerView()
            binding.btnSaveReceive.visibility = View.VISIBLE
        }
    }

    private fun initRecyclerView() {
        adapter = ItemsReceiveAdapter(
            itemsList = receiveItemsList,
            onclickListener = { itemsNewReceiveTempo -> onItemSelected(itemsNewReceiveTempo) }
        )
        binding.recyclerItemsReceived.layoutManager = LinearLayoutManager(this)
        binding.recyclerItemsReceived.adapter = adapter
    }

    private fun onItemSelected(item: ItemsNewReceiveTempo) {
        //var dialog = Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_receive_item)

        val tfLocationList: AutoCompleteTextView = dialog.findViewById(R.id.tfLocationList)
        dropDown.listArrangeWithId(
            locationList,
            tfLocationList,
            mapLocation,
            this,
            item.itemId.toString(),
            ::updateLocation
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvItemDescription: TextView = dialog.findViewById(R.id.tvItemDescription)
        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)
        val btnStatus: MaterialButton = dialog.findViewById(R.id.btnStatus)
        val llStatus: LinearLayout = dialog.findViewById(R.id.llStatus)

        if (item.quantity == 0) {
            llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ffe600"))
            checkButtonReceiveItemMaterialButton(
                btnStatus,
                "#ffe600",
                R.color.black,
                R.drawable.ic_exclamation
            )
        } else {
            if ((item.receivedQuantity + item.quantity) >= item.orderQuantity) {
                llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#20c95e"))
                checkButtonReceiveItemMaterialButton(
                    btnStatus,
                    "#20c95e",
                    R.color.white,
                    R.drawable.ic_done
                )
            } else {
                llStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#ff7700"))
                checkButtonReceiveItemMaterialButton(
                    btnStatus,
                    "#ff7700",
                    R.color.black,
                    R.drawable.ic_exclamation
                )
            }
        }
        tvItemDescription.text = item.description
        etQuantity.setText(item.quantity.toString())

        if (item.locationId != 0) {
            val key = mapLocation.entries.find { it.value == item.locationId.toString() }?.key
            tfLocationList.setText(key)
        }

        val btnSaveChange: MaterialButton = dialog.findViewById(R.id.btnSaveChange)
        btnSaveChange.setOnClickListener {
            updateItemList(item, dialog)
        }

        dialog.show()
    }

    private fun updateLocation(idSelect: String, text: String, identifierId: String) {
        receiveItemsList.forEach {
            if (it.itemId == identifierId.toInt()) {
                it.locationId = idSelect.toInt()
            }
        }
    }

    private fun updateItemList(item: ItemsNewReceiveTempo, dialog: Dialog) {
        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)
        var newValue = 0
        if (etQuantity.text.toString().isNotEmpty()) {
            newValue = etQuantity.text.toString().toInt()
        }
        receiveItemsList.forEach {
            if (it.itemId == item.itemId) {
                it.quantity = newValue
            }
        }
        adapter.notifyItemChanged(item.position)
        dialog.hide()
    }

    private fun checkButtonReceiveItemMaterialButton(
        btn: MaterialButton,
        colorTint: String,
        color: Int,
        iconDrawable: Int
    ) {
        //btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorTint))
        btn.setIconResource(iconDrawable)
        btn.setIconTintResource(color)
    }


    private fun saveNewReceive() {
        val newItems = mutableListOf<ItemNewReceive>()
        var emptyLocations = "\n"
        var empty = false
        for (it in receiveItemsList) {
            if (it.quantity != 0) {
                if (it.locationId != 0) {
                    newItems.add(ItemNewReceive(it.itemId, it.quantity, it.locationId))
                } else {
                    emptyLocations = emptyLocations + " " + it.description + "\n"
                    empty = true
                }
            }
        }
        if (empty) {
            messageDialog.showDialog(
                this@ScannerReceiveActivity,
                R.layout.dialog_error,
                "Select a location for $emptyLocations"
            ) { }
        } else {
            receiveItemsList.removeAll { it.quantity == 0 }
            if (receiveItemsList.isNotEmpty()) {
                newReceive.items = newItems
                sendNewReceive()
            } else {
                messageDialog.showDialog(
                    this@ScannerReceiveActivity,
                    R.layout.dialog_error,
                    "Please input quantities"
                ) { }
            }
        }
    }

    private fun sendNewReceive() {
        CoroutineScope(Dispatchers.Main).launch {
            apiCall.performApiCall(
                apiClient.createReceive(newReceive),
                onSuccess = { response ->
                    messageDialog.showDialog(
                        this@ScannerReceiveActivity,
                        R.layout.dialog_success,
                        ""
                    ) { goMainReceive() }
                },
                onError = { error ->
                    messageDialog.showDialog(
                        this@ScannerReceiveActivity,
                        R.layout.dialog_error,
                        error
                    ) { }
                }
            )
        }
    }

    private fun goMainReceive() {
        val intent = Intent(this, NewReceiveActivity::class.java)
        startActivity(intent)
    }

    private fun scannerGif() {
        val logo = findViewById<ImageView>(R.id.ivScanning)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Glide.with(this).asGif().load(R.drawable.scan_gif).into(logo)
        @Suppress("DEPRECATION")
        Handler().postDelayed(Runnable {
            binding.cvScanning.visibility = View.GONE;
        }, DURACION)
    }

}

/*
public class TestParseSGTINWithRfidTag {
//val people = intent.getSerializableExtra("purchaseOrderId") as Int?
    @Test
    public void whenRfidTagIsNotGS1_thenParseExceptionIsRaised() {
        assertThrows(EPCParseException.class, () -> Builder().withRFIDTag("F45349FB11DF49FA935AB6FF").build());
    }

    @Test
    public void whenPartitionIsWrong_thenParseExceptionIsRaised() {
        assertThrows(EPCParseException.class, () -> Builder().withRFIDTag("303C83F1B7DD441678901234").build());
    }

    @Test
    public void parseEpcSerialTest() throws EPCParseException {
        SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();

        assertEquals("96511988276", sgtin.getSerial());
    }

    @Test
    public void parseEpcEanTest() throws EPCParseException {
        SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();

        assertEquals("141674018641", sgtin.getCompanyPrefix() + sgtin.getItemReference());
    }

    @Test
    public void parseEpcFilterTest() throws EPCParseException {
        SGTIN sgtin = Builder().withRFIDTag("3024698E2CB1005678901234").build().getSGTIN();

        assertEquals("1", sgtin.getFilterValue());
    }

    @Test
    public void parseEpcPartitionTest() throws EPCParseException {
        SGTIN sgtin = Builder().withRFIDTag("30285471BD5A0A5678901234").build().getSGTIN();

        assertEquals("2", sgtin.getPartitionValue());
    }

    @Test
    public void parseNonHexEpc() {
        assertThrows(IllegalArgumentException.class, () -> Builder().withRFIDTag("30285471BD5A0A56789G1234").build());
    }
}
 */