package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
)