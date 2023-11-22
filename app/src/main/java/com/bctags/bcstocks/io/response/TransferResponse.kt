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
    @SerializedName("destinationBranchId") var destinationBranchId: Int,
    @SerializedName("status") var status: String,
    @SerializedName("number") var number: String,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("items") var items: List<TransferItemData>,
    @SerializedName("Branch") var Branch: BranchData,
)
data class TransferItemData(
    @SerializedName("id") var id: Int,
    @SerializedName("Item") var Item:ItemData,
)


