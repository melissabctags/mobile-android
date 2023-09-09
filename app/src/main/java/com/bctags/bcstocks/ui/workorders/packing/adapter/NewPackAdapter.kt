package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.io.response.PickedData
import com.bctags.bcstocks.io.response.PickedResponse
import com.bctags.bcstocks.model.Filter
import com.bctags.bcstocks.model.FiltersRequest
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.WorkOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//adapter: se encarga de coger la info
class NewPackAdapter(val list: List<ItemWorkOrder>, val partialId:Int, val packedList:MutableList<PackedData>, private val onClickListener:(ItemBox)->Unit): RecyclerView.Adapter<NewPackViewHolder>() {
    private val apiClient = ApiClient().apiService
    private val apiCall = ApiCall()
    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewPackViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NewPackViewHolder(layoutInflater.inflate(R.layout.rv_new_pack,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: NewPackViewHolder, position: Int) {
        val itemWorkOrder = list[position]
        val listFilters= mutableListOf<Filter>()
        listFilters.add(Filter("fillOrderId","eq", mutableListOf(partialId.toString())))
        listFilters.add(Filter("itemId","eq", mutableListOf(itemWorkOrder.Item.id.toString())))

        getPickedItem(listFilters) { pickedList ->
            val totalPickedQuantity = pickedList?.sumOf { it.quantity } ?: 0
            val totalPackedListQuantity = packedList.sumQuantityById(itemWorkOrder.id)

            onClickListener(ItemBox(itemWorkOrder.Item.id,(totalPickedQuantity-totalPackedListQuantity)))

            holder.render(itemWorkOrder, totalPickedQuantity, totalPackedListQuantity,onClickListener)
        }
    }
    fun List<PackedData>.sumQuantityById(idToMatch: Int): Int {
        return flatMap { it.items }
            .filter { it.id == idToMatch }
            .sumBy { it.quantity }
    }
    fun getPickedItem(listReq:MutableList<Filter>,callback: (List<PickedData>?) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            apiCall.performApiCall(
                apiClient.getPickedItem(FiltersRequest(listReq)),
                onSuccess = { response ->
                    val pickedList = response.data
                    callback(pickedList)
                },
                onError = { error ->
                    Log.i("ERROR", error)
                }
            )
        }

    }





}


