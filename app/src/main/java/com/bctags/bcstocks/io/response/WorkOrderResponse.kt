package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class WorkOrderResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<WorkOrderData>,
    @SerializedName("pagination") var pagination:PaginationResponse
)
data class WorkOrderData(
    @SerializedName("id") var id: Int,
    @SerializedName("bcTagsClientId") var bcTagsClientId: Int,
    @SerializedName("number") var number: String,
    @SerializedName("clientId") var clientId: Int,
    @SerializedName("createdBy") var createdBy:Int,
    @SerializedName("assignedTo") var assignedTo:Int,
    @SerializedName("dateOrderPlaced") var dateOrderPlaced:String,
    @SerializedName("shipToName") var shipToName:String,
    @SerializedName("attn") var attn:String,
    @SerializedName("contactName") var contactName:String,
    @SerializedName("address") var address:String,
    @SerializedName("addressLine2") var addressLine2:String,
    @SerializedName("addressLine3") var addressLine3:String,
    @SerializedName("city") var city:String,
    @SerializedName("state") var state:String,
    @SerializedName("country") var country:String,
    @SerializedName("zipCode") var zipCode:String,
    @SerializedName("poReference") var poReference:String,
    @SerializedName("po") var po:String,
    @SerializedName("branchId") var branchId:Int,
    @SerializedName("comments") var comments:String,
    @SerializedName("status") var status:String,
    @SerializedName("createdAt") var createdAt:String,
    @SerializedName("Client") var Client:ClientData,
    @SerializedName("Branch") var Branch:Branch,
    @SerializedName("Items") var Items:MutableList<ItemWorkOrder>,
)
data class ItemWorkOrder(
    @SerializedName("id") var id:Int,
    @SerializedName("quantity") var quantity:Int,
    @SerializedName("customerDescription") var customerDescription:String,
    @SerializedName("createdAt") var createdAt:String,
    @SerializedName("Item") var Item:ItemData,
)