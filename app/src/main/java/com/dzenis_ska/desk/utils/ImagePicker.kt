package com.dzenis_ska.desk.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.dzenis_ska.desk.act.EditAdsAct
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGE = 998
    const val MAX_IMAGE_COUNT = 3
    fun getOptions(imageCounter: Int): Options{
        val options = Options.init()
//                .setRequestCode(rCode)                                           //Request code for activity results
                .setCount(imageCounter)                                                   //Number of images to restict selection count
//                .setFrontfacing(false)
                .setMode(Options.Mode.Picture)                                     //Option to select only pictures or videos or both
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/pix/images")                                      //Custom Path For media Storage
            return options
//        Pix.start(context, options)
    }

    fun launcher(edAct: EditAdsAct, launcher: ActivityResultLauncher<Intent>?, imageCount: Int){
        PermUtil.checkForCamaraWritePermissions(edAct){
            val intent = Intent(edAct, Pix::class.java).apply {
                putExtra("options", getOptions(imageCount))
            }
            launcher?.launch(intent)
        }
    }
    
    fun getLauncherForMultiSelectImages(edAct: EditAdsAct): ActivityResultLauncher<Intent> {
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if(result.data != null){
                    val returnValues = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    if (returnValues?.size!! > 1 && edAct.chooseImageFrag == null) {
                        edAct.openChooseImageFragment(returnValues)
                    } else if (returnValues.size == 1 && edAct.chooseImageFrag == null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            edAct.rootElement.pBarLoad.visibility = View.VISIBLE
                            val bitmapList = ImageManager.imageResize(returnValues)
                            edAct.rootElement.pBarLoad.visibility = View.GONE
                            edAct.imageAdapter.update(bitmapList as ArrayList<Bitmap>)
                        }
                    } else if (edAct.chooseImageFrag != null) {
                        edAct.chooseImageFrag?.updateAdapter(returnValues)
                    }
                }
            }
        }
    }

    fun getLauncherForSingleImage(edAct: EditAdsAct): ActivityResultLauncher<Intent>{
        return edAct.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    val uris = result.data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)
                    edAct.chooseImageFrag?.setSingleImage(uris?.get(0)!!, edAct.editImagePos)
                }
            }
        }
    }

}