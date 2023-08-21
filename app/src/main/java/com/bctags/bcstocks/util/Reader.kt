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

    var epcsList: MutableList<String> = mutableListOf()

    var isInventory: Boolean = false

     fun readTag() {
        var result: Boolean = rfid.init();
        if (!result) {
            Log.i("DIDN'T WORK","DIDN'T WORK")
           // stopInventory()
        }
        if (rfid.startInventoryTag()) {
            Log.i("WORKS","WORKS")
            isInventory = true
            tagsReader()
        } else {
            stopInventory()
        }
    }

     fun tagsReader() {
        CoroutineScope(Dispatchers.Default ).launch {
            while (isInventory) {
                var uhftagInfo: UHFTAGInfo? = rfid.readTagFromBuffer() ;
                if (uhftagInfo != null) {
                    epcsList.add(uhftagInfo.epc.toString())
                } else {
                    delay(300)
                }
            }
        }
    }

     fun stopInventory() : MutableList<String> {
        rfid.stopInventory()
        rfid.free()
        epcsList = epcsList.distinct() as MutableList<String>
        //filterEpcs()
        return epcsList
    }



}