package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName


class PurchaseOrderResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var list: MutableList<PurchaseOrderData>
)

data class PurchaseOrderData(
    @SerializedName("id") var id: Int,
    @SerializedName("number") var number: String,
    @SerializedName("supplierId") var supplierId: Int,
    @SerializedName("branchId") var branchId: Int,
    @SerializedName("comments") var comments: String,
    @SerializedName("status") var status: String,
    @SerializedName("updatedAt") var updatedAt: String,
    @SerializedName("Branch") var Branch: BranchData,
    @SerializedName("Items") var ItemsPo: MutableList<ItemsPo>,
    @SerializedName("Supplier") var Supplier: SupplierData,
)

data class ItemsPo(
    @SerializedName("id") var id: Int,
    @SerializedName("itemId") var itemId: Int,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("receivedQuantity") var receivedQuantity: Int,
    @SerializedName("updatedAt") var updatedAt: String,
    @SerializedName("Item") var Item: ItemData,
)

data class BranchData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
//    @SerializedName("address") var address: String,
//    @SerializedName("zipCode") var zipCode: String,
//    @SerializedName("city") var city: String,
//    @SerializedName("state") var state: String,
//    @SerializedName("country") var country: String,
//    @SerializedName("updatedAt") var updatedAt: String,
)


