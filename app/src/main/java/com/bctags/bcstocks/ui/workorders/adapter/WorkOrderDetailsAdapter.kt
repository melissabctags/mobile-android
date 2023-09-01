package com.bctags.bcstocks.ui.workorders.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.ItemWorkOrder
import com.bctags.bcstocks.io.response.WorkOrderData

//adapter: se encarga de coger la info
class WorkOrderDetailsAdapter(val list: List<ItemWorkOrder>): RecyclerView.Adapter<WorkOrderDetailsViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrderDetailsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WorkOrderDetailsViewHolder(layoutInflater.inflate(R.layout.rv_simple_items_list,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: WorkOrderDetailsViewHolder, position: Int) {
        val item =list[position]
        holder.render(item,position)
    }


}


