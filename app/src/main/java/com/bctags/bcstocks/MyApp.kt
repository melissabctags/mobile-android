package com.bctags.bcstocks

import android.app.Application
import android.content.Context

class MyApp : Application() {

    companion object {
        lateinit var instance: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val appContext: Context
        get() = applicationContext
}
