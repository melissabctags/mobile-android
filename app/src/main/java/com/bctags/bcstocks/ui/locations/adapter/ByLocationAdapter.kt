package com.bctags.bcstocks.ui.locations.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.ItemBox


//adapter: se encarga de coger la info
class ByLocationAdapter(val list: MutableList<InventoryData>, private val onClickListener:(ItemBox)->Unit) : RecyclerView.Adapter<ByLocationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ByLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ByLocationViewHolder(layoutInflater.inflate(R.layout.rv_item_location, parent, false))
    }
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: ByLocationViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,onClickListener)
    }

}


