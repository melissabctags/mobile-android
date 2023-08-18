package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class BranchData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
)