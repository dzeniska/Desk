package com.dzenis_ska.desk

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dzenis_ska.desk.accountHelper.AccountHelper
import com.dzenis_ska.desk.act.DescriptionActivity
import com.dzenis_ska.desk.act.EditAdsAct
import com.dzenis_ska.desk.adapters.AdsRcAdapter
import com.dzenis_ska.desk.databinding.ActivityMainBinding
import com.dzenis_ska.desk.dialoghelper.DialogConst
import com.dzenis_ska.desk.dialoghelper.DialogHelper
import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.viewmodel.FirebaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AdsRcAdapter.Listener {
    private lateinit var tvAccount: TextView
    private lateinit var imAccount: ImageView
    private lateinit var binding: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = Firebase.auth
    val adapter = AdsRcAdapter(this)
    lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private val fireBaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)
        init()
        onActivityResult()
        navViewSettings()
        initRecyclerView()
        initViewModel()
//        fireBaseViewModel.loadAllAds("0")
        bottomMenuOnClick()
        scrollListener()
    }

    private fun onActivityResult() {
            googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("!!!", "Api error: ${e.message}")
                }

            }
    }

    private fun initViewModel(){
        fireBaseViewModel.liveAdsData.observe(this, {listAd->
            Log.d("!!!itemCount", "${adapter.itemCount}")
            binding.mainContent.tvEmpty.visibility = if(adapter.itemCount == 0) View.VISIBLE else View.GONE
//                    animation = AnimationUtils.loadAnimation(context, R.anim.alpha)

            if(!clearUpdate) {
                adapter.updateAdapter(listAd)
            }else{
                adapter.updateAdapterWithClear(listAd)
            }
        })
    }

    private fun init() {
        setSupportActionBar(binding.mainContent.toolbar)
        //Кнопка в тулбаре
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.mainContent.toolbar,
            R.string.open,
            R.string.close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
        imAccount = binding.navView.getHeaderView(0).findViewById(R.id.imAccauntImage)
    }
    private fun bottomMenuOnClick() = with(binding.mainContent){
        bNavView.setOnNavigationItemSelectedListener {item->
            clearUpdate = true
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
                   fireBaseViewModel.loadAllAds("0")
                    toolbar.title = getString(R.string.def)
                }
            }
            true
        }
    }

    private fun initRecyclerView() {
        binding.apply {
            mainContent.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
            mainContent.rcView.adapter = adapter

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        clearUpdate = true
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
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    return true
                }
                uiUpdate(null)
                mAuth.signOut()
                dialogHelper.accHelper.signOutG()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun uiUpdate(user: FirebaseUser?) {
        Log.d("!!!", user.toString())
        if (user == null) {
//            resources.getString(R.string.not_reg)
            dialogHelper.accHelper.signInAnonimously(object : AccountHelper.Listener{
                override fun onComplete(listener: AccountHelper.Listener) {
                    tvAccount.text = "Гость"
                    imAccount.setImageResource(R.drawable.ic_account_def)
                }
            })
        } else if(user.isAnonymous){
            tvAccount.text = "Гость"
            imAccount.setImageResource(R.drawable.ic_account_def)
        }else if(!user.isAnonymous){
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("!!!", "onStart")
        uiUpdate(mAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    override fun onDeleteItem(ad: Ad) {
        fireBaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        //эта строчка убирает мерцание итема вроде
        binding.mainContent.rcView.itemAnimator?.changeDuration = 250
        fireBaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
        binding.mainContent.rcView.itemAnimator?.changeDuration = 0
        fireBaseViewModel.onFavClick(ad)
    }
    private fun navViewSettings() = with(binding){
        val menu = navView.menu
        val adsCat = menu.findItem(R.id.adsCat)
        val spanAdsCat = SpannableString(adsCat.title)
        spanAdsCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.red_3)), 0, adsCat.title.length, 0)
        adsCat.title = spanAdsCat
        val accCat = menu.findItem(R.id.accCat)
        val spanAccCat = SpannableString(accCat.title)
        spanAccCat.setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, R.color.red_2)), 0, accCat.title.length, 0)
        accCat.title = spanAccCat
    }
    private fun scrollListener() = with(binding.mainContent){
        rcView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
//                Log.d("!!!scrollState", "${newState}")
                if(!recyclerView.canScrollVertically(SCROLL_DOWN)
                    && newState == RecyclerView.SCROLL_STATE_IDLE){
                        clearUpdate = false
//                    Log.d("!!!scroll", "${newState}")
                    val adsList = fireBaseViewModel.liveAdsData.value!!
                    if(adsList.isNotEmpty()) {
                        adsList[adsList.size - 1].let { fireBaseViewModel.loadAllAds(it.time) }
                    }
                }
            }
        })
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }

}