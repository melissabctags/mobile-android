package com.bctags.bcstocks.ui.receives

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.DatePicker
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.ActivityHistorialReceivesBinding
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.CarrierData
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.io.response.ReceiveResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.TempPagination
import com.bctags.bcstocks.ui.receives.adapter.ReceivesAdapter
import com.bctags.bcstocks.util.DrawerBaseActivity
import com.bctags.bcstocks.util.DropDown
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HistorialReceivesActivity : DrawerBaseActivity(), DatePickerDialog.OnDateSetListener {
    private lateinit var binding: ActivityHistorialReceivesBinding
    private lateinit var adapter: ReceivesAdapter
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    private val utils = Utils()
    private val dropDown = DropDown()
    val SERVER_ERROR = "Server error, try later"

    var mapCarriers: HashMap<String, String> = HashMap()
    var mapSuppliers: HashMap<String, String> = HashMap()
    var pagination = TempPagination(1, 0, 2, 0)
    var firstRound = true
    var filters = mutableListOf(Filter("", "", mutableListOf("")))
    var carrierId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialReceivesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCarrierList()
        initUI()
        getReceivesList()

    }

    var day = 0
    var month = 0
    var year = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year
        val newDate = "${savedYear}-${utils.convertDate(savedMonth)}-${utils.convertDate(savedDay)}"
        binding.tvDate.text = newDate
    }

    private fun getReceivesList() {
        val pag = Pagination(pagination.currentPage, pagination.pageSize)
        val requestBody = FilterRequest(filters, pag)
        Log.i("filters", filters.toString())
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.receiveList(requestBody),
                onSuccess = { response ->
                    useReceiveList(response)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useReceiveList(response: ReceiveResponse) {
        if (firstRound) {
            pagination = utils.ínitPagination(response.pagination.totals, pagination)
            firstRound = false
        }
        initRecyclerView(response.data)
    }

    private fun prevPagination() {
        if (pagination.prevPage != 0) {
            pagination = utils.prevPagination(pagination)
            binding.tvPage.text = buildString {
                append("Page ")
                append(pagination.currentPage.toString())
            }
            getReceivesList()
        }
    }

    private fun NextPagination() {
        if (pagination.nextPage != 0) {
            pagination = utils.NextPagination(pagination)
            binding.tvPage.text = buildString {
                append("Page ")
                append(pagination.currentPage.toString())
            }
            getReceivesList()
        }
    }

    private fun initRecyclerView(receiveList: MutableList<ReceiveData>) {
        Log.i("receiveList", receiveList.toString())
        adapter = ReceivesAdapter(
            receivesList = receiveList,
            onClickListener = { receiveData -> viewReceive(receiveData) },
        )
        binding.recyclerReceives.layoutManager = LinearLayoutManager(this)
        binding.recyclerReceives.adapter = adapter
    }

    fun viewReceive(receiveData: ReceiveData) {
        val intent = Intent(this, ReceiveDetailsActivity::class.java)
        val gson = Gson()
        intent.putExtra("RECEIVE", gson.toJson(receiveData))
        startActivity(intent)
    }

    private fun initUI() {
        binding.btnHistorialNew.setOnClickListener {
            toNewReceive()
        }
        binding.btnPrev.setOnClickListener {
            prevPagination()
        }
        binding.btnNext.setOnClickListener {
            NextPagination()
        }
        binding.llSearch.setOnClickListener {
            expandCardView()
        }
        binding.btnSearch.setOnClickListener {
            searchReceives()
        }
        binding.btnReset.setOnClickListener {
            resetForm()
        }
        binding.tvDate.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
        binding.llHeader.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun resetForm() {
        pagination = TempPagination(1, 0, 2, 0)
        binding.etNumber.text.clear()
        binding.carrierList.text.clear()
        carrierId = 0
        binding.tvDate.text = " "
        filters.clear()
        filters = mutableListOf(Filter("", "", mutableListOf("")))
        firstRound = true
        savedDay = 0
        savedMonth = 0
        savedYear = 0
        getReceivesList()
    }

    private fun searchReceives() {
        filters.clear()
        firstRound = true
        if (binding.etNumber.text.isNotEmpty()) {
            filters.add(
                Filter(
                    "number",
                    "substring",
                    mutableListOf(binding.etNumber.text.toString())
                )
            )
        }
        if (savedDay != 0 && savedMonth != 0 && savedYear != 0) {
            val fecha = binding.tvDate.text.toString()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = Calendar.getInstance().apply {
                time = dateFormat.parse(fecha)!!
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time

            val end = Calendar.getInstance().apply {
                time = dateFormat.parse(fecha)!!
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            val isoStringStart =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(start)

            val isoStringEnd = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                .apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(end)

            filters.add(Filter("createdAt", "between", mutableListOf(isoStringStart, isoStringEnd)))
        }
        if (carrierId != 0) {
            filters.add(Filter("carrierId", "eq", mutableListOf(carrierId.toString())))
        }
        if (filters.isNotEmpty()) {
            getReceivesList()
        } else {
            filters = mutableListOf(Filter("", "", mutableListOf("")))
            getReceivesList()
        }


    }

    private fun expandCardView() {
        if (binding.cvFormSearch.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.cvFormSearch.visibility = View.GONE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_down_black)
        } else {
            TransitionManager.beginDelayedTransition(binding.llBase, AutoTransition())
            binding.cvFormSearch.visibility = View.VISIBLE
            binding.acIcon.setImageResource(R.drawable.ic_arrow_up_black)
        }
    }

    private fun toNewReceive() {
        val intent = Intent(this, NewReceiveActivity::class.java)
        startActivity(intent)
    }

    private fun getCarrierList() {
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getCarrierList(),
                onSuccess = { response ->
                    useCarriers(response.list)
                },
                onError = { error ->
                    Toast.makeText(applicationContext, SERVER_ERROR, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun useCarriers(carrierResponse: MutableList<CarrierData>) {
        val list: MutableList<String> = mutableListOf()
        carrierResponse.forEach { i ->
            list.add(i.name)
            mapCarriers[i.name] = i.id.toString()
        }
        val autoComplete: AutoCompleteTextView = findViewById(R.id.carrierList)
        dropDown.listArrange(
            list,
            autoComplete,
            mapCarriers,
            this@HistorialReceivesActivity,
            ::updateCarriers
        )
    }

    private fun updateCarriers(id: String, text: String) {
        carrierId = id.toInt()
    }


}