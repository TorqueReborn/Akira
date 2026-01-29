package com.ghostreborn.akira

import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.viewpager2.widget.ViewPager2
import com.ghostreborn.akira.ui.navigation.AppNavigation
import com.ghostreborn.akira.ui.navigation.BottomNavigationBar
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
            val navController = rememberNavController()
            AkiraTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) {innerPadding ->
                    AppNavigation(
                        navController = navController,
                        paddingValues = innerPadding
                    )
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