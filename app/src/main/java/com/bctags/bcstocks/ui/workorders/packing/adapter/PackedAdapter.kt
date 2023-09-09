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
class PackedAdapter(val list: List<PackedData>, val partialId:Int, val listItems:List<ItemWorkOrder>,private val onClickListener:(PackedData)->Unit): RecyclerView.Adapter<PackedViewHolder>() {
//    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        //val parentTitle: TextView = itemView.findViewById(R.id.parentTitle)
//        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.childRecyclerView)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PackedViewHolder(layoutInflater.inflate(R.layout.rv_packed,parent,false))
    }
    override fun getItemCount(): Int =list.size
    override fun onBindViewHolder(holder: PackedViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,partialId, onClickListener)
//       val childAdapter = ChildAdapter(item.items,listItems)
//        holder.childRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
//        holder.childRecyclerView.adapter = childAdapter
    }
}
class PackedItemAdapter(val list: List<ItemPacked>): RecyclerView.Adapter<PackedItemViewHolder>() {
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

//class ChildAdapter(private val childData: List<ItemPacked>, val listItems:List<ItemWorkOrder>): RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {
//    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvItem: TextView = itemView.findViewById(R.id.tvItem)
//        val tvItemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
//    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_packed_item, parent, false)
//        return ChildViewHolder(view)
//    }
//    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
//        //holder.tvItem.text = childData[position]
//        holder.tvItem.text = "pruebas"
//        holder.tvItemQuantity.text = "100"
//    }
//    override fun getItemCount(): Int = childData.size
//}


