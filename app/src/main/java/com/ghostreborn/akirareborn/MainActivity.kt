package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.Constants.PREF_NAME


class MainActivity : AppCompatActivity() {

    // TODO login to anilist
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setData()
        getData()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_layout, AnilistLoginFragment())
            .commit()
    }

    private fun setData() {
        Constants.preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    }

    private fun getData() {
        val intent: Intent = intent
        val uri = intent.data
        if (uri != null) {
            val code = uri.getQueryParameter("code")
            Log.e("MainActivity", "Code: $code")
        }
    }
}