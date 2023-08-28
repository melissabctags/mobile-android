package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

data class ClientResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<ClientData>,
)

data class ClientData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("bankAccount") var bankAccount: String,
    @SerializedName("rfc") var rfc: String,
    @SerializedName("email") var email: String,
    @SerializedName("contactName") var contactName: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("cellPhone") var cellPhone: String,
    @SerializedName("address") var address: String,
    @SerializedName("zipCode") var zipCode: String,
    @SerializedName("city") var city: String,
    @SerializedName("state") var state: String,
    @SerializedName("country") var country: String,
    @SerializedName("createdAt") var createdAt: String,
)
