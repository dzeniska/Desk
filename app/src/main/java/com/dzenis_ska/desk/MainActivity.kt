                                                                                            package com.dzenis_ska.desk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.dzenis_ska.desk.act.EditAdsAct
import com.dzenis_ska.desk.databinding.ActivityMainBinding
import com.dzenis_ska.desk.dialoghelper.DialogConst
import com.dzenis_ska.desk.dialoghelper.DialogHelper
import com.dzenis_ska.desk.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tvAccount: TextView
    private lateinit var rootElement: ActivityMainBinding
    private val dialogHelper = DialogHelper(this)
    val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootElement = ActivityMainBinding.inflate(layoutInflater)
        val view = rootElement.root
        setContentView(view)
        init()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.id_new_ads){
            val intent = Intent(this, EditAdsAct::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == GoogleAccConst.GOOGLE_SIGN_IN_REQUEST_CODE){
//            Log.d("!!!", "asdajdkjas")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if(account != null){
                    dialogHelper.accHelper.signInFirebaseWithGoogle(account.idToken!!)
                }
            }catch (e: ApiException){
                Log.d("!!!", "Api error: ${e.message}")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun init(){
        setSupportActionBar(rootElement.mainContent.toolbar)
        //Кнопка в тулбаре
        val toggle = ActionBarDrawerToggle(this, rootElement.drawerLayout, rootElement.mainContent.toolbar, R.string.open, R.string.close)
        rootElement.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        rootElement.navView.setNavigationItemSelectedListener(this)
        tvAccount = rootElement.navView.getHeaderView(0).findViewById(R.id.tvAccauntEmail)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.id_my_adds -> {
                Toast.makeText(this, "id_my_adds", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_my_car -> {
                Toast.makeText(this, "id_my_car", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_my_pc -> {
                Toast.makeText(this, "id_my_pc", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_my_smartphones -> {
                Toast.makeText(this, "id_my_smartphones", Toast.LENGTH_SHORT ).show()
            }
            R.id.id_my_dm -> {
                Toast.makeText(this, "id_my_dm", Toast.LENGTH_SHORT ).show()
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
        tvAccount.text = if (user == null) {
            resources.getString(R.string.not_reg)
        } else {
            user.email
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("!!!", "onStart")
        uiUpdate(mAuth.currentUser)
    }
}