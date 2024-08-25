package com.ghostreborn.akira

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragment.AnilistLoginFragment
import com.ghostreborn.akira.parsers.anilist.AnilistUtils
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
        val isLoggedIn = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
            .getBoolean(Constants.PREF_LOGGED_IN, false)
        if (!isLoggedIn) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.anilist_login_frame, AnilistLoginFragment())
                .commit()
        }else{
            val userId = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                .getString(Constants.PREF_USER_ID, "")
            Toast.makeText(this, "Logged in as $userId", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleIntentData() {
        intent.data?.getQueryParameter("code")?.let { code ->
            CoroutineScope(Dispatchers.IO).launch {
                AnilistUtils().getToken(code, this@MainActivity)
            }
        }
    }

}