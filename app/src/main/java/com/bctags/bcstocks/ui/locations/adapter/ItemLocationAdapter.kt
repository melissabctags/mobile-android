package com.bctags.bcstocks.ui.locations.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.ItemBox


//adapter: se encarga de coger la info
class ItemLocationAdapter(val list: MutableList<InventoryData>, private val onClickListener:(ItemBox)->Unit) : RecyclerView.Adapter<ItemLocationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemLocationViewHolder(layoutInflater.inflate(R.layout.rv_item_location, parent, false))
    }
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: ItemLocationViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,onClickListener)
    }

}


