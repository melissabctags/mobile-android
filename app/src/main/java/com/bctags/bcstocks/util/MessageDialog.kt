package com.bctags.bcstocks.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.bctags.bcstocks.R
import com.google.android.material.button.MaterialButton
import kotlin.reflect.KFunction3

class MessageDialog {

    fun showDialog(context: Context,layoutId: Int,message:String,buttonClickListener: () -> Unit) {
        val dialog = Dialog(context)
        dialog.setContentView(layoutId)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnDialog: MaterialButton = dialog.findViewById(R.id.btnDialog)

        if(message.isNotEmpty()){
            val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
            tvMessage.text = message
        }

        btnDialog.setOnClickListener {
            dialog.hide()
            buttonClickListener.invoke()
        }
        dialog.show()
    }





}