package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class ReceiveRequest(
    val username: String,
    val password: String,
)


data class ItemsNewReceiveTempo(
    var itemId: Int,
    var quantity: Int,
    var orderQuantity: Int,
    var total: Int,
    var upc: String,
    var description: String,
    var receivedQuantity: Int,
    var position: Int,
)

data class ReceiveNew(
    var purchaseOrderId: Int,
    var carrierId: Int,
    var comments: String,
    var items: MutableList<ItemNewReceive>,
)

data class ItemNewReceive(
    var itemId: Int,
    var quantity: Int,
    var idLocation: Int,
)