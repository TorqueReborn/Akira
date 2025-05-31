package com.ghostreborn.akira.utils

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class NoInternetMonitor(
    private val noInternet: () -> Unit,
    private val internetAvailable: () -> Unit
): ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        internetAvailable()
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        noInternet()
    }

    override fun onUnavailable() {
        super.onUnavailable()
        noInternet()
    }


    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val isInternetAvailable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!isInternetAvailable) {
            noInternet()
        } else {
            internetAvailable()
        }
    }

}