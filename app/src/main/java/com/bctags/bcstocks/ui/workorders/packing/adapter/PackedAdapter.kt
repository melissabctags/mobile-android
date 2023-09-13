package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.bctags.bcstocks.util.Utils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//adapter: se encarga de coger la info
class PackedAdapter(val list: List<PackedData>, val partialId:Int, private val listItems:List<ItemWorkOrder>, private val onClickListener:(PackedData,Int)->Unit):  RecyclerView.Adapter<PackedAdapter.PackedViewHolder>() {
    val utils = Utils()
    inner class PackedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTypePackaging: TextView = itemView.findViewById(R.id.tvTypePackaging)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.childRecyclerView)
        val acIcon: ImageView = itemView.findViewById(R.id.acIcon)
        val llList: LinearLayout = itemView.findViewById(R.id.llList)
        val btnFinish: MaterialButton = itemView.findViewById(R.id.btnFinish)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_packed, parent, false)
        return PackedViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackedViewHolder, position: Int) {
        val item = list[position]
        holder.tvTypePackaging.text = item.boxName +"\n"+ item.label
        holder.tvQuantity.text = item.boxQuantity.toString()
        holder.tvCreatedAt.text = utils.dateFormatter(item.createdAt)

        holder.acIcon.setOnClickListener{
            expandCardView(holder.llList,holder.acIcon)
        }
        holder.btnFinish.setOnClickListener{
            onClickListener(item,position)
        }

        val childAdapter = ChildAdapter(item.items,listItems)
        holder.childRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = childAdapter
        }
    }
    private fun expandCardView(llList: LinearLayout,acIcon:ImageView) {
        if (llList.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(llList, AutoTransition())
            llList.visibility = View.GONE
            acIcon.setImageResource(R.drawable.ic_arrow_down_black)
        } else {
            TransitionManager.beginDelayedTransition(llList, AutoTransition())
            llList.visibility = View.VISIBLE
            acIcon.setImageResource(R.drawable.ic_arrow_up_black)
        }
    }
    override fun getItemCount(): Int = list.size
}

class ChildAdapter(private val list: MutableList<ItemPacked>, private val listItems:List<ItemWorkOrder>) :
    RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {
    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItem: TextView = itemView.findViewById(R.id.tvItem)
        val tvItemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_packed_item, parent, false)
        return ChildViewHolder(view)
    }
    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        val item  =  list[position]

        val foundEl = listItems.find{it.Item.id==item.itemId}
        if (foundEl != null) {
            holder.tvItem.text = foundEl.Item.item + "\n" + foundEl.Item.description
        }
        holder.tvItemQuantity.text = item.quantity.toString()
    }

    override fun getItemCount(): Int = list.size
}

