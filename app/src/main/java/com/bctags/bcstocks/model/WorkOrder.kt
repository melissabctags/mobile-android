package com.bctags.bcstocks.model

import com.bctags.bcstocks.io.response.WorkOrderData

data class WorkOrder(
    var id:Int
)

data class WorkOrderNewPartial(
    var workOrderId:Int
)


data class WorkOrderStatus(
    var id:Int,
    var moduleName: String,
    var partialId:Int
)

data class ActionWorkOrder(
   var workOrder: WorkOrderData,
   var partialStatus:WorkOrderStatus
)