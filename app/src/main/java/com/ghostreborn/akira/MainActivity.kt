package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragment.LoginFragment
import com.ghostreborn.akira.fragment.LoginInfoFragment
import com.ghostreborn.akira.utils.AkiraUtils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!AkiraUtils().checkLogin(this)) {
            if (Constants.hasShownInfo){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, LoginFragment())
                    .commit()
            }else{
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, LoginInfoFragment())
                    .commit()
                Constants.hasShownInfo = true
            }
        }

    }
}