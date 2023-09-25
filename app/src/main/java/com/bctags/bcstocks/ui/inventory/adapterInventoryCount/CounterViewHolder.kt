package com.bctags.bcstocks.ui.inventory.adapterInventoryCount

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.RvCounterItemBinding
import com.bctags.bcstocks.io.response.InventoryCount
import com.google.android.material.button.MaterialButton

//ViewHolder:  se encarga de pintar las celdas

class CounterViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvCounterItemBinding.bind(view)
    fun render(item: InventoryCount, position: Int) {
        binding.tvItemDescription.text = buildString {
            append(item.Item.item)
            append("\n")
            append(item.Item.description)
            append("\n")
            append(item.Item.upc)
        }
        binding.tvQuantity.text = item.quantity.toString()
        binding.tvFounded.text = item.founded.toString()
        if(item.quantity==0){
            checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ffe600", R.color.black, R.drawable.ic_exclamation)
        }else{
            if(item.founded>=item.quantity){
                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#20c95e", R.color.white, R.drawable.ic_done)
            }else{
                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ff7700", R.color.black, R.drawable.ic_exclamation)
            }
        }

    }

    private fun checkButtonReceiveItemMaterialButton(btn: MaterialButton, colorTint:String, color:Int, iconDrawable: Int){
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorTint))
        btn.setIconResource(iconDrawable)
        btn.setIconTintResource(color)
    }


}
