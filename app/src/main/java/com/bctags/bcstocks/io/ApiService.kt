package com.bctags.bcstocks.io

import com.bctags.bcstocks.io.response.CarrierResponse
import com.bctags.bcstocks.io.response.GeneralResponse
import com.bctags.bcstocks.io.response.LocationResponse
import com.bctags.bcstocks.io.response.LoginResponse
import com.bctags.bcstocks.io.response.PurchaseOrderResponse
import com.bctags.bcstocks.io.response.ReceiveResponse
import com.bctags.bcstocks.io.response.SupplierResponse
import com.bctags.bcstocks.io.response.UserResponse
import com.bctags.bcstocks.io.response.WorkOrderResponse
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.FilterRequestPagination
import com.bctags.bcstocks.model.LoginRequest
import com.bctags.bcstocks.model.ReceiveNew
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(value = "/auth/signin")//
    fun login(@Body requestBody: LoginRequest): Call<LoginResponse>

    @POST(value = "/auth/user")
    fun getCurrentUser(): Call<UserResponse>

    @POST(value = "/po/list")
    fun getPurchaseOrder(@Body requestBody: FilterRequest): Call<PurchaseOrderResponse>

    @POST(value = "/carrier/list")
    fun getCarrierList(): Call<CarrierResponse>

    @POST(value = "/location/list")
    fun getLocationsList(@Body requestBody: FilterRequestPagination): Call<LocationResponse>

    @POST(value = "/receipt/create")
    fun createReceive(@Body requestBody: ReceiveNew): Call<GeneralResponse>

    @POST(value = "/receipt/list")
    fun receiveList(@Body requestBody: FilterRequest): Call<ReceiveResponse>

    @POST(value = "/supplier/list")
    fun supplierList(): Call<SupplierResponse>

    @POST(value = "/workorder/list")
    fun workOrderList(@Body requestBody: FilterRequest): Call<WorkOrderResponse>






}