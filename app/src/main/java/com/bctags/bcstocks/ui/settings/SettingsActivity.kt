package com.bctags.bcstocks.ui.settings

import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivitySettingsBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.model.Frecuency
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

    private val modeList:List<Frecuency> = listOf(Frecuency(0x01, "China Standard(840~845MHz)"),
        Frecuency(0x02,"China Standard(920~925MHz)"),
        Frecuency(0x04,"ETSI Standard(865~868MHz)"),
        Frecuency(0x08,"United States Standard(902~928MHz)"),
        Frecuency(0x16,"Korea"),
        Frecuency(0x32,"Japan"),
        Frecuency(0x33,"South Africa(915~919MHz)"),
        Frecuency(0x34,"New Zealand"),
        Frecuency(0x80,"Morocco"),
        Frecuency(0x08,"Fixed Frequency(915MHz)"))

    private val protocolList = listOf("ISO 18000-6C","GB/T 29768","GJB 7377.1","ISO 18000-6B")
    private val linkList = listOf("DSB_ASK/FM0/40KHz","PR_ASK/Miller4/250KHz","PR_ASK/Miller4/300KHz","DSB_ASK/FM0/400KHz")
//power list 5-24
    private var mapMode: HashMap<String, String> = HashMap()
    private var mapProtocol: HashMap<String, String> = HashMap()
    private var mapPower: HashMap<String, String> = HashMap()
    private var mapLink: HashMap<String, String> = HashMap()

    private var selectedMode = Frecuency(0,"")
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
        setDropdownProtocol()
        setDropdownPower()
        setDropdownLink()
    }

    private fun initUI() {
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnSetFrequency.setOnClickListener {
            setFrecuencyMode()
        }
        binding.btnGetFrequency.setOnClickListener {
            getFrecuencyMode()
        }
    }
    private fun setDropdownModes() {
        val list: MutableList<String> = mutableListOf()
        modeList.forEach { i ->
            list.add(i.frecuency)
            mapMode[i.frecuency] = i.frecuencyId.toString()
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
        selectedMode.frecuencyId = id.toInt()
        selectedMode.frecuency = text
        Log.d("selectedMode", selectedMode.toString())
    }
    private fun setDropdownProtocol() {
        val list: MutableList<String> = mutableListOf()
        protocolList.forEach { i ->
            list.add(i)
            mapProtocol[i] = i
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.protocolList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapProtocol,
            this@SettingsActivity,
            ::updateSelectedProtocol
        )
    }
    private fun updateSelectedProtocol(id: String, text: String){
        selectedProtocol = id
    }
    private fun setDropdownPower() {
        val list: MutableList<String> = mutableListOf()
        for (i in 5..24) {
            list.add(i.toString())
            mapPower[i.toString()] = i.toString()
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.powerList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapPower,
            this@SettingsActivity,
            ::updateSelectedPower
        )
    }
    private fun updateSelectedPower(id: String, text: String){
        selectedPower = id
    }
    private fun setDropdownLink() {
        val list: MutableList<String> = mutableListOf()
        linkList.forEach { i ->
            list.add(i)
            mapLink[i] = i
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.powerList)
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
    private fun setFrecuencyMode() {
        //val mode: Int = selectedMode.frecuencyId
//        val mode = getMode("")
//        Log.d("selectedMode", "setFrequencyMode mode=$mode")
//        if (rfid.setFrequencyMode(0x08.toByte().toInt())) {
//            Log.d("Exito", "setFrequencyMode mode=$mode")
//        } else {
//            Log.d("Falla", "setFrequencyMode mode=$mode")
//        }

        lifecycleScope.launch(newSingleThreadContext("readTagReceive")) {
            withContext(Dispatchers.IO) {
                val mode = getMode("")
                val result: Boolean = rfid.init()
                if (!result) {
                    Log.i("DIDN'T WORK", "DIDN'T WORK")
                    rfid.free()
                }else{
                    Log.i("WORKS", "WORKS")
                    if (rfid.setFrequencyMode(0x08)) {
                        Log.d("Exito", "setFrequencyMode mode=$mode")
                    } else {
                        Log.d("Falla", "setFrequencyMode mode=$mode")
                    }
                    rfid.free()
                }
            }
        }
    }
    private fun getMode(modeName: String): Int {
//        if (modeName == getString(R.string.China_Standard_840_845MHz)) {
//            return 0x01
//        } else if (modeName == getString(R.string.China_Standard_920_925MHz)) {
//            return 0x02
//        } else if (modeName == getString(R.string.ETSI_Standard)) {
//            return 0x04
//        } else if (modeName == getString(R.string.United_States_Standard)) {
//            return 0x08
//        } else if (modeName == getString(R.string.Korea)) {
//            return 0x16
//        } else if (modeName == getString(R.string.Japan)) {
//            return 0x32
//        } else if (modeName == getString(R.string.South_Africa_915_919MHz)) {
//            return 0x33
//        } else if (modeName == getString(R.string.New_Zealand)) {
//            return 0x34
//        } else if (modeName == getString(R.string.Morocco)) {
//            return 0x80
//        }
        return 0x08
    }
    private fun getFrecuencyMode() {
        val mode: Int = rfid.frequencyMode
        Log.e("getFrec", "getFrequencyMode()=$mode")
        if (mode != -1) {
            val foundFrecuency = modeList.find { it.frecuencyId == mode }
            selectedMode = foundFrecuency ?: Frecuency(0, "")
            binding.modeList.setText(foundFrecuency?.frecuency ?: "Elemento no encontrado")
            Log.e("getFrec", foundFrecuency.toString())
        } else {
            Log.d("Falla", "getFrequencyMode mode=$mode")
        }
    }











}