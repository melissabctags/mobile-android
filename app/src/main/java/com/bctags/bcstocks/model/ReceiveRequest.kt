package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class ReceiveRequest(
    val username: String,
    val password: String,
)

//  var itemReceiving = ItemsNewReceiveTempo(it.Item.id,0,it.quantity,0,it.Item.description,it.Item.upc,it.receivedQuantity,0)
data class ItemsNewReceiveTempo(
    var itemId: Int,
    var quantity: Int,
    var orderQuantity: Int,
    var total: Int,
    var description: String,
    var upc: String,
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