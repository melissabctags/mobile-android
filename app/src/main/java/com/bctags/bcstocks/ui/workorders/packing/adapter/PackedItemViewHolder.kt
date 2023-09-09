package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvNewPackBinding
import com.bctags.bcstocks.databinding.RvPackedBinding
import com.bctags.bcstocks.databinding.RvPackedItemBinding
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.ItemPacked
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class PackedItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvPackedItemBinding.bind(view)

    fun render(
        item: ItemPacked,
    ) {
        binding.tvItem.text = "Item child"
        binding.tvItemQuantity.text = "100000"
        //binding.childRecyclerView

    }


}