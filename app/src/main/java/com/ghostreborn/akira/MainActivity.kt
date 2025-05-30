package com.ghostreborn.akira

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.fragment.LoginFragment
import com.ghostreborn.akira.fragment.SeasonalFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loggedIn = getSharedPreferences("AKIRA", MODE_PRIVATE)
            .getBoolean("LOGIN", false)

        val fragment: Fragment = if(loggedIn) {
            SeasonalFragment()
        } else {
            LoginFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame, fragment)
            .commit()

    }
}