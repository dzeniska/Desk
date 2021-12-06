package com.dzenis_ska.desk.utils

import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.model.AdFilter
import java.lang.StringBuilder

object FilterManager {
    fun createFilter(ad: Ad): AdFilter{

        return AdFilter(

            "${ad.category}_${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.withSend}_${ad.time}",
            "${ad.category}_${ad.time}",

            "${ad.country}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.city}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.country}_${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.index}_${ad.withSend}_${ad.time}",
            "${ad.withSend}_${ad.time}",
            "$ad.time"
        )
    }
    fun getFilter(filter: String): String{
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val tempArray = filter.split("_")
        if(tempArray[0] != "empty") {
            sBuilderNode.append("country_")
            sBuilderFilter.append("${tempArray[0]}_")
        }
        if(tempArray[1] != "empty") {
            sBuilderNode.append("city_")
            sBuilderFilter.append("${tempArray[1]}_")
        }
        if(tempArray[2] != "empty") {
            sBuilderNode.append("index_")
            sBuilderFilter.append("${tempArray[2]}_")
        }
        sBuilderNode.append("withSent_time")
        sBuilderFilter.append(tempArray[3])
        return "${sBuilderNode}|${sBuilderFilter}"
    }
}