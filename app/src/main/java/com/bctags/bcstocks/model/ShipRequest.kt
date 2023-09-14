package com.bctags.bcstocks.model

data class ShipRequest(
    var fillOrderId: Int,
    var id: Int,
    var carrierId: Int,
    var carrier: String,
    var trackingNumber: String
)
/*
*
*
*
*ship/update
*
* id (el shipId),carrierId,trackingNumber
*
*
*
*
*
*
*
* */