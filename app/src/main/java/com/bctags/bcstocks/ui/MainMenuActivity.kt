package com.bctags.bcstocks.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.bctags.bcstocks.databinding.ActivityMainMenuBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.UserResponse
import com.bctags.bcstocks.ui.inventory.InventoryActivity
import com.bctags.bcstocks.ui.locations.LocationsActivity
import com.bctags.bcstocks.ui.orders.OrdersActivity
import com.bctags.bcstocks.ui.receives.NewReceiveActivity
import com.bctags.bcstocks.ui.transfer.TransferActivity
import com.bctags.bcstocks.util.DrawerBaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainMenuActivity : DrawerBaseActivity() {
    private val apiClient = ApiClient().apiService
    private lateinit var binding: ActivityMainMenuBinding

    val SERVER_ERROR = "Server error, try later"
    val apiCall = ApiCall()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        getCurrentUser()
    }

    private fun getCurrentUser() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getCurrentUser(),
                onSuccess = { response ->
                    val userResponse: UserResponse? = response
                    val name = "Welcome " + userResponse?.userData?.firstName + " " + userResponse?.userData?.lastName
                    binding.tvWelcome.text = name
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initListeners() {
        binding.cvReceives.setOnClickListener {
            val intent = Intent(this, NewReceiveActivity::class.java)
            startActivity(intent)
        }
        binding.cvOrders.setOnClickListener {
            val intent = Intent(this, OrdersActivity::class.java)
            startActivity(intent)
        }
        binding.cvInventory.setOnClickListener {
            val intent = Intent(this, InventoryActivity::class.java)
            startActivity(intent)
        }
        binding.cvTransfer.setOnClickListener {
            val intent = Intent(this, TransferActivity::class.java)
            startActivity(intent)
        }
        binding.cvChangeLocation.setOnClickListener {
            val intent = Intent(this, LocationsActivity::class.java)
            startActivity(intent)
        }

    }


}