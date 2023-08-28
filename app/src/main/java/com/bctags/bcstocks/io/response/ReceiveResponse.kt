package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName



class ReceiveResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<ReceiveData>,
    @SerializedName("pagination") var pagination:PaginationResponse
)
data class PaginationResponse(
    var current: Int,
    var pageSize: Int,
    var totals: Int,
)
data class ReceiveData(
    @SerializedName("id") var id: Int,
    @SerializedName("number") var number: String,
    @SerializedName("invoice") var invoice: String,
    @SerializedName("comments") var comments: String,
    @SerializedName("Branch") var Branch: BranchData,
    @SerializedName("Carrier") var Carrier: Carrier,
    @SerializedName("PurchaseOrder") var purchaseOrder: PurchaseOrderRec,
    @SerializedName("receivingStatus") var receivingStatus: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("Items") var Items: MutableList<ItemReceive>,
)

data class ItemReceive(
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("Location") var location: LocationItemRec,
    @SerializedName("Item") var Item: ItemData,
)
//data class ItemRec(
//    @SerializedName("item") var item: String,
//    @SerializedName("description") var description: String,
//    @SerializedName("upc") var upc: String,
//)
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

