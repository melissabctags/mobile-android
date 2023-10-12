package com.bctags.bcstocks.ui.transfer.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemBranchBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class ItemBranchViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = RvItemBranchBinding.bind(view)

    fun render(
        item: InventoryData,
        onClickListener: (ItemBox) -> Unit,
        onClickListenerScan: (InventoryData,Int) -> Unit,position:Int
    ) {
        binding.tvLocation.text = buildString {
            append(item.Branch.name)
            append("\n")
            append(item.Location.name)
        }
        binding.tvQuantity.text = item.quantity.toString()
        binding.etSelectedQty.setText("0")
        binding.etSelectedQty.filters =
            arrayOf<InputFilter>(InputFilterMinMax("0", item.quantity.toString()))

        binding.etSelectedQty.addTextChangedListener {
            var newQuantity = binding.etSelectedQty.text.toString()
            if (newQuantity.isEmpty()) {
                newQuantity = "0"
            }
            onClickListener(ItemBox(item.id, newQuantity.toInt()))
        }

        binding.btnAction.setOnClickListener {
            onClickListenerScan(item,position)
        }
    }


}