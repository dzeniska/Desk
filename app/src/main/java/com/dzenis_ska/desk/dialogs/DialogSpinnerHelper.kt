package com.dzenis_ska.desk.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.utils.CityesHelper

class DialogSpinnerHelper {
    fun showSpinnerDialog(context: Context, list: ArrayList<String>, tvSelection: TextView){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        val rootView = LayoutInflater.from(context).inflate(R.layout.spinner_layout, null)
        val adapter = RcViewDialogSpinnerAdapter(tvSelection, dialog)
        val rcView = rootView.findViewById<RecyclerView>(R.id.rcSpView)
        val searchView = rootView.findViewById<SearchView>(R.id.svSpinner)
        rcView.layoutManager = LinearLayoutManager(context)
        rcView.adapter = adapter
        dialog.setView(rootView)
        adapter.updateAdapter(list)
        setSearchViewListener(adapter, list, searchView)
        dialog.show()
    }

    private fun setSearchViewListener(adapter: RcViewDialogSpinnerAdapter, list: ArrayList<String>, searchView: SearchView?) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val tempList = CityesHelper.filterListData(list, newText)
                adapter.updateAdapter(tempList)
                return true
            }

        })
    }


}