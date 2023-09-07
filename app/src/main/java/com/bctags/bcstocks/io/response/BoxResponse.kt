package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName
class BoxResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<BoxData>,
    @SerializedName("pagination") var pagination: PaginationResponse
)
data class BoxData(
    @SerializedName("id") var id: Int,
    @SerializedName("bcTagsClientId") var bcTagsClientId: Int,
    @SerializedName("name") var name: String,
    @SerializedName("active") var active: Boolean,
    @SerializedName("createdAt") var createdAt: String,
)
