package com.bctags.bcstocks.ui.transfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.TempInventoryData


//adapter: se encarga de coger la info
class TransferLocationAdapter(val list: MutableList<InventoryData>, private val onClickListener:(TempInventoryData)->Unit) : RecyclerView.Adapter<TransferLocationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferLocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TransferLocationViewHolder(layoutInflater.inflate(R.layout.rv_transfer_location, parent, false))
    }
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: TransferLocationViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,onClickListener)
    }

}


