package com.catchyapps.whatsdelete.appadsmanager

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R

class PleaseWaitAdScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_ad_please_wait)

        val isAdmob = intent.getBooleanExtra("admob", false)

        if (isAdmob) {
            Handler(Looper.getMainLooper()).postDelayed({
                BaseApplication.showInterstitialAdmobOnly(this)
                finish();


            }, 1000)
        } else {

            Handler(Looper.getMainLooper()).postDelayed({
                BaseApplication.showInterstitial(this)
                finish();


            }, 1000)
        }
    }

    override fun onBackPressed() {
    }
}