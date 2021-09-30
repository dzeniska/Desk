package com.dzenis_ska.desk.act

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
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
import com.dzenis_ska.desk.utils.ImageManager
import com.dzenis_ska.desk.utils.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import java.io.ByteArrayOutputStream


class EditAdsAct : AppCompatActivity(), FragmentCloseInterface {
    var chooseImageFrag: ImageListFrag? = null
    lateinit var binding: ActivityEditAdsBinding
    private val dialog = DialogSpinnerHelper()
    lateinit var imageAdapter: ImageAdapter
    private val dbManager = DbManager()


    var editImagePos = 0
    private var imageIndex = 0
    private var isEditState = false
    private var ad:Ad? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditAdsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        checkEditState()
        imageChangeCounter()
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

    private fun fillViews(ad: Ad) = with(binding){
        tvCountry.text = ad.country
        tvCity.text = ad.city
        editTel.setText(ad.tel)
        edIndex.setText(ad.index)
        checkBoxWithSend.isChecked = ad.withSend.toBoolean()
        tvCat.text = ad.category
        edTitle.setText(ad.title)
        edPrice.setText(ad.price)
        edDescription.setText(ad.description)
        ImageManager.fillImageArray(ad, imageAdapter)
    }


    private fun init() {
        imageAdapter = ImageAdapter()
        binding.vpImages.adapter = imageAdapter
    }


    //OnClicks
    fun onClickSelectCountry(view: View) {
        val listCountry = CityesHelper.getAllCountryes(this)
        dialog.showSpinnerDialog(this, listCountry, binding.tvCountry)
        if (binding.tvCity.text.toString() != getString(R.string.select_town)) {
            binding.tvCity.text = getString(R.string.select_town)
        }
    }

    fun onClickSelectCity(view: View) {
        val selectedCountry = binding.tvCountry.text.toString()
        if (selectedCountry != getString(R.string.select_country)) {
            val listCity = CityesHelper.getAllCityes(selectedCountry, this)
            dialog.showSpinnerDialog(this, listCity, binding.tvCity)
        } else {
            Toast.makeText(this, R.string.no_selected_country, Toast.LENGTH_LONG).show()
        }
    }

    fun onClickSelectCat(view: View) {
        val listCat = resources.getStringArray(R.array.category).toMutableList() as ArrayList
        dialog.showSpinnerDialog(this, listCat, binding.tvCat)
    }

    fun onClickGetImages(view: View) {
        if (imageAdapter.mainArray.size == 0) {
            ImagePicker.getMultiImages(this, 3)
        } else {
            openChooseImageFragment(null)
            chooseImageFrag?.updateAdapterFromEdit(imageAdapter.mainArray)
        }
    }

    fun onClickPublish(view: View) {
        ad = fillAd()
        if(isEditState) {
            ad?.copy(key = ad?.key)?.let { dbManager.publishAd(it, onPublishFinish()) }
        }else{
//            dbManager.publishAd(adTemp, onPublishFinish())
            uploadImages()
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
        binding.apply {
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
                editEmail.text.toString(),
                "empty",
                "empty",
                "empty",
                dbManager.db.push().key,
                "0",
                dbManager.auth.uid,
                System.currentTimeMillis().toString()
            )
        }
        return ad
    }

    override fun onFragClose(list: ArrayList<Bitmap>) {
        chooseImageFrag = null
        binding.scrollViewMain.visibility = View.VISIBLE
        imageAdapter.update(list)
    }

    fun openChooseImageFragment(returnValues: ArrayList<Uri>?) {
        chooseImageFrag = ImageListFrag(this)
        if (returnValues != null) chooseImageFrag?.resizeSelectedImages(returnValues, true, this)
        binding.scrollViewMain.visibility = View.GONE
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.place_holder, chooseImageFrag!!)
        fm.commit()
    }
    private fun uploadImages() {
        if(imageAdapter.mainArray.size == imageIndex){
            dbManager.publishAd(ad!!, onPublishFinish())
            return
        }
        val byteArray = prepareImageBiteArray(imageAdapter.mainArray[imageIndex])
        uploadImage(byteArray){
            dbManager.publishAd(ad!!, onPublishFinish())
            nextImage(it.result.toString())
        }
    }
    private fun nextImage(uri: String) {
        setImageUriToAd(uri)
        imageIndex++
        uploadImages()
    }
    private fun setImageUriToAd(uri: String){
        when (imageIndex){
            0 -> ad = ad?.copy(mainImage = uri)
            1 -> ad = ad?.copy(image2 = uri)
            2 -> ad = ad?.copy(image3 = uri)
        }
    }
    private fun prepareImageBiteArray(bitMap: Bitmap): ByteArray{
        val outStream = ByteArrayOutputStream()
        bitMap.compress(Bitmap.CompressFormat.JPEG, 25, outStream)
        return outStream.toByteArray()
    }
    private fun uploadImage(byteArray: ByteArray, listener: OnCompleteListener<Uri>){
        val imStorageRef = dbManager.dbStorage
            .child(dbManager.auth.uid!!)
            .child("image_${System.currentTimeMillis()}")

        val upTask = imStorageRef.putBytes(byteArray)
        upTask.continueWithTask{task->
            imStorageRef.downloadUrl
        }.addOnCompleteListener(listener)
    }
    private fun imageChangeCounter(){
        binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.vpImages.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }
}
