package com.dzenis_ska.desk.model


import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dzenis_ska.desk.MainActivity

import com.dzenis_ska.desk.viewmodel.FirebaseViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.coroutines.coroutineContext

class DbManager {

    val db = Firebase.database.getReference("main")
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishListener: FinishWorkListener){

        if(auth.uid != null){
            db.child(ad.key ?: "empty").child(auth.uid!!).child("ad").setValue(ad
            ).addOnCompleteListener{task->
                if(task.isSuccessful) finishListener.onFinish()
//                Toast.makeText( Context, "sdaf", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }
    fun getAllAds(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild(auth.uid + "/ad/country")
        readDataFromDb(query, readDataCallback)
    }

    fun deleteAd(ad: Ad, finishListener: FinishWorkListener){
        if(ad.key == null || ad.uid == null) return
        db.child(ad.key).child(ad.uid).removeValue().addOnCompleteListener{task ->
            if(task.isSuccessful) finishListener.onFinish()
        }
    }

   private fun readDataFromDb(query: Query, readDataCallback: ReadDataCallback?){
        query.addListenerForSingleValueEvent(object: ValueEventListener{
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
    interface  FinishWorkListener{
        fun onFinish(){
        }
    }
}