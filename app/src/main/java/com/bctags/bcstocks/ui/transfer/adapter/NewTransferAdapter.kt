package com.bctags.bcstocks.ui.transfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.TempInventoryData


class NewTransferAdapter(
    val list: MutableList<TempInventoryData>,
    private val onClickListener: (TempInventoryData, Int) -> Unit
) : RecyclerView.Adapter<NewTransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewTransferViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return NewTransferViewHolder(
            layoutInflater.inflate(
                R.layout.rv_new_transfer,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: NewTransferViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickListener,position)
    }

}


