package com.bctags.bcstocks.model

data class BoxRequest(
    var id: Int
)
data class ItemBox(
    var itemId: Int,
    var quantity: Int
)
data class Packages(
    var boxId: Int,
    var uuid: String,
    var boxQuantity: Int,
    var items: MutableList<ItemBox>
)
data class NewPack(
    var fillOrderId: Int,
    var packages: MutableList<Packages>
)


