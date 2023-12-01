package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName



class ReceiveResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<ReceiveData>,
    @SerializedName("pagination") var pagination:PaginationResponse
)
data class ReceiveData(
    @SerializedName("id") var id: Int,
    @SerializedName("number") var number: String,
    @SerializedName("invoice") var invoice: String,
    @SerializedName("status") var status: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("order") var order: String,
    @SerializedName("orderType") var orderType: String,
    @SerializedName("comments") var comments: String,
    @SerializedName("items") var items: MutableList<ItemReceive>,
//    @SerializedName("Branch") var Branch: BranchData,
//    @SerializedName("Carrier") var Carrier: Carrier,
//    @SerializedName("PurchaseOrder") var purchaseOrder: PurchaseOrderRec,
//    @SerializedName("receivingStatus") var receivingStatus: String,
//    @SerializedName("Items") var Items: MutableList<ItemReceive>,
)

data class ItemReceive(
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("item") var item: String,
    @SerializedName("upc") var upc: String,
    @SerializedName("description") var description: String,
)
data class PaginationResponse(
    var current: Int,
    var pageSize: Int,
    var totals: Int,
)
class ReceiveGetOneResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: ReceiveGetOneData,
)
data class ReceiveGetOneData(
    @SerializedName("id") var id: Int,
    @SerializedName("purchaseOrderId") var purchaseOrderId: Int,
    @SerializedName("number") var number: String,
    @SerializedName("invoice") var invoice: String,
    @SerializedName("comments") var comments: String,
    @SerializedName("orderType") var orderType: String,
    @SerializedName("Branch") var Branch: BranchData,
    @SerializedName("Carrier") var Carrier: Carrier,
    @SerializedName("PurchaseOrder") var purchaseOrder: PurchaseOrderRec,
    @SerializedName("receivingStatus") var receivingStatus: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("Items") var Items: MutableList<ItemGetOneReceive>,
)
data class ItemGetOneReceive(
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("Location") var location: LocationItemRec,
    @SerializedName("Item") var Item: ItemData,
)
data class LocationItemRec(
    @SerializedName("name") var name: String,
)
data class PurchaseOrderRec(
    @SerializedName("number") var number: String,
    @SerializedName("Supplier") var supplier: SupplierRec,
)
data class SupplierRec(
    @SerializedName("name") var name: String,
)

