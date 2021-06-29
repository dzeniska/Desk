package com.dzenis_ska.desk.model


import android.util.Log

import com.dzenis_ska.desk.viewmodel.FirebaseViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DbManager {

    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAd(ad: Ad){

        if(auth.uid != null){
            db.child(ad.key ?: "empty").child(auth.uid!!).child("ad").setValue(ad)
        }
    }

    fun readDataFromDb(readDataCallback: ReadDataCallback?){
        db.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adArray = ArrayList<Ad>()
                for(item in snapshot.children){
                    val ad = item.children.iterator().next().child("ad").getValue(Ad::class.java)
                    if(ad != null){
                        adArray.add(ad)
                    }
                }
                Log.d("!!!vm", "viewModel $adArray")
                readDataCallback?.readData(adArray)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("!!!", error.toString())
            }

        })
    }

    interface ReadDataCallback {
        fun readData(list:ArrayList<Ad>)
    }
}