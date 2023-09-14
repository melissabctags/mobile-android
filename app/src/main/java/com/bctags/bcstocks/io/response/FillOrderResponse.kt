package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class FillOrderResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<FillOrderData>,

)
data class FillOrderData(
    @SerializedName("id") var id: Int,
    @SerializedName("bcTagsClientId") var bcTagsClientId: Int,
    @SerializedName("createdBy") var createdBy: Int,
    @SerializedName("assignedTo") var assignedTo: Int,
    @SerializedName("workOrderId") var workOrderId: Int,
    @SerializedName("shipId") var shipId: Int,
    @SerializedName("status") var status: String,
    @SerializedName("number") var number: String,
    @SerializedName("createdAt") var createdAt: String,
)



