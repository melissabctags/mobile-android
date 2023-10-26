package com.bctags.bcstocks.ui.settings

import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivitySettingsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.model.Frequency
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.rscja.deviceapi.RFIDWithUHFUART
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext

class SettingsActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val messageDialog = MessageDialog()
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val dropDown = DropDown()

    private val modeList:List<Frequency> = listOf(Frequency(0x01, "China Standard(840~845MHz)"),
        Frequency(0x02,"China Standard(920~925MHz)"),
        Frequency(0x04,"ETSI Standard(865~868MHz)"),
        Frequency(0x08,"United States Standard(902~928MHz)"),
        Frequency(0x16,"Korea"),
        Frequency(0x32,"Japan"),
        Frequency(0x33,"South Africa(915~919MHz)"),
        Frequency(0x34,"New Zealand"),
        Frequency(0x80,"Morocco"),
        Frequency(0x08,"Fixed Frequency(915MHz)"))
    private var powerList: MutableList<String> = mutableListOf()
//    private var protocolList:MutableList<String> = mutableListOf("ISO 18000-6C","GB/T 29768","GJB 7377.1","ISO 18000-6B")
    private val linkList = listOf("DSB_ASK/FM0/40KHz","PR_ASK/Miller4/250KHz","PR_ASK/Miller4/300KHz","DSB_ASK/FM0/400KHz")

    private var mapMode: HashMap<String, String> = HashMap()
    private var mapProtocol: HashMap<String, String> = HashMap()
    private var mapPower: HashMap<String, String> = HashMap()
    private var mapLink: HashMap<String, String> = HashMap()

    private var selectedMode = Frequency(0,"")
    private var selectedPower=""
    private var selectedProtocol=""
    private var selectedLink=""

    private val rfid = RFIDWithUHFUART.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        setDropdownModes()
//        setDropdownProtocol()
        setDropdownPower()
        setDropdownLink()
    }

    private fun initUI() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSetFrequency.setOnClickListener {
            setFrequencyMode()
        }
        binding.btnGetFrequency.setOnClickListener {
            getFrequencyMode()
        }
        binding.btnSetPower.setOnClickListener {
           setPower()
        }
        binding.btnGetPower.setOnClickListener {
           getPower()
        }
        binding.btnSetRFLink.setOnClickListener {
           setRfLink()
        }
        binding.btnGetRFLink.setOnClickListener {
            getRfLink()
        }
//        binding.btnSetProtocol.setOnClickListener {
//           setProtocol()
//        }


    }


    private fun setDropdownModes() {
        val list: MutableList<String> = mutableListOf()
        modeList.forEach { i ->
            list.add(i.frequency)
            mapMode[i.frequency] = i.frequencyId.toString()
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.modeList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapMode,
            this@SettingsActivity,
            ::updateSelectedMode
        )
    }
    private fun updateSelectedMode(id: String, text: String){
        selectedMode.frequencyId = id.toInt()
        selectedMode.frequency = text
    }
