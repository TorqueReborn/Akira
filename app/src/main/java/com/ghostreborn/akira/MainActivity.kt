package com.ghostreborn.akira

import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.fragment.LoginFragment
import com.ghostreborn.akira.fragment.NoInternetFragment
import com.ghostreborn.akira.fragment.anime.FavoritesFragment
import com.ghostreborn.akira.fragment.anime.SeasonalFragment
import com.ghostreborn.akira.utils.NoInternetMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var noInternetMonitor: NoInternetMonitor
    private lateinit var latestButton: TextView
    private lateinit var favoritesButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        favoritesButton = findViewById<TextView>(R.id.favorites_text)
        latestButton = findViewById<TextView>(R.id.latest_text)
        setupNoInternetCheck()
    }

    override fun onResume() {
        super.onResume()

        if(internetAvailable) {
            latestButton.visibility = View.VISIBLE
            favoritesButton.visibility = View.VISIBLE
        } else {
            latestButton.visibility = View.GONE
            favoritesButton.visibility = View.GONE
        }

        val loggedIn = getSharedPreferences("AKIRA", MODE_PRIVATE)
            .getBoolean("LOGIN", false)

        latestButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, SeasonalFragment())
                .commit()
        }
        favoritesButton.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, FavoritesFragment())
                .commit()
        }

        if (internetAvailable) {
            val fragment: Fragment = if (loggedIn) {
                SeasonalFragment()
            } else {
                LoginFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, NoInternetFragment())
                .commit()
        }

    }

    private fun setupNoInternetCheck() {
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        noInternetMonitor = NoInternetMonitor(
            noInternet = {
                internetAvailable = false
            },
            internetAvailable = {
                internetAvailable = true
            }
        )
        connectivityManager.registerDefaultNetworkCallback(noInternetMonitor)
    }

    companion object {
        var internetAvailable = false
    }

}