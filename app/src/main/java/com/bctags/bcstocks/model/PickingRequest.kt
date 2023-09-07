package com.bctags.bcstocks.model

data class PickingRequest(
     var items: MutableList<PickingItem>
)
data class PickingItem(
    var fillOrderId: Int,
    var itemId: Int,
    var inventoryId: Int,
    var quantity: Int,
)