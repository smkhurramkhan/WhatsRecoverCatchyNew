package com.catchyapps.whatsdelete.appactivities.activitysplash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.BaseApplication.Companion.loadInterstitial
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.appactivities.myapplanguage.ChangeLanguageActivity
import com.catchyapps.whatsdelete.appactivities.activityhome.MainActivity
import com.catchyapps.whatsdelete.databinding.SplashScreenBinding
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var appSharedPreferences: MyAppSharedPrefs? = null
    private lateinit var splashBinding: SplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        splashBinding = SplashScreenBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)


        appSharedPreferences = MyAppSharedPrefs(this)

        initImages()

        loadInterstitial()

        initVariables()

        runProgress()


    }

    private fun initImages() {
        Glide.with(this)
            .load(R.drawable.splash_iocn)
            .into(splashBinding.icon)
    }

    private fun initVariables() {
        lifecycleScope.launch {
            if (AppHelperDb.hGetAllFolders()?.isEmpty() == true) {
                val foldersEntity =
                    EntityFolders()
                foldersEntity.playlistName = "My Collections"
                foldersEntity.noOfItems = 0
                AppHelperDb.hInsertFolder(foldersEntity)
            }
        }
    }


    private fun runProgress() {
        Handler(mainLooper).postDelayed({
            /*  if (appSharedPreferences?.hGetFirstTime() == true) {
                  startActivity(
                      Intent(
                          this@SplashScreen,
                          OnboardingScreens::class.java
                      )
                  )
              }*/
            if (appSharedPreferences?.getFirstTimeLanguageSelected()!!.not()) {
                startActivity(
                    Intent(
                        this@SplashScreen, ChangeLanguageActivity::class.java
                    )
                        .putExtra("fromHome", false)
                )
            } else {
                startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            }
            finish()
        }, 2000)
    }
}