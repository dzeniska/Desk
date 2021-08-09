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

    val db = Firebase.database.getReference(MAIN_NODE)
    val auth = Firebase.auth

    fun publishAd(ad: Ad, finishListener: FinishWorkListener){
        if(auth.uid != null){
            db.child(ad.key ?: "empty").child(auth.uid!!).child(AD_NODE).setValue(ad
            ).addOnCompleteListener{task->
                if(task.isSuccessful) finishListener.onFinish()
//                Toast.makeText( Context, "sdaf", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun adViewed(ad: Ad, listener: FinishWorkListener) {
        var counter = ad.viewsCounter.toInt()
        counter++
        Log.d("!!!views", "${auth.uid}")
        if (auth.uid != null)
            db.child(ad.key ?: "empty").child(INFO_NODE)
                .setValue(InfoItem(counter.toString(), ad.emailsCounter, ad.callsCounter))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) listener.onFinish()
                }
    }

    fun onFavClick(ad: Ad, listener: FinishWorkListener){
        if(ad.isFav){
            removeFromFavs(ad,listener)
        } else {
            addToFavs(ad, listener)
        }
    }

    private fun addToFavs(ad:Ad, listener: FinishWorkListener){
        ad.key?.let {key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS_NODE).child(uid).setValue(uid)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful) listener.onFinish()
                    }
            }
        }
    }
    private fun removeFromFavs(ad:Ad, listener: FinishWorkListener){
        ad.key?.let {key ->
            auth.uid?.let { uid ->
                db.child(key).child(FAVS_NODE).child(uid).removeValue()
                    .addOnCompleteListener { task->
                        if(task.isSuccessful) listener.onFinish()
                    }
            }
        }
    }

    fun getMyAds(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild(auth.uid + "/ad/uid").equalTo(auth.uid)
        readDataFromDb(query, readDataCallback)
    }
    fun getMyFavs(readDataCallback: ReadDataCallback?){
        val query = db.orderByChild("/favs/${auth.uid}").equalTo(auth.uid)
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
                    var ad: Ad? = null
                    item.children.forEach{ data ->
                        if(ad == null) ad = data.child(AD_NODE).getValue(Ad::class.java)
                    }
//                    ad = item.children.iterator().next().child(AD_NODE).getValue(Ad::class.java)
                    val infoItem = item.child(INFO_NODE).getValue(InfoItem::class.java)
                    val isFav = auth.uid?.let { item.child(FAVS_NODE).child(it).getValue(String::class.java) }
                    ad?.isFav = isFav != null
                    val favCounter = item.child(FAVS_NODE).childrenCount
                    ad?.viewsCounter = infoItem?.viewsCounter ?: "0"
                    ad?.emailsCounter = infoItem?.emailsCounter ?: "0"
                    ad?.callsCounter = infoItem?.callsCounter ?: "0"
                    ad?.favCounter = favCounter.toString()
                    if(ad != null) adArray.add(ad!!)

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
    companion object{
        const val AD_NODE = "ad"
        const val INFO_NODE = "info"
        const val MAIN_NODE = "main"
        const val FAVS_NODE = "favs"
    }
}