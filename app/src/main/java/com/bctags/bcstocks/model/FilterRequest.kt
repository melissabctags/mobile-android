package com.bctags.bcstocks.model
import com.google.gson.annotations.SerializedName

data class FilterRequest(
    var filters: MutableList<Filter>,
    var pagination: Pagination,
)
data class FilterRequestPagination(
    var pagination: Pagination,
)
data class Pagination(
    var current: Int,
    var pageSize: Int
)
data class Filter(
    var key: String,
    var op: String,
    var value: MutableList<String>,
)

data class FiltersRequest(
    var filters: MutableList<Filter>,
)

data class TempPagination(
    var currentPage:Int = 1,
    var prevPage:Int  = 0,
    var nextPage:Int  = 2,
    var totalPages:Int  = 0,
    var totalRecords:Int  = 0,
    var pageSize: Int=5
)