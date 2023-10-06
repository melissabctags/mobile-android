package com.bctags.bcstocks.ui.transfer.adapter

import android.annotation.SuppressLint
import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemBranchBinding
import com.bctags.bcstocks.databinding.RvItemLocationBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class ItemBranchViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvItemBranchBinding.bind(view)
       @SuppressLint("SetTextI18n")
       fun render(item: InventoryData, onClickListener: (ItemBox) -> Unit, onClickListenerScan: (InventoryData) -> Unit) {
           binding.tvLocation.text = item.Branch.name + "\n" + item.Location.name
           binding.tvQuantity.text = item.quantity.toString()
           binding.etSelectedQty.setText("0")
           binding.etSelectedQty.filters = arrayOf<InputFilter>(InputFilterMinMax("0",  item.quantity.toString()))

           binding.etSelectedQty.addTextChangedListener {
               var newQuantity = binding.etSelectedQty.text.toString()
               if (newQuantity.isEmpty()) {
                   newQuantity = "0"
               }
               onClickListener(ItemBox(item.id, newQuantity.toInt()))
           }

           binding.btnAction.setOnClickListener{

           }
    }


}