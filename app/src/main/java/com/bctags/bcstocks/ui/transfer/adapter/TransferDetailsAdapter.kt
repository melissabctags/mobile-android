package com.bctags.bcstocks.ui.transfer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.TransferOrderItemData
import com.bctags.bcstocks.io.response.TransferOrderItemExtra
import com.bctags.bcstocks.model.TempInventoryData


//adapter: se encarga de coger la info
class TransferDetailsAdapter(
    val list: MutableList<TransferOrderItemExtra>,
    private val onClickListener: (TransferOrderItemExtra) -> Unit
) : RecyclerView.Adapter<TransferDetailsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferDetailsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TransferDetailsViewHolder(
            layoutInflater.inflate(
                R.layout.rv_transfer_details,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: TransferDetailsViewHolder, position: Int) {
        val item = list[position]
        holder.render(item, onClickListener)
    }

}


