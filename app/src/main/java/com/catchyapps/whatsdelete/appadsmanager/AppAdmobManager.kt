package com.catchyapps.whatsdelete.appadsmanager

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.hide
import com.catchyapps.whatsdelete.basicapputils.show
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import timber.log.Timber

class AppAdmobManager(
    private val baseApplication: BaseApplication,
    var adPriority: Int
) {

    companion object {
        private const val TAG = "AppAdmobManager"
        private const val RETRY_DELAY_MS = 30_000L
        private const val MAX_RETRY_COUNT = 3

        @JvmStatic
        fun getPixelFromDp(application: Application, dp: Int): Int {
            val display =
                (application.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val scale = outMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }

    var interstitialAd: InterstitialAd? = null
        private set

    private var bannerAdView: AdView? = null
    private val adsFacebookManger = FBAdsManger(baseApplication, adPriority)
    private val retryHandler = Handler(Looper.getMainLooper())
    private var interstitialRetryCount = 0
    private var isLoadingInterstitial = false

    // ── Interstitial ──

    fun loadAdMobInterstitialAd() {
        if (MyAppDetectorConnection.isNotConnectedToInternet()) return

        if (isLoadingInterstitial) {
            Timber.tag(TAG).d("AdMob interstitial already loading, skipping duplicate request")
            return
        }

        isLoadingInterstitial = true
        Timber.tag(TAG).d("Loading AdMob interstitial (attempt %d/%d)",
            interstitialRetryCount + 1, MAX_RETRY_COUNT)

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            baseApplication,
            baseApplication.resources.getString(R.string.interstitial_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Timber.tag(TAG).d("AdMob interstitial loaded successfully")
                    interstitialAd = ad
                    ad.fullScreenContentCallback = fullScreenContentCallback
                    isLoadingInterstitial = false
                    interstitialRetryCount = 0
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.tag(TAG).w("AdMob interstitial load failed: code=%d msg=%s",
                        loadAdError.code, loadAdError.message)
                    interstitialAd = null
                    isLoadingInterstitial = false
                    scheduleRetryIfNeeded()
                }
            }
        )
    }

    private val fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Timber.tag(TAG).d("AdMob interstitial dismissed, reloading fresh ad")
            interstitialRetryCount = 0
            isLoadingInterstitial = false
            loadAdMobInterstitialAd()
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            Timber.tag(TAG).w("AdMob interstitial failed to show: %s", adError.message)
        }

        override fun onAdShowedFullScreenContent() {
            Timber.tag(TAG).d("AdMob interstitial shown")
            interstitialAd = null
        }
    }

    private fun scheduleRetryIfNeeded() {
        interstitialRetryCount++
        if (interstitialRetryCount < MAX_RETRY_COUNT) {
            Timber.tag(TAG).d("Scheduling AdMob interstitial retry in %dms (attempt %d/%d)",
                RETRY_DELAY_MS, interstitialRetryCount + 1, MAX_RETRY_COUNT)
            retryHandler.postDelayed({ loadAdMobInterstitialAd() }, RETRY_DELAY_MS)
        } else {
            Timber.tag(TAG).w("AdMob interstitial max retries (%d) reached, giving up", MAX_RETRY_COUNT)
            interstitialRetryCount = 0
        }
    }

    // ── Native Banner ──

    fun admobNativeBanner(mNativeAdContainer: ViewGroup?, alternateView: View?) {
        mNativeAdContainer?.layoutParams?.height = getPixelFromDp(baseApplication, 64)
        try {
            val adLoader = AdLoader.Builder(
                baseApplication,
                baseApplication.resources.getString(R.string.native_advanced)
            )
                .forNativeAd { nativeAd: NativeAd? ->
                    Timber.tag(TAG).d("AdMob native banner loaded")
                    val cd = ColorDrawable()
                    val styles = NativeTemplateStyle.Builder()
                        .withMainBackgroundColor(cd).build()
                    val layoutInflater =
                        baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    @SuppressLint("InflateParams")
                    val cardView = layoutInflater
                        .inflate(R.layout.custom_native_ad_view_layout, null) as LinearLayout

                    val templateView = cardView.findViewById<TemplateView>(R.id.my_template)
                    templateView.show()
                    templateView.setStyles(styles)
                    templateView.setNativeAd(nativeAd)

                    mNativeAdContainer?.removeAllViews()
                    mNativeAdContainer?.addView(cardView)
                    mNativeAdContainer?.show()
                    alternateView?.hide()
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        Timber.tag(TAG).w("AdMob native banner failed: code=%d msg=%s",
                            loadAdError.code, loadAdError.message)
                        if (adPriority == 1) {
                            adsFacebookManger.customNativeBanner(mNativeAdContainer, alternateView)
                        } else {
                            alternateView?.show()
                        }
                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "AdMob native banner exception")
        }
    }

    // ── Adaptive Banner ──

    private val adSize: AdSize
        get() {
            val display =
                (baseApplication.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)
            val widthPixels = outMetrics.widthPixels.toFloat()
            val density = outMetrics.density
            val adWidth = (widthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(baseApplication, adWidth)
        }

    fun adMobAdaptiveBanner(adContainerView: ViewGroup) {
        try {
            bannerAdView = AdView(baseApplication).apply {
                setAdUnitId(baseApplication.resources.getString(R.string.banner_ad_unit_id))
            }

            adContainerView.layoutParams.height = getPixelFromDp(baseApplication, 60)
            addPlaceHolderTextView(adContainerView)

            bannerAdView?.setAdSize(adSize)
            bannerAdView?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Timber.tag(TAG).d("AdMob adaptive banner loaded")
                    adContainerView.removeAllViews()
                    adContainerView.addView(bannerAdView)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Timber.tag(TAG).w("AdMob adaptive banner failed: %s", loadAdError.message)
                    adsFacebookManger.fbBannerAdView(adContainerView)
                }
            }

            bannerAdView?.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "AdMob adaptive banner exception")
        }
    }

    private fun addPlaceHolderTextView(adContainerView: ViewGroup) {
        val valueTV = TextView(baseApplication).apply {
            setText(R.string.ad_loading)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
        }
        adContainerView.addView(valueTV)
    }

    // ── Native Ad (Full) ──

    fun customNativeAd(nativeAdLayout: ViewGroup?, itemPicker: View?) {
        if (itemPicker == null) {
            nativeAdLayout?.layoutParams?.height = getPixelFromDp(baseApplication, 240)
        }

        @SuppressLint("InflateParams")
        val adLoader = AdLoader.Builder(
            baseApplication,
            baseApplication.resources.getString(R.string.native_advanced)
        )
            .forNativeAd { unifiedNativeAd: NativeAd? ->
                Timber.tag(TAG).d("AdMob native ad loaded")
                val cd = ColorDrawable()
                val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(cd).build()
                val layoutInflater =
                    baseApplication.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val cardView =
                    layoutInflater.inflate(R.layout.layout_native_ad_, null) as FrameLayout
                val templateView = cardView.findViewById<TemplateView>(R.id.my_template)
                templateView.show()
                templateView.setStyles(styles)
                templateView.setNativeAd(unifiedNativeAd)
                if (nativeAdLayout != null) {
                    nativeAdLayout.removeAllViews()
                    nativeAdLayout.addView(cardView)
                    nativeAdLayout.show()
                    itemPicker?.hide()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Timber.tag(TAG).w("AdMob native ad failed: code=%d msg=%s",
                        loadAdError.code, loadAdError.message)
                    if (adPriority == 1) {
                        adsFacebookManger.showFbNativeAds(nativeAdLayout, itemPicker)
                    } else {
                        itemPicker?.show()
                    }
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }
}
