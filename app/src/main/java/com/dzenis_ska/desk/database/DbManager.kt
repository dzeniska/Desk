package com.dzenis_ska.desk.database


import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {

    val db = Firebase.database.reference

    fun publishAd(){

        db.setValue("Hola!")
        Log.d("!!!", "realTimeDataBase")
    }
}