package com.catchyapps.whatsdelete

import android.app.Activity
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import com.catchyapps.whatsdelete.appadsmanager.AppAdmobManager
import com.catchyapps.whatsdelete.appadsmanager.MyAppDetectorConnection
import com.catchyapps.whatsdelete.appadsmanager.FBAdsManger
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.BuildConfig
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
        isPremium = appSharedPrefs.isPremium

        MobileAds.initialize(this) {/* initializationStatus: InitializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
            }*/
        }


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

        @JvmStatic
        fun loadInterstitial() {
            adMobManager?.loadAdMobInterstitialAd()
            adsFacebookManger?.loadFbInterstitial()
        }

        @JvmStatic
        fun isFbInterstitialAvailable(): Boolean {
            if (isPremium) return false
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return false

            if (adsFacebookManger?.fbInterstitialAd != null && adsFacebookManger?.fbInterstitialAd?.isAdLoaded!!) {
                return true
            } else adsFacebookManger?.loadFbInterstitial()

            return false
        }

        @JvmStatic
        fun isInterstitialAvailable(): Boolean {
            if (isPremium) return false
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return false
            when (adPriority) {
                1 -> {
                    if (adMobManager?.interstitialAd != null) {
                        return true
                    } else adMobManager?.loadAdMobInterstitialAd()
                    if (adsFacebookManger?.fbInterstitialAd != null
                        && adsFacebookManger?.fbInterstitialAd?.isAdLoaded!!
                    ) {
                        return false
                    } else adsFacebookManger?.loadFbInterstitial()

                }
                2 -> {
                    if (adsFacebookManger?.fbInterstitialAd != null && adsFacebookManger?.fbInterstitialAd?.isAdLoaded!!) {
                        return true
                    } else adsFacebookManger?.loadFbInterstitial()

                    if (adMobManager?.interstitialAd != null) {
                        return true
                    } else adMobManager?.loadAdMobInterstitialAd()

                }
            }
            return false
        }

        //ads
        @JvmStatic
        fun showInterstitial(activity: Activity?) {
            if (isPremium) return

            if (MyAppDetectorConnection.isNotConnectedToInternet()) return
            // Facebook Inter
            if (adsFacebookManger?.fbInterstitialAd != null && adsFacebookManger?.fbInterstitialAd!!.isAdLoaded) {
                adsFacebookManger?.fbInterstitialAd!!.show()
                return
            } else adsFacebookManger?.loadFbInterstitial()


        }

        @JvmStatic
        fun showInterstitialAdmobOnly(activity: Activity?) {
            if (isPremium) return

            if (MyAppDetectorConnection.isNotConnectedToInternet()) return
            when (adPriority) {
                1 -> {
                    // Admob Inter
                    if (adMobManager?.interstitialAd != null) {
                        adMobManager?.interstitialAd!!.show(activity!!)
                        return
                    } else adMobManager?.loadAdMobInterstitialAd()

                    // Facebook Inter
                    if (adsFacebookManger?.fbInterstitialAd != null && adsFacebookManger?.fbInterstitialAd!!.isAdLoaded) {
                        adsFacebookManger?.fbInterstitialAd!!.show()
                        return
                    } else adsFacebookManger?.loadFbInterstitial()


                }
                2 -> {
                    // Facebook Inter
                    if (adsFacebookManger?.fbInterstitialAd != null && adsFacebookManger?.fbInterstitialAd!!.isAdLoaded) {
                        adsFacebookManger?.fbInterstitialAd!!.show()
                        return
                    } else adsFacebookManger?.loadFbInterstitial()


                    // Admob Inter
                    if (adMobManager?.interstitialAd != null) {
                        adMobManager?.interstitialAd!!.show(activity!!)
                        return
                    } else adMobManager?.loadAdMobInterstitialAd()


                }
            }
        }


        @JvmStatic
        fun showBanner(bannerAdContainer: FrameLayout?) {
            if (isPremium) return
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return

            when (adPriority) {
                1, 2 ->
                    adsFacebookManger?.fbBannerAdView(bannerAdContainer)

            }
        }


        @JvmStatic
        fun showNativeBanner(bannerAdContainer: ViewGroup?, alternateView: View?) {
            if (isPremium) return
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return
            when (adPriority) {
                1,2 -> adsFacebookManger?.fbNativeBanner(bannerAdContainer)
            }
        }

        @JvmStatic
        fun showNativeBannerAdmobOnly(bannerAdContainer: ViewGroup?, alternateView: View?) {
            if (isPremium) return
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return

            when (adPriority) {
                1,2 -> adMobManager?.admobNativeBanner(bannerAdContainer, alternateView)
            }
        }

        @JvmStatic
        fun showDefaultNativeAd(bannerAdContainer: FrameLayout?, alternateView: View?) {
            if (isPremium) return
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return
            when (adPriority) {
                1, 2 -> adsFacebookManger?.showFbNativeAds(bannerAdContainer, alternateView)

            }
        }


        @JvmStatic
        fun showDefaultNativeAdAdmobOnly(bannerAdContainer: FrameLayout?, alternateView: View?) {
            if (isPremium) return
            if (MyAppDetectorConnection.isNotConnectedToInternet()) return
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
