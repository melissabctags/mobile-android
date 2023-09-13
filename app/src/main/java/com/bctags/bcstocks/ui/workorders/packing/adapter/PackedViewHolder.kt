package com.bctags.bcstocks.ui.workorders.packing.adapter

import android.text.InputFilter
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvNewPackBinding
import com.bctags.bcstocks.databinding.RvPackedBinding
import com.bctags.bcstocks.io.response.ItemPacked
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PackedData
import com.bctags.bcstocks.model.ItemBox
import com.bctags.bcstocks.util.InputFilterMinMax

//ViewHolder:  se encarga de pintar las celdas

class PackedViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val binding = RvPackedBinding.bind(view)
   private lateinit var adapter : PackedItemAdapter

    fun render(
        item: PackedData,
        partialId:Int,
        onClickListener: (PackedData) -> Unit
    ) {
        binding.tvTypePackaging.text = "item"
        binding.tvQuantity.text = "100"

        //binding.childRecyclerView

    }


}