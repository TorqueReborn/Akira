package com.ghostreborn.akira

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ghostreborn.akira.fragments.HomeFragment
import com.ghostreborn.akira.fragments.LoginFragment
import com.ghostreborn.akira.models.retro.EntryMinimized
import com.ghostreborn.akira.parser.kitsu.KitsuAPI
import com.ghostreborn.akira.utils.AkiraUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CoroutineScope(Dispatchers.IO).launch {
            test()
        }

    }

    suspend fun test(): List<String> {
        val entryMinimized = ArrayList<EntryMinimized>()
        KitsuAPI().test(0)?.meta?.count?.let { total ->
            for (i in 0 until total step 50) {
                entryMinimized.add(KitsuAPI().test(i)!!)
            }
        }

        return entryMinimized.flatMap { it.data.map { entry -> entry.id } }
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