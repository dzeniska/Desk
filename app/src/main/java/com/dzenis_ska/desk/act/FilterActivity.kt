package com.dzenis_ska.desk.act

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.databinding.ActivityFilterBinding
import com.dzenis_ska.desk.dialogs.DialogSpinnerHelper
import com.dzenis_ska.desk.utils.CityesHelper

class FilterActivity : AppCompatActivity() {
    lateinit var binding: ActivityFilterBinding
    private val dialog = DialogSpinnerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBarSettings()
        onClickSelectCountry()
        onClickSelectCity()
        onClickDone()
        onClickClear()
        getFilter()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
    private fun getFilter() = with(binding){
        val filter = intent.getStringExtra(FILTER_KEY)
        if(filter != null &&filter != "empty"){
            val filterArray = filter.split("_")
            if(filterArray[0] != "empty") tvCountry.text = filterArray[0]
            if(filterArray[1] != "empty") tvCity.text = filterArray[1]
            if(filterArray[2] != "empty") edIndex.setText(filterArray[2])
            checkBoxWithSend.isChecked = filterArray[3].toBoolean()
        }
    }

    //OnClicks
    private fun onClickSelectCountry() = with(binding){
        tvCountry.setOnClickListener {
            val listCountry = CityesHelper.getAllCountryes(this@FilterActivity)
            dialog.showSpinnerDialog(this@FilterActivity, listCountry, tvCountry)
            if (tvCity.text.toString() != getString(R.string.select_town)) {
                tvCity.text = getString(R.string.select_town)
            }
        }
    }
    private fun onClickDone() = with(binding){
        btDone.setOnClickListener {
          val i = Intent().apply {
              putExtra(FILTER_KEY, createFilter())
          }
            setResult(RESULT_OK, i)
            finish()
        }
    }

    private fun onClickClear() = with(binding){
        btClear.setOnClickListener {
           tvCountry.text = getString(R.string.select_country)
           tvCity.text = getString(R.string.select_town)
            edIndex.setText("")
            checkBoxWithSend.isChecked = false
            setResult(RESULT_CANCELED)
        }
    }

    private fun onClickSelectCity() = with(binding) {
        tvCity.setOnClickListener {
            val selectedCountry = tvCountry.text.toString()
            if (selectedCountry != getString(R.string.select_country)) {
                val listCity = CityesHelper.getAllCityes(selectedCountry, this@FilterActivity)
                dialog.showSpinnerDialog(this@FilterActivity, listCity, tvCity)
            } else {
                Toast.makeText(this@FilterActivity, R.string.no_selected_country, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createFilter(): String = with(binding){
        val sBuilder = StringBuilder()
        val arrayTempFilter = listOf(
            tvCountry.text,
            tvCity.text,
            edIndex.text,
            checkBoxWithSend.isChecked.toString()
        )
        for((i, s) in arrayTempFilter.withIndex()){
            if(s != getString(R.string.select_country) && s != getString(R.string.select_town) && s.isNotEmpty()){
                sBuilder.append(s)
                if(i != arrayTempFilter.size - 1)sBuilder.append("_")
            }else{
                sBuilder.append("empty")
                if(i != arrayTempFilter.size - 1)sBuilder.append("_")
            }
        }
        return sBuilder.toString()
    }

    fun actionBarSettings(){
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
    }
    companion object {
        const val FILTER_KEY = "filter_key"
    }
}