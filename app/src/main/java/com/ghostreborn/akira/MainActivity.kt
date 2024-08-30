package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.SearchFragment
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        handleIntentData()
        checkLogin()
    }

    private fun checkLogin(){
        if (!AkiraUtils().checkLogin(this)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, SearchFragment("ANIME"))
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, HomeFragment())
                .commit()
        }
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AkiraUtils().getTokenAndUserID(code, this@MainActivity)
            }
        }
    }

}