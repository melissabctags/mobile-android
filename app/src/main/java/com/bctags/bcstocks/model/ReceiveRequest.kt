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
    var description: String,
    var upc: String,
    var receivedQuantity: Int,
    var position: Int,
    var locationId: Int,
)
data class ReceiveNew(
    var purchaseOrderId: Int,
    var carrierId: Int,
    var comments: String,
    var items: MutableList<ItemNewReceive>,
    var invoice:String,
    var orderType: String
)
data class ItemNewReceive(
    var itemId: Int,
    var quantity: Int,
    var locationId: Int,
)




data class ReceiveTempo(
    var id: Int,
    var number: String,
    var purchaseOrderId: Int,
    var purchaseOrder: String,
    var carrierId: Int,
    var carrier: String,
    var comments: String,
    var items: MutableList<ItemsNewReceiveTempo>,
    var invoice:String
)