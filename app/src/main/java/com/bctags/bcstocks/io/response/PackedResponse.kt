package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class PackedResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<PackedData>,
)
data class PackedData(
    @SerializedName("label") var label: String,
    @SerializedName("uuid") var uuid: String,
    @SerializedName("boxId") var boxId: Int,
    @SerializedName("boxName") var boxName: String,
    @SerializedName("boxQuantity") var boxQuantity: Int,
    @SerializedName("items") var items: MutableList<ItemPacked>,
    @SerializedName("createdAt") var createdAt:String,
    @SerializedName("fillOrderId") var fillOrderId:Int,
    @SerializedName("shipId") var shipId:Int,
    @SerializedName("carrierName") var carrierName:String,
    @SerializedName("trackingNumber") var trackingNumber:String,
)
data class ItemPacked(
    @SerializedName("id") var id:Int,
    @SerializedName("quantity") var quantity:Int,
    @SerializedName("itemId") var itemId:Int,
)



