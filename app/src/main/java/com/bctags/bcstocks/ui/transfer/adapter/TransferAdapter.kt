package com.bctags.bcstocks.ui.transfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.TransferData
import com.bctags.bcstocks.model.TempInventoryData


//adapter: se encarga de coger la info
class TransferAdapter(
    val list: MutableList<TransferData>,
    private val onClickListener: (TransferData) -> Unit
) : RecyclerView.Adapter<TransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TransferViewHolder(
            layoutInflater.inflate(
                R.layout.rv_transfer,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: TransferViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickListener)
    }

}


