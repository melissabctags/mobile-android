package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class ItemResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<ItemData>,
    @SerializedName("pagination") var pagination: PaginationResponse
)

data class ItemData(
    @SerializedName("id") var id: Int,
    @SerializedName("item") var item: String,
    @SerializedName("upc") var upc: String,
    @SerializedName("description") var description: String,
    @SerializedName("perishable") var perishable: Boolean,
    @SerializedName("categoryId") var categoryId: Int,
    @SerializedName("scanType") var scanType: Boolean,
    @SerializedName("supplierId") var supplierId: Int,
    @SerializedName("price") var price: Double,
    @SerializedName("unitMeasurementId") var unitMeasurementId: Int,
    @SerializedName("updatedAt") var updatedAt: String,
    //@SerializedName("UnitMeasurement") var unitMeasurement: UnitData,
)
