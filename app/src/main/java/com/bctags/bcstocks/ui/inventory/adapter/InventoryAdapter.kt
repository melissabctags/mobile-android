package com.bctags.bcstocks.ui.inventory.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.io.response.ReceiveData


//adapter: se encarga de coger la info
class InventoryAdapter(val list: MutableList<InventoryData>, private val onClickListener:(InventoryData)->Unit) : RecyclerView.Adapter<InventoryViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return InventoryViewHolder(layoutInflater.inflate(R.layout.rv_inventory_list_item, parent, false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int = list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,onClickListener)
    }




}


