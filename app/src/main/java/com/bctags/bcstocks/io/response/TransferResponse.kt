package com.bctags.bcstocks.io.response

import android.content.ClipData.Item
import com.google.gson.annotations.SerializedName

data class TransferResponse(
    @SerializedName("success") var success: Boolean,
    @SerializedName("data") var data: MutableList<WorkOrderData>,
    @SerializedName("pagination") var pagination:PaginationResponse
)


