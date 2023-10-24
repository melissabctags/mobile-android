package com.bctags.bcstocks.model

data class SettingsRequest(
    var fillOrderId: Int,
    var id: Int,
    var carrierId: Int,
    var carrier: String,
    var trackingNumber: String
)

data class Frecuency(
    var frecuencyId:Int,
    var frecuency: String
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