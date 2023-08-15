package com.bctags.bcstocks.io

import android.content.Context
import android.content.SharedPreferences
import com.bctags.bcstocks.MyApp
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val authToken = retrieveAuthToken()
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $authToken")
            .build()
        return chain.proceed(request)
    }

    private fun retrieveAuthToken(): String {
        val sharedPreferences: SharedPreferences =
            MyApp.instance.applicationContext.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        return sharedPreferences.getString("TOKEN", "") ?: ""
    }

}