package com.dzenis_ska.desk.utils

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    fun launcher(edAct: EditAdsAct, imageCount: Int) {
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCount)) { result ->
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectImages(edAct, result.data)
                    closePixFragment(edAct)
                }
            }
//                    PixEventCallback.Status.BACK_PRESSED -> // back pressed called
        }
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
                    edAct.rootElement.pBarLoad.visibility = View.VISIBLE
                    val bitmapList = ImageManager.imageResize(uris, edAct)
                    edAct.rootElement.pBarLoad.visibility = View.GONE
                    edAct.imageAdapter.update(bitmapList as ArrayList<Bitmap>)
                }
            } else if (edAct.chooseImageFrag != null) {
                edAct.chooseImageFrag?.updateAdapter(uris as ArrayList<Uri>)
            }
        }


        fun getLauncherForSingleImage(edAct: EditAdsAct): ActivityResultLauncher<Intent> {
            return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                /* if (result.resultCode == Activity.RESULT_OK) {
                     if (result.data != null) {
                         val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                         edAct.chooseImageFrag?.setSingleImage(uris?.get(0)!!, edAct.editImagePos)
                     }
                 }*/
            }
        }

    }