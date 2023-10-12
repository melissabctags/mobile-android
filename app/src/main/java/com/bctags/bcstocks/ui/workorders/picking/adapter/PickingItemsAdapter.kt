package com.bctags.bcstocks.ui.workorders.picking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.PickedItem
import com.bctags.bcstocks.io.response.PickedWorkOrderData
import com.bctags.bcstocks.model.WorkOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//adapter: se encarga de coger la info
class PickingItemsAdapter(
    val list: List<ItemWorkOrder>,
    private val onClickListener: (ItemWorkOrder) -> Unit,
    val pickedItems: List<PickedItem>
) : RecyclerView.Adapter<PickingItemsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickingItemsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PickingItemsViewHolder(
            layoutInflater.inflate(
                R.layout.rv_item_picking,
                parent,
                false
            )
        )
    }



    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: PickingItemsViewHolder, position: Int) {
        val item = list[position]

        holder.render(item, onClickListener, pickedItems)
    }


}


