package com.catchyapps.whatsdelete.appactivities.myapplanguage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.AppLanguageUtils
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appactivities.myapplanguage.languageadapter.AdapterCountry
import com.catchyapps.whatsdelete.appactivities.activityhome.MainActivity
import com.catchyapps.whatsdelete.appactivities.activityonboarding.OnboardingScreens
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.databinding.ScreenChangeLanugageBinding

class ChangeLanguageActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var changeLanguageBinding: ScreenChangeLanugageBinding
    private var languageList = AppLanguageUtils.getLanguages()

    private var adapterCountry: AdapterCountry? = null

    private var myAppSharedPrefs: MyAppSharedPrefs? = null

    private var fromHome = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLanguageBinding = ScreenChangeLanugageBinding.inflate(layoutInflater)
        setContentView(changeLanguageBinding.root)

        myAppSharedPrefs = MyAppSharedPrefs(this)
        fromHome = intent.getBooleanExtra("fromHome", false)

        initToolbar()
        setupAdapter()

    }

    private fun initToolbar() {
        changeLanguageBinding.toolbar.apply {

            toolbar.background = getDrawable(R.drawable.bottom_corner_round)
            toolbarTitle.text = getString(R.string.change_language)

            btnback.visibility = View.GONE
            btnPremium.setOnClickListener {
                startActivity(Intent(this@ChangeLanguageActivity, ActivityPremium::class.java))
            finish()
            }

        }
    }

    private fun setupAdapter() {
        changeLanguageBinding.countriesRecycler.layoutManager = LinearLayoutManager(this)

        adapterCountry = AdapterCountry(
            this,
            languageList,
            onClick = { item ->
                myAppSharedPrefs?.setFirstTimeLanguageSelected(true)
                updateLocale(item.languageId)
                updateLanguage()
            }
        )

        changeLanguageBinding.countriesRecycler.adapter = adapterCountry
    }

    override fun onBackPressed() {
        if (fromHome) {
            finish()
        }
    }


    private fun updateLanguage() {
        if (fromHome) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finishAffinity()
        } else {
            val intent = Intent(this, OnboardingScreens::class.java)
            startActivity(intent)
            finish()
        }

    }


}