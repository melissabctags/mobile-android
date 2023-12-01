package com.bctags.bcstocks.ui.receives.adapter

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.databinding.RvReceivesListBinding
import com.bctags.bcstocks.databinding.RvSimpleItemsListBinding
import com.bctags.bcstocks.io.response.ItemGetOneReceive
import com.bctags.bcstocks.io.response.ItemReceive
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.util.Utils

//ViewHolder:  se encarga de pintar las celdas

class ReceiveDetailsViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvSimpleItemsListBinding.bind(view)
    val utils= Utils()


    fun render(itemReceive: ItemGetOneReceive, position:Int){
        binding.tvName.text = itemReceive.Item.item
        binding.tvDescription.text = itemReceive.Item.description
        binding.tvUnits.text = itemReceive.quantity.toString()
        binding.tvLocation.text = itemReceive.location.name

        if(position%2==1 ){
            val colorResId = R.color.light_gray
            val color = ContextCompat.getColor(itemView.context, colorResId)
            binding.llRow.setBackgroundColor(color)
        }
//        itemView.setOnClickListener{
//
//        }


    }


}