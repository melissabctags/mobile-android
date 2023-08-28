package com.bctags.bcstocks.io

import android.util.Log
import android.widget.Toast
import com.bctags.bcstocks.io.response.LocationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCall {

    fun <T> performApiCall(call: Call<T>, onSuccess: (response: T) -> Unit, onError: (error: String) -> Unit ) {
        call.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("API Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<T>, t: Throwable) {
                onError("Network Error: ${t.message}")
            }
        })
    }

//    apiCall.performApiCall(
//    apiClient.getLocationsList(requestBody),
//    onSuccess = { response ->
//        val locationResponse: LocationResponse? = response
//        locationResponse?.list?.forEach { i ->
//            locationList.add(i.name + " " + i.Branch.name)
//            mapLocation[i.name + " " + i.Branch.name] = i.id.toString();
//        }
//    },
//    onError = { error ->
//        Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
//    }
//    )

//    CoroutineScope(Dispatchers.IO).launch {
//        val call = apiClient.getPurchaseOrder(poRequestBody)
//        call.enqueue(object : Callback<PurchaseOrderResponse> {
//            override fun onResponse(call: Call<PurchaseOrderResponse>,response: Response<PurchaseOrderResponse>) {
//                if (response.isSuccessful) {
//                    val purchaseOrderResponse: PurchaseOrderResponse? = response.body()
//                    var list: MutableList<String> = mutableListOf()
//                    purchaseOrderResponse?.list?.forEach { po ->
//                        list.add(po.number)
//                        mapPurchaseOrders[po.number] = po.id.toString();
//                    }
//                    val autoComplete: AutoCompleteTextView = findViewById(R.id.purchaseOrderList)
//                    dropDown.listArrange(list,autoComplete,mapPurchaseOrders,this@NewReceiveActivity,::updatePo)
//                } else {
//                    Toast.makeText(applicationContext, SERVER_ERROR,Toast.LENGTH_SHORT).show()
//                }
//            }
//            override fun onFailure(call: Call<PurchaseOrderResponse>, t: Throwable) {
//                Toast.makeText(applicationContext,SERVER_ERROR,Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

}