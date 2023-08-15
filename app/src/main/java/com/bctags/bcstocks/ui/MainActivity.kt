package com.bctags.bcstocks.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bctags.bcstocks.R
import com.bctags.bcstocks.ui.login.LoginActivity
import com.bctags.bcstocks.util.PreferenceHelper
import com.bctags.bcstocks.util.PreferenceHelper.get
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions


class MainActivity : AppCompatActivity() {

    val DURACION: Long = 3000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //FULL SCREEN
        supportActionBar?.hide()
        @Suppress("DEPRECATION")
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val logo = findViewById<ImageView>(R.id.gifLogo)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        Glide.with(this).asGif().load(R.drawable.splash).into(logo)
        changeActivity()
    }

    private fun changeActivity() {
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        val session = sharedPreferences.getBoolean("SESSION", false)
        val token = sharedPreferences.getString("TOKEN", "")
        Log.i("SESSION","$session -- $token")

        if(session && token!=""){
            @Suppress("DEPRECATION")
            Handler().postDelayed(Runnable {
                val intent = Intent(this, MainMenuActivity::class.java)
                startActivity(intent)
            }, DURACION)
        }else{
            @Suppress("DEPRECATION")
            Handler().postDelayed(Runnable {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }, DURACION)
        }
    }





}