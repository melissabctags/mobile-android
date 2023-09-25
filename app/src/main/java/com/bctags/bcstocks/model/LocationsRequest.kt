package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class LocationsRequest(
    var username: String,
    var password: String,
)
data class locationChanges(
   var movements: MutableList<ItemLocationsRequest>
)
data class ItemLocationsRequest(
    var quantity: Int,
    var inventoryId: Int,
    var locationIdDestination: Int,
)

data class CountLocation(
    var id:Int,
    var name: String
)

