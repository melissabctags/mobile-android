package com.bctags.bcstocks.ui.receives.adapter

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvReceivesListBinding
import com.bctags.bcstocks.io.response.ReceiveData
import com.bctags.bcstocks.util.Utils

//ViewHolder:  se encarga de pintar las celdas

class ReceivesViewHolder(view: View):RecyclerView.ViewHolder(view)  {

    private val binding = RvReceivesListBinding.bind(view)
    val utils= Utils()


    fun render(receiveData: ReceiveData, onclickListener:(ReceiveData)->Unit){
        binding.tvNumber.text= receiveData.number
        binding.tvCarrier.text= receiveData.Carrier.name
        binding.tvDate.text= utils.dateFormatter(receiveData.createdAt)

        itemView.setOnClickListener{
            onclickListener(receiveData)
        }
    }


}