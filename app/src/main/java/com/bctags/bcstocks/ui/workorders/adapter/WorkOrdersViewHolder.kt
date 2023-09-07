package com.bctags.bcstocks.ui.workorders.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvWorkorderListBinding
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.ActionWorkOrder
import com.bctags.bcstocks.model.WorkOrderStatus

//ViewHolder:  se encarga de pintar las celdas
class WorkOrdersViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvWorkorderListBinding.bind(view)

    fun render(
        item: WorkOrderData,
        onclickListener: (WorkOrderData) -> Unit,
        onSecondClickListener: (ActionWorkOrder) -> Unit,
        partialId: Int,
        moduleName: String
    ) {
        binding.tvNumber.text = item.number
        binding.tvClient.text = item.Client.name
        binding.tvPoReference.text = item.poReference
        binding.tvPo.text = item.po
        binding.tvDate.text = item.dateOrderPlaced

        if (item.status == "created" || item.status == "in_progress") {
            binding.mbIcon.visibility = View.VISIBLE
        }

        if (moduleName.isEmpty()) {
            binding.btnAction.text = "Pick"
        } else {
            binding.btnAction.text = moduleName.uppercase()
        }

        binding.btnView.setOnClickListener {
            onclickListener(item)
        }

        val dataAction = ActionWorkOrder(item, WorkOrderStatus(item.id, moduleName, partialId))
        binding.btnAction.setOnClickListener {
            onSecondClickListener(dataAction)
        }
    }


}