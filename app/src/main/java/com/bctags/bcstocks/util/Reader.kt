package com.bctags.bcstocks.util

import android.util.Log
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReaderRFID {

    var rfid: RFIDWithUHFUART = RFIDWithUHFUART.getInstance();

    val epcsList: MutableList<String> = mutableListOf()

    var isInventory: Boolean = false

    fun readTag() {
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

    fun tagsReader() {
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

    fun stopInventory(): MutableList<String> {
        Log.i("STOP", "STOP")
        rfid.stopInventory()
        rfid.free()
        Log.i("STOP2","STOP2")
        //epcsList = epcsList.distinct() as MutableList<String>
        Log.i("STOP3",epcsList.toString())
        return epcsList
    }


}