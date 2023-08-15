package com.bctags.bcstocks.util

import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.bctags.bcstocks.R

class DropDown {

    fun listArrange(list: List<String>, autoComplete: AutoCompleteTextView, mapping: HashMap<String, String>, context: Context, action: (param1: String, param2: String)->Unit) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(context, R.layout.list_item, list)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val selectedText = autoComplete.text.toString()
            val itemId: String = mapping[selectedText].toString()
            action(itemId,selectedText)
        }
    }

}