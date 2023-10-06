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


}