package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemPacked
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
class PackedItemAdapter(val list: List<ItemPacked>, val partialId:Int, val listItems:List<ItemWorkOrder>): RecyclerView.Adapter<PackedItemViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackedItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PackedItemViewHolder(layoutInflater.inflate(R.layout.rv_packed_item,parent,false))
    }
    override fun getItemCount(): Int =list.size
    override fun onBindViewHolder(holder: PackedItemViewHolder, position: Int) {
        val item = list[position]

        holder.render(item)
    }
}


