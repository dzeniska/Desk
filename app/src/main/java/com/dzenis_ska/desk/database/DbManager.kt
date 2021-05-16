package com.dzenis_ska.desk.database


import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {

    val db = Firebase.database.getReference("main")

    fun publishAd(){

        db.setValue("Hola2!")
        Log.d("!!!", "realTimeDataBase")
    }
}