//    private fun setDropdownProtocol() {
//        protocolList.forEach { i ->
//            mapProtocol[i] = i
//        }
//        val autoComplete: AutoCompleteTextView = findViewById(R.id.protocolList)
//        dropDown.listArrange(
//            protocolList,
//            autoComplete,
//            mapProtocol,
//            this@SettingsActivity,
//            ::updateSelectedProtocol
//        )
//    }
//    private fun updateSelectedProtocol(id: String, text: String){
//        selectedProtocol = id
//        Log.i("selectedProtocol",selectedProtocol)
//    }
//    private fun setProtocol() {
//        if (!selectedProtocol.isNullOrEmpty()) {
//            lifecycleScope.launch {
//                withContext(Dispatchers.IO) {
//                    val result: Boolean = rfid.init()
//                    if (!result) {
//                        rfid.free()
//                    } else {
//                        try {
//                            val index = protocolList.indexOfFirst { it == selectedProtocol }
//                            Log.i("index",index.toString())
//                            if (rfid.setProtocol(index)) {
//                                showSuccessMsg("Protocol saved")
//                            } else {
//                                showErrorMsg("An error occurred.\nTry again.")
//                            }
//                            rfid.free()
//                        } catch (e: Exception) {
//                            e.message?.let { Log.e("ERROR", it) }
//                            showErrorMsg("An error occurred.\nTry again.")
//                        }
//                    }
//                }
//            }
//        } else {
//            showErrorMsg("Must select a protocol.")
//        }
//    }


    private fun setDropdownPower() {
        for (i in 1..30) {
            powerList.add(i.toString())
            mapPower[i.toString()] = i.toString()
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.powerList)
        dropDown.listArrange(
            powerList,
            autoComplete,
            mapPower,
            this@SettingsActivity,
            ::updateSelectedPower
        )
    }
    private fun updateSelectedPower(id: String, text: String){
        selectedPower = id
    }
    private fun setPower() {
        if (selectedPower.toInt() in 1..30) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val result: Boolean = rfid.init()
                    if (!result) {
                        rfid.free()
                    } else {
                        try {
                            if (rfid.setPower(selectedPower.toInt())) {
                                showSuccessMsg("Power saved.")
                            } else {
                                showErrorMsg("An error occurred.\nTry again.")
                            }
                            rfid.free()
                        } catch (e: Exception) {
                            showErrorMsg("An error occurred.\nTry again.")
                            e.message?.let { Log.e("ERROR", it) }
                        }
                    }
                }
            }
        }else{
            showErrorMsg("Must select a number between 1 - 30.")
        }
    }
    private fun getPower() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DOESNT WORK", "DOESNT WORK")
                    rfid.free()
                }else{
                    Log.i("WORKS", "WORKS")
                    val power = rfid.getPower()
                    Log.i("GET", "$power")
                    if (power != -1) {
                        val index = powerList.indexOfFirst { it == power.toString() }
                        setPowerList(index)
                    } else {
                        Log.i("Falla", "getFrequencyMode mode=$power")
                    }
                    rfid.free()
                }
            }
        }
    }
    private fun setPowerList(index: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (index != -1) {
                    binding.powerList.setText(powerList[index])
                }
            }
        }
    }

    private fun setDropdownLink() {
        val list: MutableList<String> = mutableListOf()
        linkList.forEach { i ->
            list.add(i)
            mapLink[i] = i
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.linkList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapLink,
            this@SettingsActivity,
            ::updateSelectedLink
        )
    }
    private fun updateSelectedLink(id: String, text: String){
        selectedLink = id
    }
    private fun getRfLink() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DOESNT WORK", "DOESNT WORK")
                    rfid.free()
                }else{
                    Log.i("WORKS", "WORKS")
                    val link = rfid.getRFLink()
                    Log.i("GET", "$link")
                    if (link != -1) {
                        setLinkList(link)
                    } else {
                        Log.i("Falla", "getFrequencyMode mode=$link")
                    }
                    rfid.free()
                }
            }
        }
    }
    private fun setLinkList(index: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (index != -1) {
                    binding.linkList.setText(linkList[index])
                }
            }
        }
    }

    private fun setRfLink() {
        if (!selectedLink.isNullOrEmpty()) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val result: Boolean = rfid.init()
                    if (!result) {
                        rfid.free()
                    } else {
                        try {
                            val index = linkList.indexOfFirst { it == selectedLink }
                            Log.i("index",index.toString())
                            if (rfid.setRFLink(index)) {
                                showSuccessMsg("RFLink saved")
                            } else {
                                showErrorMsg("An error occurred.\nTry again.")
                            }
                            rfid.free()
                        } catch (e: Exception) {
                            e.message?.let { Log.e("ERROR", it) }
                            showErrorMsg("An error occurred.\nTry again.")
                        }
                    }
                }
            }
        } else {
            showErrorMsg("Must select a RFLink.")
        }
    }




    private fun setFrequencyMode() {
        if (selectedMode.frequencyId != 0) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val result: Boolean = rfid.init()
                    if (!result) {
                        rfid.free()
                    } else {
                        try {
                            if (rfid.setFrequencyMode(selectedMode.frequencyId)) {
                                showSuccessMsg("Frequency saved")
                            } else {
                                showErrorMsg("An error occurred.\nTry again.")
                            }
                            rfid.free()
                        } catch (e: Exception) {
                            e.message?.let { Log.e("ERROR", it) }
                            showErrorMsg("An error occurred.\nTry again.")
                        }
                    }
                }
            }
        } else {
            showErrorMsg("Must select a working mode.")
        }
    }
    private fun getFrequencyMode() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DOESNT WORK", "DOESNT WORK")
                    rfid.free()
                }else{
                    Log.i("WORKS", "WORKS")
                    val mode = rfid.getFrequencyMode()
                    Log.i("GET", "$mode")
                    if (mode != -1) {
                        val index = modeList.indexOfFirst { it.frequencyId == mode }
                        setFrequencyList(index)
                    } else {
                        Log.i("Falla", "getFrequencyMode mode=$mode")
                    }
                    rfid.free()
                }
            }
        }
    }
    private fun setFrequencyList(index: Int) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                if (index != -1) {
                    binding.modeList.setText(modeList[index].frequency)
                }
            }
        }
    }
    private fun showErrorMsg(message:String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                messageDialog.showDialog(
                    this@SettingsActivity,
                    R.layout.dialog_error,
                    message
                ) { }
            }
        }
    }
    private fun showSuccessMsg(message:String) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                messageDialog.showDialog(
                    this@SettingsActivity,
                    R.layout.dialog_success,
                    message
                ) { }
            }
        }
    }




}