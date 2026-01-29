package com.ghostreborn.akira

import android.os.Bundle
import androidx.compose.ui.Modifier
import android.net.ConnectivityManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.ghostreborn.akira.ui.theme.AkiraTheme
import com.ghostreborn.akira.utils.NoInternetMonitor
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.navigation.compose.rememberNavController
import com.ghostreborn.akira.ui.navigation.AppNavigation
import com.ghostreborn.akira.ui.navigation.BottomNavigationBar

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var noInternetMonitor: NoInternetMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
            AkiraTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(
                            navController = navController
                        )
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            BottomNavigationBar(navController = navController)
                        }
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