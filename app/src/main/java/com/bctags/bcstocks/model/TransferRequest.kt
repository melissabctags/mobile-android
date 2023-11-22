package com.bctags.bcstocks.model

import com.bctags.bcstocks.io.response.InventoryData

data class TransferRequest(
    var fillOrderId: Int,
)

data class TempInventoryData(
    var inventory: InventoryData,
    var quantity: Int
)

data class NewTransfer(
    var destinationBranchId:Int,
    var items: MutableList<TransferItemRequest>
)

data class TransferItemRequest(
    var inventoryId:Int,
    var quantity: Int
)





