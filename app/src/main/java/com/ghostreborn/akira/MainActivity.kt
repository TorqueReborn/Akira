package com.ghostreborn.akira

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.database.SavedEntry
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        checkLogin()

        CoroutineScope(Dispatchers.IO).launch {
            val db = AkiraUtils().getDB(this@MainActivity)
            db.savedEntryDao().insert(SavedEntry("1", "2", "3"))

            val entries = db.savedEntryDao().getAll()
            Log.e("DATABASE", entries.toString())

        }

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