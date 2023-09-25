package com.bctags.bcstocks.ui.transfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityTransferBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.MessageDialog
import com.google.gson.Gson

class TransferActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityTransferBinding
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val messageDialog = MessageDialog()
    private val gson= Gson()
    private val dropDown= DropDown()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)
    }
}