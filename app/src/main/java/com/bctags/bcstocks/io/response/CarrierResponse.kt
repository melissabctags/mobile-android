package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class CarrierResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var list: MutableList<CarrierData>
)

class CarrierData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
)
class Carrier(
    @SerializedName("name") var name: String,
)

