package com.bctags.bcstocks.io.response

import com.google.gson.annotations.SerializedName

class LocationResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var list: MutableList<LocationData>
)

class LocationData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("Branch") var Branch: BranchData,
    //@SerializedName("barcode") var barcode: String,
)
class LocationBarcode(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("barcode") var barcode: String,
)



