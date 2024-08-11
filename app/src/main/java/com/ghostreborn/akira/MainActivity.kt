package com.ghostreborn.akira

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.ghostreborn.akira.fragment.HomeFragment


class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, HomeFragment())
            .commit()

    }
}