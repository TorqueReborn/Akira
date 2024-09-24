package com.ghostreborn.akira

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.api.GojoAPI
import com.ghostreborn.akira.fragment.HomeFragment
import com.ghostreborn.akira.fragment.LoginFragment
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!AkiraUtils().checkLogin(this)) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, LoginFragment())
                .commit()
        }else{
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, HomeFragment())
                .commit()
        }

        CoroutineScope(Dispatchers.IO).launch {
            Log.e("TAG", GojoAPI().test())
        }

    }
}