package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class UserResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var userData: UserData
)

data class UserData(
    @SerializedName("firstName") var firstName: String,
    @SerializedName("lastName") var lastName: String,
    @SerializedName("isRoot") var isRoot: Boolean,
    @SerializedName("branchId") var branchId: Int,
)
