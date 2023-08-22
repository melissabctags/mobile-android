package com.bctags.bcstocks.ui.receives.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.RvItemReceiveBinding
import com.bctags.bcstocks.model.ItemsNewReceiveTempo
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors.getColorStateList

//ViewHolder:  se encarga de pintar las celdas

class ItemsReceiveViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvItemReceiveBinding.bind(view)

    fun render(item: ItemsNewReceiveTempo, onclickListener:(ItemsNewReceiveTempo)->Unit){
        binding.tvItemDescription.text= item.description
        binding.tvQuantity.text= item.quantity.toString()
        binding.tvOrderQuantity.text= item.orderQuantity.toString()

        if(item.quantity==0){
            checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ffe600",R.color.black,R.drawable.ic_exclamation)
        }else{
            if((item.receivedQuantity+item.quantity)>=item.orderQuantity){
                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#20c95e",R.color.white,R.drawable.ic_done)
            }else{
                checkButtonReceiveItemMaterialButton(binding.btnStatus,"#ff7700",R.color.black,R.drawable.ic_exclamation)
            }
        }

        itemView.setOnClickListener{
            onclickListener(item)
        }
    }

    private fun checkButtonReceiveItemMaterialButton(btn: MaterialButton, colorTint:String, color:Int, iconDrawable: Int){
        btn.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorTint))
        btn.setIconResource(iconDrawable)
        btn.setIconTintResource(color)
    }




}