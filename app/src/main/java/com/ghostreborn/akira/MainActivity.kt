package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.utils.AkiraUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntentData()
        checkLogin()
    }

    private fun checkLogin(){
        if (!AkiraUtils().checkLogin(this)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, LoginFragment())
                .commit()
        }
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            AkiraUtils().storeLoginCode(code, this)
        }
    }

}