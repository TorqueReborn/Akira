package com.ghostreborn.akira

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.fragments.SearchFragment
import com.ghostreborn.akira.kitsu.KitsuAPI
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLogin()
    }

    private fun checkLogin(){
        if(!AkiraUtils().checkLogin(this)){
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, LoginFragment())
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, SearchFragment())
                .commit()
        }
    }

}