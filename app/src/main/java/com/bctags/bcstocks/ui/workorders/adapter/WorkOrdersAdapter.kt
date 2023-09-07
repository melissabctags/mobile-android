package com.bctags.bcstocks.ui.workorders.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bctags.bcstocks.R
import com.bctags.bcstocks.io.ApiCall
import com.bctags.bcstocks.io.ApiClient
import com.bctags.bcstocks.io.response.WorkOrderData
import com.bctags.bcstocks.model.ActionWorkOrder
import com.bctags.bcstocks.model.FilterRequest
import com.bctags.bcstocks.model.Pagination
import com.bctags.bcstocks.model.ReceiveNew
import com.bctags.bcstocks.model.WorkOrderStatus
import com.bctags.bcstocks.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

//adapter: se encarga de coger la info
class WorkOrdersAdapter(val list: List<WorkOrderData>, private val onclickListener:(WorkOrderData)->Unit, private val onSecondClickListener:(ActionWorkOrder)->Unit, private val workordersString:String): RecyclerView.Adapter<WorkOrdersViewHolder>() {

    //pasar el item, layout a modificar
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkOrdersViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return WorkOrdersViewHolder(layoutInflater.inflate(R.layout.rv_workorder_list,parent,false))
    }

    //devuelve el tamaño del listado
    override fun getItemCount(): Int =list.size

    //pasar por cada item y va a llamar al render
    override fun onBindViewHolder(holder: WorkOrdersViewHolder, position: Int) {
        val item =list[position]


        if(workordersString.isNotEmpty() && workordersString!="{}"){
            val gson= Gson()
            var workOrderStatus = gson.fromJson(workordersString, Array<WorkOrderStatus>::class.java).asList()
           // var workOrderStatus = Json.decodeFromString<Array<WorkOrderStatus>>(workordersString).asList()

            Log.i("WORK_ORDER_ADAPTER",workOrderStatus.toString())
            Log.i("WORK_ORDER_ADAPTER2",workOrderStatus.toList().toString())
            val result = workOrderStatus.find { it.id == item.id }
            if (result != null) {
                holder.render(item,onclickListener,onSecondClickListener,result.partialId,result.moduleName)
            }else{
                holder.render(item,onclickListener,onSecondClickListener,0,"")
            }
        }else{
            holder.render(item,onclickListener,onSecondClickListener,0,"")
        }
//        holder.render(item,onclickListener,onSecondClickListener)
    }





}


