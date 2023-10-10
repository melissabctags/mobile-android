package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class PickedResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<PickedData>,
)
data class PickedData(
    @SerializedName("id") var id: Int,
    @SerializedName("bcTagsClientId") var bcTagsClientId: Int,
    @SerializedName("pickedBy") var pickedBy: Int,
    @SerializedName("fillOrderId") var fillOrderId: Int,
    @SerializedName("itemId") var itemId: Int,
    @SerializedName("inventoryId") var inventoryId:Int,
    @SerializedName("quantity") var quantity:Int,
    @SerializedName("createdAt") var createdAt:String,
)

data class WorkOrderPickedResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: String,
)


