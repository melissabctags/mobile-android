package com.bctags.bcstocks.ui.workorders.picking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.ItemWorkOrder

//adapter: se encarga de coger la info
class PickingItemsAdapter(val list: List<ItemWorkOrder>, private val onClickListener:(ItemWorkOrder)->Unit): RecyclerView.Adapter<PickingItemsViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickingItemsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PickingItemsViewHolder(layoutInflater.inflate(R.layout.rv_item_picking,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: PickingItemsViewHolder, position: Int) {
        val item =list[position]
        //TODO NECESITO QUE LA LISTA TENGA EL TOTAL PICKEADO DE TODOS LOS PARCIALES PARA SABER LA CANTIDAD RESTANTE QUE PUEDO TOMAR
        holder.render(item,onClickListener)
    }




}


