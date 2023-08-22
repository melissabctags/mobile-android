package com.bctags.bcstocks.ui.receives.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.ReceiveData

//adapter: se encarga de coger la info
class ReceivesAdapter(val receivesList: List<ReceiveData>, private val onclickListener:(ReceiveData)->Unit): RecyclerView.Adapter<ReceivesViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceivesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ReceivesViewHolder(layoutInflater.inflate(R.layout.rv_receives_list,parent,false))
    }
    //devuelve el tamaño del listado
    override fun getItemCount(): Int =receivesList.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: ReceivesViewHolder, position: Int) {
        val item =receivesList[position]
        holder.render(item,onclickListener)
    }


}


