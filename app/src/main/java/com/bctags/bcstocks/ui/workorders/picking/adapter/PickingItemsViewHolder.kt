package com.bctags.bcstocks.ui.workorders.picking.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.RvItemPickingBinding
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PickedItem
import com.bctags.bcstocks.io.response.PickedWorkOrderData
import com.google.android.material.button.MaterialButton

class PickingItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvItemPickingBinding.bind(view)
    fun render(
        item: ItemWorkOrder,
        onclickListener: (ItemWorkOrder) -> Unit,
        pickedItems:List<PickedItem>
    ) {

        binding.tvItemDescription.text = item.Item.description
        binding.tvOrderQuantity.text = item.quantity.toString()

        val foundItem = pickedItems.find { it.itemId == item.Item.id }

        if (foundItem != null) {
            binding.tvQuantity.text = foundItem.quantity.toString()
            if (foundItem.quantity >= item.quantity) {
                checkButtonReceiveItemMaterialButton(
                    binding.btnStatus,
                    "#20c95e",
                    R.color.white,
                    R.drawable.ic_done
                )
            } else {
                checkButtonReceiveItemMaterialButton(
                    binding.btnStatus,
                    "#ff7700",
                    R.color.black,
                    R.drawable.ic_exclamation
                )
            }
        } else {
            binding.tvQuantity.text = "0"
            checkButtonReceiveItemMaterialButton(
                binding.btnStatus,
                "#ffe600",
                R.color.black,
                R.drawable.ic_exclamation
            )
        }
//        if(item.quantity==0){
//            checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ffe600", R.color.black, R.drawable.ic_exclamation)
//        }else{
//            if((item.receivedQuantity+item.quantity)>=item.orderQuantity){
//                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#20c95e", R.color.white, R.drawable.ic_done)
//            }else{
//                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ff7700", R.color.black, R.drawable.ic_exclamation)
//            }
//        }
        itemView.setOnClickListener {
            onclickListener(item)
        }
    }

    private fun checkButtonReceiveItemMaterialButton(
        btn: MaterialButton,
        colorTint: String,
        color: Int,
        iconDrawable: Int
    ) {
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorTint))
        btn.setIconResource(iconDrawable)
        btn.setIconTintResource(color)
    }



}