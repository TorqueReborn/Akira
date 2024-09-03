package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.utils.AkiraUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkLogin()
    }

    private fun checkLogin() {
        if (!AkiraUtils().checkLogin(this)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, LoginFragment())
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, HomeFragment())
                .commit()
        }
    }

}