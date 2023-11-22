package com.bctags.bcstocks.ui.transfer.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvNewTransferBinding
import com.bctags.bcstocks.model.TempInventoryData

//ViewHolder:  se encarga de pintar las celdas

class NewTransferViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvNewTransferBinding.bind(view)

    fun render(
        item: TempInventoryData,
        onClickListener: (TempInventoryData, Int) -> Unit,
        position: Int
    ) {
        binding.tvLocation.text = buildString {
            append(item.inventory.Item.item)
            append("\n")
            append(item.inventory.Item.description)
        }
        binding.tvQuantity.text = item.quantity.toString()

        binding.btnDelete.setOnClickListener {
            onClickListener(item, position)
        }
    }


}