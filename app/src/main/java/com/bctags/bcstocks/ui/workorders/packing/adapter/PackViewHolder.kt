package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemPickingBinding
import com.bctags.bcstocks.databinding.RvPickedItemBinding
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.google.android.material.button.MaterialButton

//ViewHolder:  se encarga de pintar las celdas

class PackViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvPickedItemBinding.bind(view)

    fun render(item: ItemWorkOrder, totalPicked:Int,totalPacked:Int ) {

        binding.tvItemDescription.text= item.Item.description
        binding.tvPicked.text= totalPicked.toString()
        binding.tvQuantityToPack.text= (totalPicked-totalPacked).toString()


//       itemView.setOnClickListener {
//
//        }

    }


}