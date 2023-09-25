package com.bctags.bcstocks.ui.inventory.adapterInventoryCount

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.databinding.RvCountLocationsBinding
import com.bctags.bcstocks.databinding.RvInventoryListItemBinding
import com.bctags.bcstocks.io.response.InventoryData
import com.bctags.bcstocks.model.CountLocation

//ViewHolder:  se encarga de pintar las celdas

class CountLocationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = RvCountLocationsBinding.bind(view)

    fun render(item: CountLocation, onClickListener: (CountLocation,Int) -> Unit, position: Int) {
        binding.tvName.text = item.name

        binding.btnAction.setOnClickListener {
            onClickListener(item,position)
        }
    }


}
