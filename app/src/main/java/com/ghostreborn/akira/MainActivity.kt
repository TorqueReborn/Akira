package com.ghostreborn.akira

import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akira.adapter.ViewPagerAdapter
import com.ghostreborn.akira.utils.NoInternetMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
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
        viewPager = findViewById(R.id.home_view_pager)
        viewPager.adapter = ViewPagerAdapter(this)
        setupNoInternetCheck()
    }

    private fun setupNoInternetCheck() {
        connectivityManager = getSystemService(ConnectivityManager::class.java)
        noInternetMonitor = NoInternetMonitor(noInternet = {
            internetAvailable = false
        }, internetAvailable = {
            internetAvailable = true
        })
        connectivityManager.registerDefaultNetworkCallback(noInternetMonitor)
    }

    companion object {
        var internetAvailable = false
    }

}