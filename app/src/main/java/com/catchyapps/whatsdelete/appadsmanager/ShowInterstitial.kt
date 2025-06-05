package com.catchyapps.whatsdelete.appadsmanager

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs

object ShowInterstitial {
    @JvmStatic
    fun showInter(activity: Activity) {
        if (BaseApplication.isFbInterstitialAvailable()) {
            activity.startActivity(Intent(activity, PleaseWaitAdScreen::class.java)
                .putExtra("admob", false))
        }
    }

    @JvmStatic
    fun showAdmobInter(activity: Activity) {
        if (BaseApplication.isInterstitialAvailable()) {
            activity.startActivity(Intent(activity, PleaseWaitAdScreen::class.java)
                .putExtra("admob", true))
        }
    }
    @JvmStatic
    fun hideNativeAndBanner(view: ViewGroup, activity: Activity) {
        val appPrefs = MyAppSharedPrefs(activity)
        if (appPrefs.isPremium) {
            view.visibility = View.GONE
        }
    }
}