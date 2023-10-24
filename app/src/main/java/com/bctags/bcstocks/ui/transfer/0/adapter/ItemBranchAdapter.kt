package com.bctags.bcstocks.ui.transfer.`0`.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.ItemBox


//adapter: se encarga de coger la info
class ItemBranchAdapter(
    val list: MutableList<InventoryData>,
    private val onClickListener: (ItemBox) -> Unit,private val onClickListenerScan: (InventoryData,Int) -> Unit
) : RecyclerView.Adapter<ItemBranchViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemBranchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemBranchViewHolder(
            layoutInflater.inflate(
                R.layout.rv_item_branch,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: ItemBranchViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickListener,onClickListenerScan,position)
    }

}


