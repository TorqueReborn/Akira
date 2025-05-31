package com.ghostreborn.akira

import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.ghostreborn.akira.fragment.LoginFragment
import com.ghostreborn.akira.fragment.NoInternetFragment
import com.ghostreborn.akira.fragment.SeasonalFragment
import com.ghostreborn.akira.utils.NoInternetMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var noInternetMonitor: NoInternetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectivityManager = getSystemService(ConnectivityManager::class.java)
    }

    override fun onResume() {
        super.onResume()

        val loggedIn = getSharedPreferences("AKIRA", MODE_PRIVATE)
            .getBoolean("LOGIN", false)

        if(internetAvailable) {
            val fragment: Fragment = if(loggedIn) {
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

//    override fun onPause() {
//        super.onPause()
//        connectivityManager.unregisterNetworkCallback(noInternetMonitor)
//    }

    companion object {
        var internetAvailable = false
    }

}