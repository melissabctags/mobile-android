package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName



class ReceiveResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var list: MutableList<ReceiveData>
)

data class ReceiveData(
    @SerializedName("id") var id: Int,
    @SerializedName("number") var number: String,
    @SerializedName("supplierId") var supplierId: Int,
    @SerializedName("branchId") var branchId: Int,
    @SerializedName("supplier") var supplier: SupplierResponse,
    @SerializedName("comments") var comments: String,
    @SerializedName("status") var status: String,
    @SerializedName("updatedAt") var updatedAt: String,
    @SerializedName("Branch") var Branch: BranchData,
    @SerializedName("Items") var Items: MutableList<ItemReceive>,
    @SerializedName("Carrier") var Carrier: CarrierData,
)

data class ItemReceive(
    @SerializedName("id") var id: Int,
    @SerializedName("itemId") var itemId: Int,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("locationId") var locationId: Int,
    @SerializedName("updatedAt") var updatedAt: String,
    @SerializedName("Item") var Item: ItemData,
    @SerializedName("Location") var Location: Location,
)
data class Location(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
)

