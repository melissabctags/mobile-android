package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class FilterRequest(
    var filterList: MutableList<Filter>,
    var pagination: Pagination,
)
data class FilterRequestPagination(
    var pagination: Pagination,
)
data class Pagination(
    //pagination: { current: page, pageSize }
    var current: Int,
    var pageSize: Int
)
data class Filter(
    var key: String,
    var op: String,
    var value: MutableList<String>,
)