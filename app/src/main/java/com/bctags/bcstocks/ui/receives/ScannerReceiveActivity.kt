package com.bctags.bcstocks.ui.receives


import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityScannerReceiveBinding
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.BranchData
import com.bctags.bcstocks.io.response.PurchaseOrderData
import com.bctags.bcstocks.io.response.SupplierData
import com.bctags.bcstocks.model.ItemsNewReceiveTempo
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.EPCTools
import com.bctags.bcstocks.ui.receives.adapter.ItemsReceiveAdapter
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

    var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance();
    var isInventory: Boolean = false;

    var newReceive: ReceiveNew = ReceiveNew(0, 0, "",  mutableListOf())
    var purchaseOrder: PurchaseOrderData =  PurchaseOrderData(0,"",0,0,"","","", BranchData(0,""),mutableListOf(),SupplierData(0,""))
    val DURACION: Long = 2500;
    var hashUpcs: HashMap<String, Int> = HashMap()
    var epcsList: MutableList<String> = mutableListOf()
    var receiveItemsList: MutableList<ItemsNewReceiveTempo> = mutableListOf()

    private lateinit var adapter: ItemsReceiveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerReceiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()

        val gson = Gson()
        newReceive = gson.fromJson(intent.getStringExtra("RECEIVE"), ReceiveNew::class.java)
        purchaseOrder = gson.fromJson(intent.getStringExtra("PURCHASE_ORDER"), PurchaseOrderData::class.java)

        scannerGif()
        initItemsList()
        initRecyclerView()
        readTag()
    }

    private fun initItemsList() {
        (purchaseOrder.ItemsPo).forEach{
            var itemReceiving = ItemsNewReceiveTempo(it.Item.id,0,it.quantity,0,it.Item.description,it.Item.upc,it.receivedQuantity,0)
            receiveItemsList.add(itemReceiving)
        }
    }

    private fun readTag() {
        var result: Boolean = rfid.init();
        if (!result) {
            //connect fail
            Log.i("NO PRENDIO","NO PRENDIO")
            stopInventory()
        }
        if (rfid.startInventoryTag()) {
            Log.i("PRENDIO","PRENDIO")
            isInventory = true
            tagsReader()
        } else {
            stopInventory()
        }
    }
    private var start:Long = 0


    private fun tagsReader() {
        start = System.currentTimeMillis()
        Log.i("TIMER-----",(start).toString())
        CoroutineScope(Dispatchers.Default ).launch {
            while (isInventory) {
                var uhftagInfo: UHFTAGInfo? = rfid.readTagFromBuffer() ;
                if (uhftagInfo != null) {
                    epcsList.add(uhftagInfo.epc.toString())
                    //checkEpcList(uhftagInfo.epc.toString())
                } else {
                    delay(300)
                }
            }
        }
    }

    private fun checkEpcList(epc: String?) {
        //Log.i("EPC",epc.toString())
        if(epcsList.isEmpty()){
            epcsList.add(epc!!)
        }else{
            if(!epcsList.contains(epc)){
                epcsList.add(epc!!)
            }
        }
    }

    private fun stopInventory() {
        isInventory = false
        rfid.stopInventory()
        rfid.free()

        epcsList = epcsList.distinct() as MutableList<String>

        filterEpcs()
    }
    val tools= EPCTools()

//    val sgtin = Sgtin198()e

    private fun filterEpcs() {
        epcsList.forEach { i ->
//            val sgtin:SGTIN = Builder().withRFIDTag(i).build().sgtin;
//            val prefix = sgtin.companyPrefix.toString()
//            val reference = sgtin.itemReference.toString()//
//            val upc=prefix+reference

           // val prefix =  tools.parseHexString(i);

            val upc =tools.getGTIN(i).toString()

//            val sgtin198 = Sgtin198.fromEpc(i)
//            val prefix = Sgtin.getCompanyPrefixDigits(sgtin198.partition.toInt()).toString()
//            val reference = Sgtin.getItemReferenceDigits(sgtin198.partition.toInt()).toString()//
//            val upc=prefix+reference

            Log.i("EPC",i)
            Log.i("UPC",upc)
            if(hashUpcs.isEmpty() || !hashUpcs.containsKey(upc)){
                hashUpcs[upc] = 1
            }else{
                hashUpcs[upc] = hashUpcs[upc]!! + 1
            }
        }
        Log.i("LISTA",receiveItemsList.toString())
        receiveItemsList.forEach{
            if(hashUpcs.containsKey(it.upc)){
                it.quantity= hashUpcs[it.upc]?.toInt() ?: 0
            }
        }
        initRecyclerView()
        Log.i("TIMER---",(System.currentTimeMillis() - start).toString())
    }

    private fun initRecyclerView(){
        adapter = ItemsReceiveAdapter(
            itemsList = receiveItemsList,
            onclickListener = { itemsNewReceiveTempo -> onItemSelected(itemsNewReceiveTempo) }
        )
        binding.recyclerItemsReceived.layoutManager = LinearLayoutManager(this)
        binding.recyclerItemsReceived.adapter = adapter
    }


    private fun onItemSelected(item: ItemsNewReceiveTempo){
        var dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_receive_item)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvItemDescription:TextView = dialog.findViewById(R.id.tvItemDescription)
        val etQuantity:EditText = dialog.findViewById(R.id.etQuantity)
        val btnStatus:MaterialButton = dialog.findViewById(R.id.btnStatus)

        if(item.quantity==0){
            checkButtonReceiveItemMaterialButton(btnStatus,"#ffe600",R.color.black,R.drawable.ic_exclamation)
        }else{
            if((item.receivedQuantity+item.quantity)>=item.orderQuantity){
                checkButtonReceiveItemMaterialButton(btnStatus,"#20c95e",R.color.white,R.drawable.ic_done)
            }else{
                checkButtonReceiveItemMaterialButton(btnStatus,"#ff7700",R.color.black,R.drawable.ic_exclamation)
            }
        }
        tvItemDescription.text = item.description
        etQuantity.setText(item.quantity.toString())

        var btnSaveChange:MaterialButton = dialog.findViewById(R.id.btnSaveChange)
        btnSaveChange.setOnClickListener{
            updateItemList(item,dialog)
        }

        dialog.show()
    }

    private fun updateItemList(item:ItemsNewReceiveTempo,dialog: Dialog) {
        val etQuantity:EditText = dialog.findViewById(R.id.etQuantity)
        var newValue = 0
        if(!etQuantity.text.toString().isNullOrEmpty()){
            newValue=etQuantity.text.toString().toInt()
        }
        receiveItemsList.forEach{
            if(it.itemId==item.itemId){
                it.quantity=newValue
            }
        }
        adapter.notifyItemChanged(item.position)
        dialog.hide()
    }


    private fun checkButtonReceiveItemMaterialButton(btn: MaterialButton, colorTint:String, color:Int, iconDrawable: Int){
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorTint))
        btn.setIconResource(iconDrawable)
        btn.setIconTintResource(color)
    }

    private fun initListeners() {
        binding.tvStopScanning.setOnClickListener {
            stopInventory()
        }
        binding.ivGoBack.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun scannerGif() {
        val logo = findViewById<ImageView>(R.id.ivScanning)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Glide.with(this).asGif().load(R.drawable.scan_gif).into(logo)
        @Suppress("DEPRECATION")
        Handler().postDelayed(Runnable {
            binding.cvScanning.setVisibility(View.GONE);
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