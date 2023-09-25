package com.bctags.bcstocks.ui.inventory.adapterInventoryCount

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.InventoryCount
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.CountLocation


//adapter: se encarga de coger la info
class CounterAdapter(val list: MutableList<InventoryCount>) : RecyclerView.Adapter<CounterViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CounterViewHolder(layoutInflater.inflate(R.layout.rv_counter_item, parent, false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int = list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,position)
    }




}


