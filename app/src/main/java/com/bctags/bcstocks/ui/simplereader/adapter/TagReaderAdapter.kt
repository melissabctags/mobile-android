package com.bctags.bcstocks.ui.simplereader.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.response.ItemData
import com.bctags.bcstocks.io.response.ReceiveData


//adapter: se encarga de coger la info
class TagReaderAdapter(val list: MutableList<ItemData>,var hashUpcs: MutableMap<String, Int>) :
    RecyclerView.Adapter<TagReaderViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagReaderViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TagReaderViewHolder(
            layoutInflater.inflate(
                R.layout.rv_simple_reader_tag,
                parent,
                false
            )
        )
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int = list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: TagReaderViewHolder, position: Int) {
        val item = list[position]
        holder.render(item,hashUpcs)
    }


}


