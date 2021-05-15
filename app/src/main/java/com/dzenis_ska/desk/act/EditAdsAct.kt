package com.dzenis_ska.desk.act

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dzenis_ska.desk.frag.FragmentCloseInterface
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.adapters.ImageAdapter
import com.dzenis_ska.desk.databinding.ActivityEditAdsBinding
import com.dzenis_ska.desk.dialogs.DialogSpinnerHelper
import com.dzenis_ska.desk.frag.ImageListFrag
import com.dzenis_ska.desk.utils.CityesHelper
import com.dzenis_ska.desk.utils.ImageManager
import com.dzenis_ska.desk.utils.ImagePicker
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    var editImagePos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()
    }

    private fun init() {
        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Approvecker", Toast.LENGTH_LONG).show()
                    ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
                } else {
                    Toast.makeText(this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
    }

    //OnClicks
    fun onClickSelectCountry(view: View) {
        val listCountry = CityesHelper.getAllCountryes(this)
        dialog.showSpinnerDialog(this, listCountry, rootElement.tvCountry)
        if (rootElement.tvCity.text.toString() != getString(R.string.select_town)) {
            rootElement.tvCity.text = getString(R.string.select_town)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = rootElement.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val listCity = CityesHelper.getAllCityes(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, rootElement.tvCity)
        } else {
            Toast.makeText(this, R.string.no_selected_country, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {

        val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCat, rootElement.tvCat)

    }

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getImages(this, 3, ImagePicker.REQUEST_CODE_GET_IMAGES)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }

    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        chooseImageFrag = null
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
    }

    fun openChooseImageFragment(returnValues: ArrayList<String>?) {
        chooseImageFrag = ImageListFrag(this, returnValues)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
}
