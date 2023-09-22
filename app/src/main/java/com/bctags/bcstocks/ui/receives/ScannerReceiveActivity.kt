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
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityScannerReceiveBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.BranchData
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.io.response.PurchaseOrderData
import com.bctags.bcstocks.io.response.SupplierData
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.ItemNewReceive
import com.bctags.bcstocks.model.ItemsNewReceiveTempo
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.ui.receives.adapter.ItemsReceiveAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.util.MessageDialog
import com.bctags.bcstocks.util.ReaderRFID
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ScannerReceiveActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityScannerReceiveBinding

    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()

    val dropDown = DropDown()
    val tools = EPCTools()
    val messageDialog = MessageDialog()
    val reader: ReaderRFID = ReaderRFID()

    var newReceive: ReceiveNew = ReceiveNew(0, 0, "", mutableListOf(), "")
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
    val DURACION: Long = 2500;

    val mapLocation: HashMap<String, String> = HashMap()
    var hashUpcs: MutableMap<String, Int> = mutableMapOf()
    var receiveItemsList: MutableList<ItemsNewReceiveTempo> = mutableListOf()

    private lateinit var adapter: ItemsReceiveAdapter
    val SERVER_ERROR = "Server error, try later"
    var locationList: MutableList<String> = mutableListOf()

    private var isScanning = true
    var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance()
    val epcsList: MutableList<String> = mutableListOf()
    var isInventory: Boolean = false
    private var branchId=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        branchId = sharedPreferences.getInt("BRANCH", 0)
        initListeners()

        val gson = Gson()
        newReceive = gson.fromJson(intent.getStringExtra("RECEIVE"), ReceiveNew::class.java)
        purchaseOrder =
            gson.fromJson(intent.getStringExtra("PURCHASE_ORDER"), PurchaseOrderData::class.java)
        getLocations()
        scannerGif()
        initItemsList()
        initRecyclerView()
        readTag()
    }

    private fun getLocations() {
        val pag = Pagination(1, 1000)
        val filters:MutableList<Filter> = mutableListOf()
        filters.add(Filter("branchId", "eq", mutableListOf(branchId.toString())))
        val requestBody = FilterRequest(filters,pag)

        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getLocationsList(requestBody),
                onSuccess = { response ->
                    val locationResponse: LocationResponse? = response
                    locationResponse?.list?.forEach { i ->
                        locationList.add(i.name + " " + i.Branch.name)
                        mapLocation[i.name + " " + i.Branch.name] = i.id.toString();
                    }
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initItemsList() {
        (purchaseOrder.ItemsPo).forEach {
            var itemReceiving = ItemsNewReceiveTempo(
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

    private fun readTag() {
        var result: Boolean = rfid.init();
        if (!result) {
            Log.i("DIDN'T WORK", "DIDN'T WORK")
            rfid.stopInventory()
            rfid.free()
        }
        if (rfid.startInventoryTag()) {
            Log.i("WORKS", "WORKS")
            isInventory = true
            tagsReader()
        } else {
            stopInventory()
        }
    }

    private fun tagsReader() {
        CoroutineScope(Dispatchers.Default).launch {
            while (isInventory) {
                val uhftagInfo: UHFTAGInfo? = rfid.readTagFromBuffer();
                Log.i("EPC", uhftagInfo.toString())
                if (uhftagInfo != null) {
                    epcsList.add(uhftagInfo.epc.toString())
                } else {
                    delay(300)
                }
            }
        }
    }

    private fun stopInventory() {
        isScanning = false
        isInventory = false
        rfid.stopInventory()
        rfid.free()
        val btnText = "Scan"
        binding.tvScan.text = btnText
        val list = epcsList.distinct() as MutableList<String>
        countUpcs(list)
        checkReceivesUpcs()
    }

    private fun countUpcs(epcsList: MutableList<String>) {
        epcsList.forEach { i ->
            val upc = tools.getGTIN(i).toString()
            if (hashUpcs.isEmpty() || !hashUpcs.containsKey(upc)) {
                hashUpcs[upc] = 1
            } else {
                hashUpcs[upc] = hashUpcs[upc]!! + 1
            }
        }
    }

    private fun checkReceivesUpcs() {
        receiveItemsList.forEach {
            if (hashUpcs.containsKey(it.upc)) {
                it.quantity = hashUpcs[it.upc]?.toInt() ?: 0
            }
        }
        initRecyclerView()
        binding.btnSaveReceive.visibility = View.VISIBLE;
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
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_receive_item)

        val autoComplete: AutoCompleteTextView = dialog.findViewById(R.id.tfLocationList)
        dropDown.listArrangeWithId(
            locationList,
            autoComplete,
            mapLocation,
            this,
            item.itemId.toString(),
            ::updateLocation
        )

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvItemDescription: TextView = dialog.findViewById(R.id.tvItemDescription)
        val etQuantity: EditText = dialog.findViewById(R.id.etQuantity)
        val btnStatus: MaterialButton = dialog.findViewById(R.id.btnStatus)
        val tfLocationList: AutoCompleteTextView = dialog.findViewById(R.id.tfLocationList)
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

        Log.i("onItemSelected", item.locationId.toString())
        if (item.locationId != null && item.locationId != 0) {
            val key = mapLocation.entries.find { it.value == item.locationId.toString() }?.key
            tfLocationList.setText(key)
        }

        var btnSaveChange: MaterialButton = dialog.findViewById(R.id.btnSaveChange)
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
        if (!etQuantity.text.toString().isNullOrEmpty()) {
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

    private fun initListeners() {
        binding.tvScan.setOnClickListener {
            if (isScanning) {
                stopInventory()
            } else {
                val btnText = "Stop"
                binding.tvScan.text = btnText
                isScanning = true
                receiveItemsList.clear()
                hashUpcs.clear()
                initItemsList()
                initRecyclerView()
                readTag()
            }
        }
        binding.ivGoBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSaveReceive.setOnClickListener {
            saveNewReceive()
        }
    }

    private fun saveNewReceive() {
        var newItems = mutableListOf<ItemNewReceive>()
        var emptyLocations = "\n"
        var empty = false
        for (it in receiveItemsList) {
            if (it.locationId != null && it.locationId != 0) {
                newItems.add(ItemNewReceive(it.itemId, it.quantity, it.locationId))
            } else {
                emptyLocations = emptyLocations + " " + it.description + "\n"
                empty = true
            }
        }
        if (empty) {
            messageDialog.showDialog(
                this@ScannerReceiveActivity,
                R.layout.dialog_error,
                "Select a location for $emptyLocations"
            ) { }
        } else {
            newReceive.items = newItems
            sendNewReceive()
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

    fun goMainReceive() {
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