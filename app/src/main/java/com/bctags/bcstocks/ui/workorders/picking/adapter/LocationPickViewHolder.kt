package com.bctags.bcstocks.ui.workorders.picking.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemPickingBinding
import com.bctags.bcstocks.databinding.RvLocationPickingBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ItemWorkOrder

//ViewHolder:  se encarga de pintar las celdas

class LocationPickViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvLocationPickingBinding.bind(view)

    fun render(item: InventoryData, onClickListener: (InventoryData) -> Unit ) {
        val texto = item.Location.name + " - " + item.Branch.name
        binding.tvLocation.text= texto
        binding.tvTotal.text= item.quantity.toString()


       itemView.setOnClickListener {
            onClickListener(item)
        }

    }


}