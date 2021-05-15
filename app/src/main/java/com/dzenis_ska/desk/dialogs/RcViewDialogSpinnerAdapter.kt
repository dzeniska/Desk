package com.dzenis_ska.desk.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.act.EditAdsAct

class RcViewDialogSpinnerAdapter(val tvSelection: TextView, val dialog: AlertDialog): RecyclerView.Adapter<RcViewDialogSpinnerAdapter.SpViewHolder>() {
    private val mainList = ArrayList<String>()

    class SpViewHolder(itemView: View, val tvSelection: TextView, val dialog: AlertDialog): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var itemText = ""
        fun setData(text:String){
            val tvSpItem = itemView.findViewById<TextView>(R.id.tvSpItem)
            tvSpItem.text = text
            itemText = text
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            tvSelection.text = itemText
            dialog.dismiss()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sp_list_item, parent, false)
        return SpViewHolder(view, tvSelection, dialog)
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: SpViewHolder, position: Int) {
        holder.setData(mainList[position])
    }
    fun updateAdapter(list: ArrayList<String>){
        mainList.clear()
        mainList.addAll(list)
        notifyDataSetChanged()
    }

}


