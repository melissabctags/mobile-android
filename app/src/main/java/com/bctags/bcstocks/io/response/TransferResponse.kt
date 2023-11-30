package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class TransferResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<TransferData>,
    @SerializedName("pagination") var pagination:PaginationResponse
)
data class TransferData(
    @SerializedName("id") var id: Int,
    @SerializedName("destinationBranchName") var destinationBranchName: String,
    @SerializedName("status") var status: String,
    @SerializedName("number") var number: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("createdByName") var createdByName: String,
)
data class TransferOrderResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: TransferOrderData,
)
data class TransferOrderData(
    @SerializedName("id") var id: Int,
    @SerializedName("number") var number: String,
    @SerializedName("status") var status: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("destinationBranchName") var destinationBranchName: String,
    @SerializedName("createdByName") var createdByName: String,
    @SerializedName("items") var items: MutableList<TransferOrderItemData>,
)
data class TransferOrderItemData(
    @SerializedName("itemId") var itemId: Int,
    @SerializedName("itemNumber") var itemNumber: String,
    @SerializedName("itemDescription") var itemDescription:String,
    @SerializedName("locationName") var locationName:String,
    @SerializedName("itemUpc") var itemUpc:String,
    @SerializedName("quantity") var quantity:Int,
)

data class TransferOrderItemExtra(
    @SerializedName("itemId") var itemId:Int,
    @SerializedName("itemNumber") var itemNumber: String,
    @SerializedName("itemDescription") var itemDescription:String,
    @SerializedName("locationName") var locationName:String,
    @SerializedName("quantity") var quantity:Int,
    @SerializedName("itemUpc") var itemUpc:String,
    @SerializedName("scanned") var scanned:Int,
)