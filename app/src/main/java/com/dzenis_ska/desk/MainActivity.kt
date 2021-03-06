package com.dzenis_ska.desk

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.dzenis_ska.desk.act.FilterActivity
import com.dzenis_ska.desk.adapters.AdsRcAdapter
import com.dzenis_ska.desk.databinding.ActivityMainBinding
import com.dzenis_ska.desk.dialoghelper.DialogConst
import com.dzenis_ska.desk.dialoghelper.DialogHelper
import com.dzenis_ska.desk.model.Ad
import com.dzenis_ska.desk.utils.AppMainState
import com.dzenis_ska.desk.utils.BillingManager
import com.dzenis_ska.desk.utils.FilterManager
import com.dzenis_ska.desk.viewmodel.FirebaseViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
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
    lateinit var filterLauncher: ActivityResultLauncher<Intent>
    private val fireBaseViewModel: FirebaseViewModel by viewModels()
    private var clearUpdate: Boolean = true
    private var currentCategory: String? = null
    private var filter: String = "empty"
    private var filterDb: String = ""
    private var pref: SharedPreferences? = null
    private var isPremiumUser = false
    private var bManager: BillingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        pref = getSharedPreferences(BillingManager.MAIN_PREF, MODE_PRIVATE)
        isPremiumUser = pref?.getBoolean(BillingManager.REMOVE_ADS_PREF, false)!!
        isPremiumUser = true
        if(!isPremiumUser){
                (application as AppMainState).showAdIfAvailable(this){
                Toast.makeText(this@MainActivity, "yobanaya reklama", Toast.LENGTH_SHORT).show()
            }
            initAds()
        } else {
            binding.mainContent.adView2.visibility = View.GONE
        }

        init()

        onActivityResult()
        navViewSettings()
        initRecyclerView()
        initViewModel()
        bottomMenuOnClick()
        scrollListener()
        onActivityResultFilter()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_filter) {
            val i = Intent(this@MainActivity, FilterActivity::class.java).apply{
                putExtra(FilterActivity.FILTER_KEY, filter)
            }
            filterLauncher.launch(i)
        }
        return super.onOptionsItemSelected(item)
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

    private fun onActivityResultFilter(){
        filterLauncher = registerForActivityResult(ActivityResultContracts
            .StartActivityForResult()) {
            if(it.resultCode == RESULT_OK){
                filter = it.data?.getStringExtra(FilterActivity.FILTER_KEY)!!
                filterDb = FilterManager.getFilter(filter)
            } else if(it.resultCode == RESULT_CANCELED){
                filterDb = ""
                filter = "empty"
            }
        }
    }

    private fun initViewModel(){
        fireBaseViewModel.liveAdsData.observe(this, {listAd->
            val list = getAdsByCategory(listAd)

//                    animation = AnimationUtils.loadAnimation(context, R.anim.alpha)
            if(!clearUpdate) {
                adapter.updateAdapter(list)
            }else{
                adapter.updateAdapterWithClear(list)
            }
            binding.mainContent.tvEmpty.visibility = if(adapter.itemCount == 0) View.VISIBLE else View.GONE
        })
    }

    private fun getAdsByCategory(list: ArrayList<Ad>): ArrayList<Ad>{
        val tempList = ArrayList<Ad>()
        tempList.addAll(list)
        if(currentCategory != getString(R.string.def)){
            tempList.clear()
            list.forEach {
                if(currentCategory == it.category) tempList.add(it)
            }
        }
        tempList.reverse()
        return tempList
    }

    private fun init() {
        currentCategory = getString(R.string.def)
        setSupportActionBar(binding.mainContent.toolbar)
        //???????????? ?? ??????????????
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
                    currentCategory = getString(R.string.def)
                    fireBaseViewModel.loadAllAdsFirstPage(filterDb)
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
                getAdsFromCat(getString(R.string.add_my_car))
            }
            R.id.id_my_pc -> {
                getAdsFromCat(getString(R.string.add_my_pc))
            }
            R.id.id_my_smartphones -> {
                getAdsFromCat(getString(R.string.add_my_smartphones))
            }
            R.id.id_my_dm -> {
                getAdsFromCat(getString(R.string.add_my_dm))
            }
            R.id.id_remove_ads -> {
                bManager = BillingManager(this)
                bManager?.startConnection()
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

    private fun getAdsFromCat(cat: String){
        currentCategory = cat
        fireBaseViewModel.loadAllAdsFromCat(cat, filterDb)
    }

    fun uiUpdate(user: FirebaseUser?) {
        user?.email?.let {  }
        if (user == null) {
//            resources.getString(R.string.not_reg)
            dialogHelper.accHelper.signInAnonimously(object : AccountHelper.Listener{
                override fun onComplete(listener: AccountHelper.Listener) {
                    tvAccount.text = "??????????"
                    imAccount.setImageResource(R.drawable.ic_account_def)
                }
            })
        } else if(user.isAnonymous){
            tvAccount.text = "??????????"
            imAccount.setImageResource(R.drawable.ic_account_def)
        }else if(!user.isAnonymous){
            tvAccount.text = user.email
            Picasso.get().load(user.photoUrl).into(imAccount)
        }
    }

    override fun onStart() {
        super.onStart()
        uiUpdate(mAuth.currentUser)
    }

    override fun onResume() {
        super.onResume()
        binding.mainContent.adView2.resume()
        binding.mainContent.bNavView.selectedItemId = R.id.id_home
    }

    override fun onPause() {
        super.onPause()
        binding.mainContent.adView2.pause()
    }

    private fun initAds(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.mainContent.adView2.loadAd(adRequest)
    }

    override fun onDeleteItem(ad: Ad) {
        fireBaseViewModel.deleteItem(ad)
    }

    override fun onAdViewed(ad: Ad) {
        //?????? ?????????????? ?????????????? ???????????????? ?????????? ??????????
        binding.mainContent.rcView.itemAnimator?.changeDuration = 250
        fireBaseViewModel.adViewed(ad)
        val i = Intent(this, DescriptionActivity::class.java)
        i.putExtra("AD", ad)
        startActivity(i)
    }

    override fun onFavClicked(ad: Ad) {
//        binding.mainContent.rcView.itemAnimator?.changeDuration = 0
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
                if(!recyclerView.canScrollVertically(SCROLL_DOWN)
                    && newState == RecyclerView.SCROLL_STATE_IDLE){
                        clearUpdate = false
                    val adsList = fireBaseViewModel.liveAdsData.value!!
                    if(adsList.isNotEmpty()) {
                        getAdsFromCatS(adsList)
                    }
                }
            }
        })
    }
    private fun getAdsFromCatS(adsList: ArrayList<Ad>) {
        adsList[0].let {

            if (currentCategory == getString(R.string.def)) {
                fireBaseViewModel.loadAllAdsNextPage(it.time, filterDb)
            } else {
                fireBaseViewModel.loadAllAdsFromCatNextPage(it.category!!, it.time, filterDb)
            }
        }
    }

    override fun onDestroy() {
        binding.mainContent.adView2.destroy()
        bManager?.closeConnection()
        super.onDestroy()
    }

    companion object {
        const val EDIT_STATE = "edit_state"
        const val ADS_DATA = "ads_data"
        const val SCROLL_DOWN = 1
    }

}