package com.bctags.bcstocks.model

data class PackedRequest(
     var items: MutableList<PickingItem>
)
data class PackageIds(
    var packageIds: List<Int>
)