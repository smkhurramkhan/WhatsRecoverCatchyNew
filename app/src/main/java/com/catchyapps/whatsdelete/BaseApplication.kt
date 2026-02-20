package com.catchyapps.whatsdelete

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import com.catchyapps.whatsdelete.appadsmanager.AppAdmobManager
import com.catchyapps.whatsdelete.appadsmanager.FBAdsManger
import com.catchyapps.whatsdelete.appadsmanager.MyAppDetectorConnection
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appnotifications.ServiceRestartWorker
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.google.android.gms.ads.MobileAds
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import timber.log.Timber


class BaseApplication : LocaleAwareApplication() {

    fun getContext(): BaseApplication? {
        return instance?.getContext()
    }


    override fun onCreate() {
        super.onCreate()
        // turnOnStrictMode()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        initTimber()

        AppHelperDb.initializeDb(this)


        instance = this
        val appSharedPrefs = MyAppSharedPrefs(this)
        isPremium = appSharedPrefs.isPremium == true

        MobileAds.initialize(this)


        adsFacebookManger =
            FBAdsManger(
                this,
                adPriority
            )
        adMobManager =
            AppAdmobManager(
                this,
                adPriority
            )

        // Schedule periodic worker to keep the notification listener service alive
        ServiceRestartWorker.schedule(this)
    }


    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    super.log(priority, String.format(MyAppConstants.hTag, tag), message, t)
                }
            })
        }
    }


    companion object {
        @JvmStatic
        var instance: BaseApplication? = null
            private set
        var adsFacebookManger: FBAdsManger? = null
        var adMobManager: AppAdmobManager? = null
        var adPriority = 1
        private var isPremium = false

        // ── Helper functions ──

        private fun canShowAds(): Boolean =
            !isPremium && !MyAppDetectorConnection.isNotConnectedToInternet()

        private fun isFbAdLoaded(): Boolean =
            adsFacebookManger?.fbInterstitialAd?.isAdLoaded == true

        private fun isAdmobAdLoaded(): Boolean =
            adMobManager?.interstitialAd != null

        private fun reloadFbIfNeeded() {
            if (!isFbAdLoaded()) adsFacebookManger?.loadFbInterstitial()
        }

        private fun reloadAdmobIfNeeded() {
            if (!isAdmobAdLoaded()) adMobManager?.loadAdMobInterstitialAd()
        }

        // ── Interstitial: Load ──

        @JvmStatic
        fun loadInterstitial() {
            if (isPremium) return
            adMobManager?.loadAdMobInterstitialAd()
            adsFacebookManger?.loadFbInterstitial()
        }

        // ── Interstitial: Availability checks ──

        @JvmStatic
        fun isFbInterstitialAvailable(): Boolean {
            if (!canShowAds()) return false
            if (isFbAdLoaded()) return true
            reloadFbIfNeeded()
            return false
        }

        @JvmStatic
        fun isInterstitialAvailable(): Boolean {
            if (!canShowAds()) return false
            return when (adPriority) {
                1 -> checkAdmobThenFb()
                2 -> checkFbThenAdmob()
                else -> false
            }
        }

        private fun checkAdmobThenFb(): Boolean {
            if (isAdmobAdLoaded()) return true
            reloadAdmobIfNeeded()
            if (isFbAdLoaded()) return true
            reloadFbIfNeeded()
            return false
        }

        private fun checkFbThenAdmob(): Boolean {
            if (isFbAdLoaded()) return true
            reloadFbIfNeeded()
            if (isAdmobAdLoaded()) return true
            reloadAdmobIfNeeded()
            return false
        }

        // ── Interstitial: Show ──

        /** Show FB interstitial only (intentional - no AdMob fallback) */
        @JvmStatic
        fun showInterstitial(activity: Activity?) {
            if (!canShowAds()) return
            if (isFbAdLoaded()) {
                adsFacebookManger?.fbInterstitialAd?.show()
            } else {
                reloadFbIfNeeded()
            }
        }

        /** Show interstitial with priority-based fallback (AdMob + FB) */
        @JvmStatic
        fun showInterstitialAdmobOnly(activity: Activity?) {
            if (!canShowAds()) return
            when (adPriority) {
                1 -> showAdmobThenFb(activity)
                2 -> showFbThenAdmob(activity)
            }
        }

        private fun showAdmobThenFb(activity: Activity?) {
            if (isAdmobAdLoaded() && activity != null) {
                adMobManager?.interstitialAd?.show(activity)
                return
            }
            reloadAdmobIfNeeded()

            if (isFbAdLoaded()) {
                adsFacebookManger?.fbInterstitialAd?.show()
                return
            }
            reloadFbIfNeeded()
        }

        private fun showFbThenAdmob(activity: Activity?) {
            if (isFbAdLoaded()) {
                adsFacebookManger?.fbInterstitialAd?.show()
                return
            }
            reloadFbIfNeeded()

            if (isAdmobAdLoaded() && activity != null) {
                adMobManager?.interstitialAd?.show(activity)
                return
            }
            reloadAdmobIfNeeded()
        }

        // ── Banner ads ──

        @JvmStatic
        fun showBanner(bannerAdContainer: FrameLayout?) {
            if (!canShowAds()) return
            adsFacebookManger?.fbBannerAdView(bannerAdContainer)
        }

        @JvmStatic
        fun showNativeBanner(bannerAdContainer: ViewGroup?, alternateView: View?) {
            if (!canShowAds()) return
            adsFacebookManger?.fbNativeBanner(bannerAdContainer)
        }

        @JvmStatic
        fun showNativeBannerAdmobOnly(bannerAdContainer: ViewGroup?, alternateView: View?) {
            if (!canShowAds()) return
            adMobManager?.admobNativeBanner(bannerAdContainer, alternateView)
        }

        // ── Native ads ──

        @JvmStatic
        fun showDefaultNativeAd(bannerAdContainer: FrameLayout?, alternateView: View?) {
            if (!canShowAds()) return
            adsFacebookManger?.showFbNativeAds(bannerAdContainer, alternateView)
        }

        @JvmStatic
        fun showDefaultNativeAdAdmobOnly(bannerAdContainer: FrameLayout?, alternateView: View?) {
            if (!canShowAds()) return
            when (adPriority) {
                1 -> adMobManager?.customNativeAd(bannerAdContainer, alternateView)
                2 -> adsFacebookManger?.showFbNativeAds(bannerAdContainer, alternateView)
            }
        }

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }
}
