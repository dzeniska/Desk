package com.dzenis_ska.desk.act

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.dzenis_ska.desk.MainActivity
import com.dzenis_ska.desk.frag.FragmentCloseInterface
import com.dzenis_ska.desk.R
import com.dzenis_ska.desk.adapters.ImageAdapter
import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.model.DbManager
import com.dzenis_ska.desk.databinding.ActivityEditAdsBinding
import com.dzenis_ska.desk.dialogs.DialogSpinnerHelper
import com.dzenis_ska.desk.frag.ImageListFrag
import com.dzenis_ska.desk.utils.CityesHelper
import com.dzenis_ska.desk.utils.ImagePicker


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var rootElement: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()


    var editImagePos = 0
    private var isEditState = false
    private var ad:Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()
        checkEditState()
    }
    private fun checkEditState(){
        isEditState = isEditState()
        if(isEditState){
            ad = intent.getSerializableExtra(MainActivity.ADS_DATA) as Ad
            ad?.let {ad -> fillViews(ad) }
        }
    }

    private fun isEditState(): Boolean{
        return intent.getBooleanExtra(MainActivity.EDIT_STATE, false)
    }

    private fun fillViews(ad: Ad) = with(rootElement){
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)

    }


    private fun init() {
        imageAdapter = ImageAdapter()
        rootElement.vpImages.adapter = imageAdapter
    }



//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        ImagePicker.showSelectedImages(resultCode, requestCode, data, this)
//    }

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
            ImagePicker.launcher(this, 3)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) {
        val adTemp = fillAd()
        if(isEditState) {
            dbManager.publishAd(adTemp.copy(key = ad?.key), onPublishFinish())
        }else{
            dbManager.publishAd(adTemp, onPublishFinish())
        }

    }
    private fun onPublishFinish(): DbManager.FinishWorkListener{
        return object: DbManager.FinishWorkListener{
            override fun onFinish() {
               finish()
            }
        }
    }

    private fun fillAd(): Ad {
        val ad: Ad
        rootElement.apply {
            ad = Ad(
                tvCountry.text.toString(),
                tvCity.text.toString(),
                editTel.text.toString(),
                edIndex.text.toString(),
                checkBoxWithSend.isChecked.toString(),
                tvCat.text.toString(),
                edTitle.text.toString(),
                edPrice.text.toString(),
                edDescription.text.toString(),
                dbManager.db.push().key,
                "0",
                dbManager.auth.uid
            )
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        chooseImageFrag = null
        rootElement.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
    }

    fun openChooseImageFragment(returnValues: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this, returnValues)
        rootElement.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
}
