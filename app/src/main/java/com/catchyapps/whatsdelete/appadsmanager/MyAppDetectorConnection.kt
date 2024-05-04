package com.catchyapps.whatsdelete.appadsmanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.catchyapps.whatsdelete.BaseApplication.Companion.instance

object MyAppDetectorConnection {
    fun isNotConnectedToInternet(): Boolean {
        var connectivity: ConnectivityManager? = null
        try {
            connectivity =
                instance?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            for (networkInfo in info) if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                return false
            }
        }
        return true
    }
}