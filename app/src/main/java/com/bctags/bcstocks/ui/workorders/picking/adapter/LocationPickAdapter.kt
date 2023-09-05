package com.bctags.bcstocks.ui.workorders.picking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryData

//adapter: se encarga de coger la info
class LocationPickAdapter(val list: List<InventoryData>, private val onClickListener:(InventoryData)->Unit): RecyclerView.Adapter<LocationPickViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationPickViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LocationPickViewHolder(layoutInflater.inflate(R.layout.rv_location_picking,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: LocationPickViewHolder, position: Int) {
        val item =list[position]
        holder.render(item,onClickListener)
    }


}


