package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName


class SupplierResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var list: MutableList<SupplierData>
)

class SupplierData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
//    @SerializedName("bankAccount") var bankAccount: String,
//    @SerializedName("contactName") var contactName: String,
//    @SerializedName("rfc") var rfc: String,
//    @SerializedName("updatedAt") var updatedAt: String,
)

