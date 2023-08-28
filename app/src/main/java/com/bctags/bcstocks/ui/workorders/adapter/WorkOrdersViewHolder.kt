package com.bctags.bcstocks.ui.workorders.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvWorkorderListBinding
import com.bctags.bcstocks.io.response.WorkOrderData

//ViewHolder:  se encarga de pintar las celdas

class WorkOrdersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvWorkorderListBinding.bind(view)

    fun render(
        item: WorkOrderData,
        onclickListener: (WorkOrderData) -> Unit,
        onSecondClickListener: (WorkOrderData) -> Unit
    ) {
        binding.tvNumber.text = item.number
        binding.tvClient.text = item.Client.name
        binding.tvPoReference.text = item.poReference
        binding.tvPo.text = item.po
        binding.tvDate.text = item.dateOrderPlaced

        if (item.status == "created" || item.status == "picking") {
            binding.mbIcon.visibility = View.VISIBLE
        }

        binding.btnView.setOnClickListener {
            onclickListener(item)
        }
        binding.btnPick.setOnClickListener {
            onSecondClickListener(item)
        }
    }


}