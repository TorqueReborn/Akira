package com.ghostreborn.akirareborn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akirareborn.Constants.PREF_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    // TODO create database
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
            val code = uri.getQueryParameter("code").toString()
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code)
            }
        }
    }
}