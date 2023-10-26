package com.bctags.bcstocks.model

data class SettingsRequest(
    var fillOrderId: Int,
    var id: Int,
    var carrierId: Int,
    var carrier: String,
    var trackingNumber: String
)

data class Frequency(
    var frequencyId:Int,
    var frequency: String
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