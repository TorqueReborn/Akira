package com.ghostreborn.akira

import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akira.ui.theme.AkiraTheme
import com.ghostreborn.akira.utils.NoInternetMonitor

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var noInternetMonitor: NoInternetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AkiraTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Text("This is a main screen")
                    }
                }
            }
        }
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