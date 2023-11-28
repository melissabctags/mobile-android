package com.bctags.bcstocks.ui.transfer.adapter

import android.annotation.SuppressLint
import android.text.InputFilter
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvItemLocationBinding
import com.bctags.bcstocks.databinding.RvNewTransferBinding
import com.bctags.bcstocks.databinding.RvTransferBinding
import com.bctags.bcstocks.databinding.RvTransferLocationBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.TransferData
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.model.TempInventoryData
import com.bctags.bcstocks.util.InputFilterMinMax
import com.bctags.bcstocks.util.Utils

//ViewHolder:  se encarga de pintar las celdas

class TransferViewHolder(view: View):RecyclerView.ViewHolder(view)  {
    private val utils = Utils()
    private val binding = RvTransferBinding.bind(view)

       fun render(item: TransferData, onClickListener: (TransferData) -> Unit) {
           binding.tvTransfer.text = item.number
           binding.tvBranch.text = item.destinationBranchName
           //binding.tvStatus.text = item.status
           binding.tvDate.text =  utils.dateFormatter(item.createdAt)

           binding.btnAction.setOnClickListener {
               onClickListener(item)
           }

    }





}