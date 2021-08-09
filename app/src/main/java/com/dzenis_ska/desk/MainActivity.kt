package com.dzenis_ska.desk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dzenis_ska.desk.accountHelper.AccountHelper
import com.dzenis_ska.desk.act.EditAdsAct
import com.dzenis_ska.desk.adapters.AdsRcAdapter
import com.dzenis_ska.desk.databinding.ActivityMainBinding
import com.dzenis_ska.desk.dialoghelper.DialogConst
import com.dzenis_ska.desk.dialoghelper.DialogHelper
import com.dzenis_ska.desk.dialoghelper.GoogleAccConst
import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var rootElement: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    private val fireBaseViewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        val view = rootElement.root

        setContentView(view)
        init()
        initRecyclerView()
        initViewModel()
        fireBaseViewModel.loadAllAds()
        bottomMenuOnClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE) {
//            Log.d("!!!", "asdajdkjas")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("!!!", "Api error: ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initViewModel(){
        fireBaseViewModel.liveAdsData.observe(this, {listAd->
            rootElement.mainContent.tvEmpty.apply{
                if(listAd.isEmpty()){
                    visibility = View.VISIBLE
                    animation = AnimationUtils.loadAnimation(context, R.anim.alpha)
                }else{
                    visibility = View.GONE
                }
            }
            adapter.updateAdapter(listAd)
        })
    }

    private fun init() {
        setSupportActionBar(rootElement.mainContent.toolbar)
        //Кнопка в тулбаре
        val toggle = ActionBarDrawerToggle(
            this,
            rootElement.drawerLayout,
            rootElement.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        rootElement.navView.setNavigationItemSelectedListener(this)
        tvAccount = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
    }
    private fun bottomMenuOnClick() = with(rootElement.mainContent){
        bNavView.setOnNavigationItemSelectedListener {item->
            when (item.itemId){
                R.id.id_new_ad ->{
                        val intent = Intent(this@MainActivity, EditAdsAct::class.java)
                        startActivity(intent)
                }
                R.id.id_my_ads ->{
                    fireBaseViewModel.loadMyAds()
                    toolbar.title = getString(R.string.add_my_adds)
                }
                R.id.id_favs ->{
                    fireBaseViewModel.loadMyFavs()
                    Toast.makeText(this@MainActivity, "pressed id_favs", Toast.LENGTH_SHORT).show()
                }
                R.id.id_home ->{
                   fireBaseViewModel.loadAllAds()
                    toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        rootElement.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_adds -> {
                Toast.makeText(this, "id_my_adds", Toast.LENGTH_SHORT).show()
            }
            R.id.id_my_car -> {
                Toast.makeText(this, "id_my_car", Toast.LENGTH_SHORT).show()
            }
            R.id.id_my_pc -> {
                Toast.makeText(this, "id_my_pc", Toast.LENGTH_SHORT).show()
            }
            R.id.id_my_smartphones -> {
                Toast.makeText(this, "id_my_smartphones", Toast.LENGTH_SHORT).show()
            }
            R.id.id_my_dm -> {
                Toast.makeText(this, "id_my_dm", Toast.LENGTH_SHORT).show()
            }
            R.id.id_sign_up -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_UP_STATE)
                //Toast.makeText(this, "id_sign_up", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_sign_in -> {
                dialogHelper.createSignDialog(DialogConst.SIGN_IN_STATE)
                //Toast.makeText(this, "id_sign_in", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_sign_out -> {
                if(mAuth.currentUser?.isAnonymous == true){
                    rootElement.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
        }
        rootElement.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        Log.d("!!!", user.toString())
        if (user == null) {
//            resources.getString(R.string.not_reg)
            dialogHelper.accHelper.signInAnonimously(object : AccountHelper.Listener{
                override fun onComplete(listener: AccountHelper.Listener) {
                    tvAccount.text = "Гость"
                }
            })
        } else if(user.isAnonymous){
            tvAccount.text = "Гость"

        }else if(!user.isAnonymous){
            tvAccount.text = user.email
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("!!!", "onStart")
        uiUpdate(mAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        rootElement.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
    }

    override fun onDeleteItem(ad: Ad) {
        fireBaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        //эта строчка убирает мерцание итема вроде
        rootElement.mainContent.rcView.itemAnimator?.changeDuration = 250
        fireBaseViewModel.adViewed(ad)
    }

    override fun onFavClicked(ad: Ad) {
        rootElement.mainContent.rcView.itemAnimator?.changeDuration = 0
        fireBaseViewModel.onFavClick(ad)
    }

}