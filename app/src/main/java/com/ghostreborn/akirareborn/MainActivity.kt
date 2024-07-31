package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ghostreborn.akirareborn.anilist.AnilistUtils
import com.ghostreborn.akirareborn.fragment.AnilistFragment
import com.ghostreborn.akirareborn.fragment.HomeFragment
import com.ghostreborn.akirareborn.test.TestFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // TODO show progress based on episode titles parsed
    // TODO show a sweepable tab layout for anilist for CURRENT, DROPPED etc
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBottomNavigation()
        setData()
        getToken()
    }

    private fun setBottomNavigation(){
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_layout, HomeFragment()).commit()
        bottomNavigationView.selectedItemId = R.id.nav_anime
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            val selectedFragment: Fragment
            val id = item.itemId
            selectedFragment = when (id) {
                R.id.nav_anime -> {
                    HomeFragment()
                }
                R.id.nav_user -> {
                    AnilistFragment()
                }
                else -> {
                    TestFragment()
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_layout, selectedFragment).commit()
            true
        }
    }

    private fun setData() {
        Constants.akiraSharedPreferences =
            getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        Constants.anilistToken =
            Constants.akiraSharedPreferences.getString(Constants.AKIRA_TOKEN, "")!!
        Constants.anilistUserID =
            Constants.akiraSharedPreferences.getString(Constants.AKIRA_USER_ID, "")!!
        Log.e("TAG", Constants.anilistUserID)
    }

    private fun getToken() {
        if (Constants.akiraSharedPreferences.getBoolean(Constants.AKIRA_LOGGED_IN, false)) {
            return
        }
        val intent: Intent = intent
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code!!)
            }
        }
    }

}