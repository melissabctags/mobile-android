package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class LoginResponse (
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var loginData:  token
)

class token (
    @SerializedName("token") var token: String
)