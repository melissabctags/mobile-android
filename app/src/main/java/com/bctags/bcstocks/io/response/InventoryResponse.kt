package com.bctags.bcstocks.io.response

import com.bctags.bcstocks.model.Pagination
import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<InventoryData>
)
data class InventoryResponsePagination(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<InventoryData>,
    @SerializedName("pagination") var pagination: PaginationResponse
)
data class InventoryData(
    @SerializedName("id") var id: Int,
    @SerializedName("branchId") var branchId: Int,
    @SerializedName("itemId") var itemId: Int,
    @SerializedName("locationId") var locationId: Int,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("notes") var notes: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("Branch") var Branch: BranchData,
    @SerializedName("Item") var Item: ItemData,
    @SerializedName("Location") var Location: LocationData,


)

