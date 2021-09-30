package com.dzenis_ska.desk.act

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import androidx.viewpager2.widget.ViewPager2
import com.dzenis_ska.desk.adapters.ImageAdapter
import com.dzenis_ska.desk.databinding.ActivityDescriptionBinding
import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.utils.ImageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityDescriptionBinding
    lateinit var adapter: ImageAdapter
    private var ad: Ad? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescriptionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()

        binding.fbTel.setOnClickListener {
            call()
        }
        binding.fbEmail.setOnClickListener {
            sentEmail()
        }
    }
    fun init(){
        adapter = ImageAdapter()
        binding.apply {
            viewPager2.adapter = adapter
        }
        getIntentFromMainAct()
        imageChangeCounter()
    }
    private fun getIntentFromMainAct(){
        ad = intent.getSerializableExtra("AD") as Ad
        if(ad != null) updateUI(ad!!)
    }
    private fun updateUI(ad: Ad){
        ImageManager.fillImageArray(ad, adapter)
        fillTextViews(ad)
    }

    private fun fillTextViews(ad: Ad)= with(binding){
        tvTitle.text = ad.title
        tvDescription.text = ad.description
        tvEmai.text = ad.email
        tvPrice.text = ad.price
        tvTel.text = ad.tel
        tvCountry.text = ad.country
        tvCity.text = ad.city
        tvIndex.text = ad.index
        tvWithSent.text = isWithSent(ad.withSend.toBoolean())
    }
    private fun isWithSent(withSent: Boolean): String{
        return if(withSent) "Да" else "Нет"
    }

    private fun call(){
        val callUri = "tel:${ad?.tel}"
        val iCall = Intent(Intent.ACTION_DIAL)
        iCall.data = callUri.toUri()
        startActivity(iCall)
    }
    private fun sentEmail(){
        val iSendEmai = Intent(Intent.ACTION_SEND)
        iSendEmai.type = "message/rfc822"
        iSendEmai.apply {
            putExtra(Intent.EXTRA_EMAIL, arrayOf(ad?.email))
            putExtra(Intent.EXTRA_SUBJECT, "dsdfsdf")
            putExtra(Intent.EXTRA_TEXT, "text dsdfsdf")
        }
        try{
            startActivity(Intent.createChooser(iSendEmai, "Открыть с "))
        }catch (e: ActivityNotFoundException){

        }
    }
    private fun imageChangeCounter(){
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val imageCounter = "${position + 1}/${binding.viewPager2.adapter?.itemCount}"
                binding.tvImageCounter.text = imageCounter
            }
        })
    }
}