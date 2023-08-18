package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class GeneralResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: String
)
