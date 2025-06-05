package com.catchyapps.whatsdelete.appactivities.activityfavourite

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.catchyapps.whatsdelete.BaseApplication.Companion.showNativeBanner
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments.FavDocsFragment
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments.FavImagesMediaFragment
import com.catchyapps.whatsdelete.appclasseshelpers.TabsCollectionsVP
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments.FavAudiosMediaFragment
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments.FavVideosMediaFragment
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments.FavVoiceMediaFragment
import com.catchyapps.whatsdelete.databinding.ScreenFavoriteBinding

class ActivityFavorite : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var tabsViewPagerCollections: TabsCollectionsVP? = null
    var fragment: Fragment? = null
    private lateinit var binding: ScreenFavoriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAds()
        initToolbar()

        tabsViewPagerCollections = TabsCollectionsVP(supportFragmentManager)
        fragment = FavImagesMediaFragment()
        tabsViewPagerCollections?.addFragment(fragment!!, "Images")
        fragment = FavVideosMediaFragment()
        tabsViewPagerCollections?.addFragment(fragment!!, "Videos")
        fragment = FavDocsFragment()
        tabsViewPagerCollections?.addFragment(fragment!!, "Documents")
        fragment = FavAudiosMediaFragment()
        tabsViewPagerCollections?.addFragment(fragment!!, "Audios")
        fragment = FavVoiceMediaFragment()
        tabsViewPagerCollections?.addFragment(fragment!!, "Voice")
        binding.viewpager.adapter = tabsViewPagerCollections
        binding.tabs.setupWithViewPager(binding.viewpager)
        binding.viewpager.currentItem = 0
    }

    private fun initToolbar() {
        binding.toolbar.title = getString(R.string.favourites)
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(binding.topAdLayout, this)
        showNativeBanner(binding.nativeContainer, binding.shimmerViewContainer)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}