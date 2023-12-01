package com.bctags.bcstocks.ui.transfer.adapter

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvTransferDetailsBinding
import com.bctags.bcstocks.io.response.TransferOrderItemData
import com.bctags.bcstocks.io.response.TransferOrderItemExtra

//ViewHolder:  se encarga de pintar las celdas

class TransferDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvTransferDetailsBinding.bind(view)
    fun render(item: TransferOrderItemExtra, onClickListener: (TransferOrderItemExtra) -> Unit, status:String) {
        binding.tvItemName.text = buildString {
            append(item.itemNumber)
            append("\n")
            append(item.itemDescription)
        }
        binding.tvQuantity.text = item.quantity.toString()
        binding.tvScanned.text = item.scanned.toString()
        if(status=="sent" || status=="sent" ){
            binding.tvScanned.visibility = View.GONE
        }
    }

}





