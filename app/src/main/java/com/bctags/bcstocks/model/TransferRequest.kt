package com.bctags.bcstocks.model

import com.bctags.bcstocks.io.response.InventoryData

data class TransferRequest(
    var fillOrderId: Int,

)
data class TempInventoryData(
    var inventory: InventoryData,
    var quantity: Int
)






