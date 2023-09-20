package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class BranchData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
)
class BranchResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: List<Branch>,
    @SerializedName("pagination") var pagination:PaginationResponse,
)
data class Branch(
    @SerializedName("id") var id: Int,
    @SerializedName("bcTagsClientId") var bcTagsClientId: Int,
    @SerializedName("name") var name: String,
    @SerializedName("businessName") var businessName: String,
    @SerializedName("address") var address: String,
    @SerializedName("city") var city: String,
    @SerializedName("state") var state: String,
    @SerializedName("country") var country: String,
    @SerializedName("zipCode") var zipCode: String,
    @SerializedName("createdAt") var createdAt: String,
)

