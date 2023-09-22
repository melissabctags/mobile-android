package com.bctags.bcstocks.ui.simplereader.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvReceivesListBinding
import com.bctags.bcstocks.databinding.RvSimpleItemsListBinding
import com.bctags.bcstocks.databinding.RvSimpleReaderTagBinding
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.util.Utils

//ViewHolder:  se encarga de pintar las celdas

class TagReaderViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvSimpleReaderTagBinding.bind(view)
    fun render(item: ItemData){
        binding.tvName.text= item.item
        binding.tvDescription.text= item.description
        binding.tvUpc.text= item.upc

//        itemView.setOnClickListener{
//
//        }
    }


}