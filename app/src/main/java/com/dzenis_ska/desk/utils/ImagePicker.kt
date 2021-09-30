package com.dzenis_ska.desk.utils

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    const val MAX_IMAGE_COUNT = 3
    fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = true
            mode = Mode.Picture
            path = "/pix/images"
        }
        return options

    }

    fun getMultiImages(edAct: EditAdsAct, imageCount: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct, result.data)
                }
            }
//                    PixEventCallback.Status.BACK_PRESSED -> // back pressed called
        }
    }
    fun addImages(edAct: EditAdsAct, imageCount: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct as Activity)
                }
            }
//                    PixEventCallback.Status.BACK_PRESSED -> // back pressed called
        }
    }

    fun getSingleImage(edAct: EditAdsAct) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct)
                    singleImage(edAct, result.data[0])
                }
            }
//                    PixEventCallback.Status.BACK_PRESSED -> // back pressed called
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsAct){
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder,
            edAct.chooseImageFrag!!
        ).commit()
    }

    private fun closePixFragment(edAct: EditAdsAct) {
        Log.d("!!!", "good")
        val fList = edAct.supportFragmentManager.fragments
        fList.forEach {
            if (it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    fun getMultiSelectImages(edAct: EditAdsAct, uris: List<Uri>) {

        if (uris.size > 1 && edAct.chooseImageFrag == null) {
            edAct.openChooseImageFragment(uris as ArrayList<Uri>)
        } else if (uris.size == 1 && edAct.chooseImageFrag == null) {
            CoroutineScope(Dispatchers.Main).launch {
                edAct.binding.pBarLoad.visibility = View.VISIBLE
                val bitmapList = ImageManager.imageResize(uris, edAct)
                edAct.binding.pBarLoad.visibility = View.GONE
                edAct.imageAdapter.update(bitmapList as ArrayList<Bitmap>)
                closePixFragment(edAct)
            }
        }
    }

    private fun singleImage(edAct: EditAdsAct, uri:Uri) {
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos)
    }

}