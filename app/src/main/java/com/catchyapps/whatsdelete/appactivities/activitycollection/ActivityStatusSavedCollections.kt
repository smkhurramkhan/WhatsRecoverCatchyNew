package com.catchyapps.whatsdelete.appactivities.activitycollection

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.RecoverTabsPagerAdapter
import com.catchyapps.whatsdelete.databinding.ScreenCollectionsSavedBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ActivityStatusSavedCollections : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var activitySavedBinding: ScreenCollectionsSavedBinding
    private lateinit var tabsPagerAdapter: RecoverTabsPagerAdapter
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activitySavedBinding = ScreenCollectionsSavedBinding.inflate(layoutInflater)
        setContentView(activitySavedBinding.root)

        initToolbar()
        initAds()
        hInitPager()
    }

    private fun hInitPager() {

        tabsPagerAdapter = RecoverTabsPagerAdapter(supportFragmentManager, lifecycle)

        val hPagerFragmentList = mutableListOf<Fragment>().also {
            it.add(StatusSavedCollectionFragment())
            it.add(ScreenshotsSavedFragment())
        }
        val hFragmentNameList = mutableListOf<String>().also {
            it.add(getString(R.string.saved_status))
            it.add(getString(R.string.screenshots))
        }

        tabsPagerAdapter.hSetFragmentList(hPagerFragmentList)

        activitySavedBinding.apply {
            viewpager.adapter = tabsPagerAdapter

            val tabLayout: TabLayout = tabs
            TabLayoutMediator(
                tabLayout,
                viewpager
            ) { tab: TabLayout.Tab, position: Int ->
                tab.text = hFragmentNameList[position]
            }.attach()
            activitySavedBinding.viewpager.currentItem = 0
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.saved_collections)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(activitySavedBinding.topAdLayout, this)
        ShowInterstitial.hideNativeAndBanner(activitySavedBinding.bannercontainer, this)

        BaseApplication.showNativeBanner(
            activitySavedBinding.nativeContainer,
            activitySavedBinding.shimmerViewContainer
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_status_main, menu)
        val mSearch = menu.findItem(R.id.action_help)
        val mWhatsApp = menu.findItem(R.id.action_whatsapp)
        mSearch.isVisible = false
        mWhatsApp.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_prem) {
            val intent = Intent(this@ActivityStatusSavedCollections, ActivityPremium::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}