package com.bctags.bcstocks.util

import android.icu.text.SimpleDateFormat
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.CarrierResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Utils {

    fun dateFormatter(dateToFormat:String):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val output = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val d = sdf.parse(dateToFormat)
        return output.format(d)
    }

















}