package com.dzenis_ska.desk.utils

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList

object CityesHelper {
    fun getAllCountryes(context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jsonFile = String(byteArray)
            val jsonObject = JSONObject(jsonFile)
            val countriesNames = jsonObject.names()
            if (countriesNames != null) {
                for (c in 0 until countriesNames.length()) {
                    tempArray.add(countriesNames.getString(c))
                }
            }
        } catch (e: IOException) {
        }
        Log.d("!!!", tempArray.size.toString())
        return tempArray
    }

    fun getAllCityes(country: String, context: Context): ArrayList<String> {
        val tempArray = ArrayList<String>()
        try {
            val inputStream: InputStream = context.assets.open("countriesToCities.json")
            val size: Int = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            val jsonFile = String(byteArray)
            val jsonObject = JSONObject(jsonFile)
            val cityNames = jsonObject.getJSONArray(country)
            for (c in 0 until cityNames.length()) {
                tempArray.add(cityNames.getString(c))
            }

        } catch (e: IOException) {
        }
        Log.d("!!!", tempArray.size.toString())
        return tempArray
    }

    fun filterListData(list: ArrayList<String>, searchText: String?): ArrayList<String> {
        val tempList = ArrayList<String>()
        tempList.clear()
        if (searchText == null) {
            tempList.add("[No result///]")
            return tempList
        }
        val searchTextLower = searchText?.toLowerCase(Locale.ROOT)
        for (selection: String in list) {
            if (selection.toLowerCase(Locale.ROOT).startsWith(searchTextLower)) {
                tempList.add(selection)
            }
        }
        if (tempList.size == 0) tempList.add("[No result///]")
        return tempList
    }
}