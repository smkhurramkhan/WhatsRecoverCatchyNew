package com.catchyapps.whatsdelete.appadsmanager

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs

object ShowInterstitial {

    /** Show FB-only interstitial via the "please wait" screen. */
    @JvmStatic
    fun showInter(activity: Activity) {
        if (!BaseApplication.isFbInterstitialAvailable()) return
        activity.startActivity(
            Intent(activity, PleaseWaitAdScreen::class.java)
                .putExtra(PleaseWaitAdScreen.EXTRA_AD_TYPE, PleaseWaitAdScreen.AD_TYPE_FB)
        )
    }

    /** Show priority-based interstitial (AdMob + FB fallback) via the "please wait" screen. */
    @JvmStatic
    fun showAdmobInter(activity: Activity) {
        if (!BaseApplication.isInterstitialAvailable()) return
        activity.startActivity(
            Intent(activity, PleaseWaitAdScreen::class.java)
                .putExtra(PleaseWaitAdScreen.EXTRA_AD_TYPE, PleaseWaitAdScreen.AD_TYPE_ADMOB)
        )
    }

    /** Hide native/banner container if user is premium. */
    @JvmStatic
    fun hideNativeAndBanner(view: ViewGroup, activity: Activity) {
        if (MyAppSharedPrefs(activity).isPremium) {
            view.visibility = View.GONE
        }
    }
}
