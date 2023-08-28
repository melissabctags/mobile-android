package com.bctags.bcstocks.ui.workorders.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.WorkOrderData

//adapter: se encarga de coger la info
class WorkOrdersAdapter(val list: List<WorkOrderData>, private val onclickListener:(WorkOrderData)->Unit, private val onSecondClickListener:(WorkOrderData)->Unit): RecyclerView.Adapter<WorkOrdersViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrdersViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WorkOrdersViewHolder(layoutInflater.inflate(R.layout.rv_workorder_list,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: WorkOrdersViewHolder, position: Int) {
        val item =list[position]
        holder.render(item,onclickListener,onSecondClickListener)
    }


}


