package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvNewPackBinding
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class NewPackViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvNewPackBinding.bind(view)
    fun render(
        item: ItemWorkOrder,
        totalPicked: Int,
        totalPacked: Int,
        onClickListener: (ItemBox) -> Unit
    ) {
        binding.tvItemDescription.text = item.Item.description
        binding.tvQuantity.text = (totalPicked - totalPacked).toString()
        binding.etToPack.setText((totalPicked - totalPacked).toString())
        binding.etToPack.filters =
            arrayOf<InputFilter>(InputFilterMinMax("0", (totalPicked - totalPacked).toString()))

        binding.etToPack.addTextChangedListener {
            var newQuantity = binding.etToPack.text.toString()
            if (newQuantity.isEmpty()) {
                newQuantity = "0"
            }
            onClickListener(ItemBox(item.Item.id, newQuantity.toInt()))
        }
    }


}