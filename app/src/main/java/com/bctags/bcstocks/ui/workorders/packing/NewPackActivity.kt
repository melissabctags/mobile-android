package com.bctags.bcstocks.ui.workorders.packing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityNewPackBinding
import com.bctags.bcstocks.databinding.ActivityPackingBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.Branch
import com.bctags.bcstocks.io.response.ClientData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.Utils

class NewPackActivity : DrawerBaseActivity() {
    private lateinit var binding: ActivityNewPackBinding
    // private lateinit var adapter: PickingItemsAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()


    val SERVER_ERROR = "Server error, try later"
    var workOrdersPref:String= "[{}]"
    private val client: ClientData = ClientData(0,"","","","","","","","","","","","","")
    private val branch: Branch = Branch(0,0,"","","","","","","","")
    private var workOrder: WorkOrderData? = WorkOrderData(0,0,"",0,0,0,"","","","","","","","","","","","","",0,"","","",client,branch,mutableListOf())
    private var partialId: Int=0
    private var workOrderId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}