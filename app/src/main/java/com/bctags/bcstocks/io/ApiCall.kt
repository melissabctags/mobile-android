package com.bctags.bcstocks.io

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiCall {

    suspend fun <T> makeApiCall(call: Call<T>, onSuccess: (T) -> Unit, onError: (Throwable) -> Unit) {
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                onSuccess(response.body()!!)
            } else {
                onError(Exception("API call failed with code ${response.code()}"))
            }
        } catch (e: Throwable) {
            onError(e)
        }
    }



}