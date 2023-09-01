package com.bctags.bcstocks.ui.workorders.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.RvSimpleItemsListBinding
import com.bctags.bcstocks.io.response.ItemWorkOrder


//ViewHolder:  se encarga de pintar las celdas
class WorkOrderDetailsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvSimpleItemsListBinding.bind(view)

    fun render(
        item: ItemWorkOrder,position:Int
    ) {
        binding.tvName.text = item.Item.item
        binding.tvDescription.text = item.Item.description
        binding.tvUnits.text = item.quantity.toString()
        binding.tvLocation.visibility = View.GONE


        if(position%2==1 ){
            val colorResId = R.color.light_gray
            val color = ContextCompat.getColor(itemView.context, colorResId)
            binding.llRow.setBackgroundColor(color)
        }
    }


}