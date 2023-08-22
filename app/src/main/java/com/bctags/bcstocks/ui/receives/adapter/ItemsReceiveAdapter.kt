package com.bctags.bcstocks.ui.receives.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.model.ItemsNewReceiveTempo

//adapter: se encarga de coger la info
class ItemsReceiveAdapter( val itemsList: List<ItemsNewReceiveTempo>, private val onclickListener:(ItemsNewReceiveTempo)->Unit): RecyclerView.Adapter<ItemsReceiveViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsReceiveViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ItemsReceiveViewHolder(layoutInflater.inflate(R.layout.rv_item_receive,parent,false))
    }
    //devuelve el tamaño del listado
    override fun getItemCount(): Int =itemsList.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: ItemsReceiveViewHolder, position: Int) {
        val item =itemsList[position]
        item.position=position
        holder.render(item,onclickListener)


    }


}


