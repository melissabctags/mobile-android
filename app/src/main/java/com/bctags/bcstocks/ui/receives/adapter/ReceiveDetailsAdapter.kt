package com.bctags.bcstocks.ui.receives.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.ItemReceive


//adapter: se encarga de coger la info
class ReceiveDetailsAdapter(val list: MutableList<ItemReceive>) :
    RecyclerView.Adapter<ReceiveDetailsViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiveDetailsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ReceiveDetailsViewHolder(layoutInflater.inflate(R.layout.rv_simple_items_list, parent, false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int = list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: ReceiveDetailsViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,position)
    }


}


