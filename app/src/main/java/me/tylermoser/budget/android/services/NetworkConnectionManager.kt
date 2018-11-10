package me.tylermoser.budget.android.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

/**
 * A helper class that provides methods for managing the device's network connection
 *
 * @author Tyler Moser
 */
class NetworkConnectionManager {

    /**
     * Returns whether or not the device is currently connected to a network
     */
    fun isDeviceConnectedToNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }

}