package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class LocationsRequest(
    var username: String,
    var password: String,
)
data class ItemLocationsRequest(
    var inventoryId: Int,
    var locationIdDestination: Int,
    var quantity: Int,
)
