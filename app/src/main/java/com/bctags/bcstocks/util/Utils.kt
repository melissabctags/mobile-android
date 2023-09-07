package com.bctags.bcstocks.util

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.Toast
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.PartialSetStatus
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.model.WorkOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Utils {
    private val apiClient = ApiClient().apiService
    val apiCall = ApiCall()
    fun dateFormatter(dateToFormat:String):String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val output = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val d = sdf.parse(dateToFormat)
        return output.format(d)
    }
    fun ínitPagination(totals: Int,pagination: TempPagination):TempPagination {
        pagination.currentPage = 1
        pagination.totalRecords = totals
        pagination.prevPage = 0
        if (totals < pagination.pageSize) {
            pagination.totalPages = 1
            pagination.nextPage = 0
        } else {
            if (totals % pagination.pageSize == 1) {
                pagination.totalPages = (totals / pagination.pageSize) + 1
            } else {
                pagination.totalPages = totals / pagination.pageSize
            }
            if (pagination.totalPages > 1) {
                pagination.nextPage = 2
            }
        }
        return pagination
    }
    fun prevPagination(pagination: TempPagination):TempPagination  {
        if (pagination.prevPage != 0) {
            --pagination.prevPage
            --pagination.currentPage
            --pagination.nextPage

        }
        return pagination
    }
    fun NextPagination(pagination: TempPagination):TempPagination {
        if (pagination.nextPage != 0) {
            ++pagination.prevPage
            ++pagination.currentPage
            ++pagination.nextPage

            if (pagination.nextPage > pagination.totalPages) {
                pagination.nextPage = 0
            }
        }
        return pagination
    }

    fun convertDate(input: Int): String? {
        return if (input >= 10) {
            input.toString()
        } else {
            "0$input"
        }
    }

    fun getChangeStatus(partialId: Int,status:String,applicationContext: Context) {
        val requestBody = PartialSetStatus(partialId,status)
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.changePartialStatus(requestBody),
                onSuccess = { response ->
                    if(response.success){
                        Toast.makeText(applicationContext, "Data updated.", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(applicationContext, "SERVER ERROR", Toast.LENGTH_LONG).show()
                    }
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, "SERVER ERROR", Toast.LENGTH_LONG).show()
                }
            )
        }
    }











}