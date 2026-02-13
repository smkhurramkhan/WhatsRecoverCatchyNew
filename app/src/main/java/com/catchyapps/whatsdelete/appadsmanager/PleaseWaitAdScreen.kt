package com.catchyapps.whatsdelete.appadsmanager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import timber.log.Timber

/**
 * Transient "Please Wait" screen shown while an interstitial ad is about to display.
 *
 * Re-checks ad availability before showing to avoid the race condition where
 * the ad expires or gets consumed between the initial check and the show call.
 */
class PleaseWaitAdScreen : AppCompatActivity() {

    companion object {
        const val EXTRA_AD_TYPE = "ad_type"
        const val AD_TYPE_FB = "fb"
        const val AD_TYPE_ADMOB = "admob"
        private const val TAG = "PleaseWaitAdScreen"
        private const val SHOW_DELAY_MS = 1000L
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_ad_please_wait)

        val adType = intent.getStringExtra(EXTRA_AD_TYPE) ?: AD_TYPE_FB

        handler.postDelayed({
            showAdAndFinish(adType)
        }, SHOW_DELAY_MS)
    }

    private fun showAdAndFinish(adType: String) {
        when (adType) {
            AD_TYPE_ADMOB -> {
                if (BaseApplication.isInterstitialAvailable()) {
                    Timber.tag(TAG).d("Showing priority-based interstitial")
                    BaseApplication.showInterstitialAdmobOnly(this)
                } else {
                    Timber.tag(TAG).w("Interstitial no longer available, closing wait screen")
                }
            }
            AD_TYPE_FB -> {
                if (BaseApplication.isFbInterstitialAvailable()) {
                    Timber.tag(TAG).d("Showing FB interstitial")
                    BaseApplication.showInterstitial(this)
                } else {
                    Timber.tag(TAG).w("FB interstitial no longer available, closing wait screen")
                }
            }
        }
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back press while waiting for ad
    }
}
