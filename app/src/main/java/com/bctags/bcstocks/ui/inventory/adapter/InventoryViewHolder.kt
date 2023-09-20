package com.bctags.bcstocks.ui.inventory.adapter

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvInventoryListItemBinding
import com.bctags.bcstocks.databinding.RvReceivesListBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.util.Utils

//ViewHolder:  se encarga de pintar las celdas

class InventoryViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvInventoryListItemBinding.bind(view)
       @SuppressLint("SetTextI18n")
       fun render(item: InventoryData, onClickListener: (InventoryData) -> Unit,){
        binding.tvItem.text= item.Item.item + "\n" + item.Item.description
        binding.tvTotal.text= item.quantity.toString()
        binding.tvLocation.text= item.Branch.name + "\n" + item.Location.name


        itemView.setOnClickListener{
            onClickListener(item)
        }
    }


}