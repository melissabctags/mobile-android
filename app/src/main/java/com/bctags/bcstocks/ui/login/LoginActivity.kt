package com.bctags.bcstocks.ui.login


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.LoginResponse
import com.bctags.bcstocks.model.LoginRequest
import com.bctags.bcstocks.ui.MainMenuActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private val SERVER_ERROR_MESSAGE = "Server Error. Try Again."
    private val INCORRECT_CREDENTIALS = "You have entered an invalid username or password."
    private val EMPTY_CREDENTIALS = "Please enter a valid username & password."
    private val apiClient = ApiClient().apiService

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        val session = sharedPreferences.getBoolean("SESSION", false)
        val token = sharedPreferences.getString("TOKEN", "")
        if (session && token != "") {
            goToMenu()
        }
        initComponents()
        initListeners()
    }

    private fun initListeners() {
        btnLogin.setOnClickListener { performLogin() }
    }

    private fun initComponents() {
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
    }

    private fun goToMenu() {
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createSessionPreference(token: String) {
        Log.i("TOKEN", " $token")
        val sharedPreferences = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("TOKEN", token).apply()
        sharedPreferences.edit().putBoolean("SESSION", true).apply()
    }

    private fun performLogin() {
        val txUsername = findViewById<EditText>(R.id.etUsername).text.toString()
        val txPassword = findViewById<EditText>(R.id.etPassword).text.toString()
        if (txUsername != "" && txPassword != "") {
            val loginRequestBody = LoginRequest(txUsername, txPassword)

            CoroutineScope(Dispatchers.Main).launch {
                val call = apiClient.login(loginRequestBody)
                call.enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()

                            if (loginResponse == null) {
                                Toast.makeText(
                                    applicationContext,
                                    SERVER_ERROR_MESSAGE,
                                    Toast.LENGTH_SHORT
                                ).show()
                                return
                            }
                            if (loginResponse.success) {
                                createSessionPreference(loginResponse.loginData.token)
                                goToMenu()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    INCORRECT_CREDENTIALS,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                applicationContext,
                                SERVER_ERROR_MESSAGE,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            applicationContext,
                            SERVER_ERROR_MESSAGE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                })
            }
        } else {
            Toast.makeText(
                applicationContext,
                EMPTY_CREDENTIALS,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}