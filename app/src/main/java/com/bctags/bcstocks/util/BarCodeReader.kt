package com.bctags.bcstocks.util

import android.content.Context
import android.util.Log
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory

class BarCodeReader {
    var barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder

    fun close() {
        barcodeDecoder.close()
    }
    fun open(context: Context) {
        barcodeDecoder.open(context)
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                Log.i("BARCODE", barcodeEntity.barcodeData)
                stop()
            } else {
                Log.i("BARCODE", "FAILED")
            }
        }
    }
    fun start(context: Context) {
        open(context)
    }
    fun stop() {
        close()
        barcodeDecoder.stopScan()
    }

}