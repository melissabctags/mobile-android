package com.bctags.bcstocks.ui.transfer.adapter

import android.annotation.SuppressLint
import android.text.InputFilter
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemLocationBinding
import com.bctags.bcstocks.databinding.RvNewTransferBinding
import com.bctags.bcstocks.databinding.RvTransferLocationBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.TempInventoryData
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class NewTransferViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvNewTransferBinding.bind(view)
       @SuppressLint("SetTextI18n")
       fun render(item: TempInventoryData,onClickListener: (TempInventoryData,Int) -> Unit,position:Int) {
           Log.i("item",item.toString())
           binding.tvLocation.text = item.inventory.Item.item + "\n" + item.inventory.Item.description
           binding.tvQuantity.text = item.quantity.toString()


           binding.btnDelete.setOnClickListener {
               onClickListener(item,position)
           }
    }


}