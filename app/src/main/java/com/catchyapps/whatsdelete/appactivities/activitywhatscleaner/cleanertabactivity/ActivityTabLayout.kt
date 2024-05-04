package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanertabactivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.databinding.TabsItemBinding
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.CleanerAdapterTabs
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ActivityTabLayout : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var hItemTabsBinding: TabsItemBinding
    private lateinit var title: String

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hItemTabsBinding = TabsItemBinding.inflate(layoutInflater)
        setContentView(hItemTabsBinding.root)

        intentExtra()

        initAds()

        setupToolbar()


        hSetPagerAdapter()


    }

    private fun intentExtra() {
        title = intent.extras?.getString(CleanerConstans.H_CLEANER_IC).toString()
    }

    private fun hGetZeroTitle(title: String): CharSequence {
        return when (title) {
            CleanerConstans.H_IMAGE -> {
                getString(R.string.received_images)

            }
            CleanerConstans.H_VIDEOS -> {
                getString(R.string.received_videos)

            }
            CleanerConstans.H_DOCUMENTS -> {
                getString(R.string.received_documents)

            }
            CleanerConstans.H_AUDIO -> {
                getString(R.string.received_audio)

            }
            CleanerConstans.H_VOICE -> {
                getString(R.string.received_voice)

            }
            CleanerConstans.H_WALLPAPERS -> {
                getString(R.string.received_wallpapers)

            }
            CleanerConstans.H_GIFS -> {
                getString(R.string.received_gifs)

            }

            else -> ""
        }
    }


    private fun hGet1stTitle(title: String): CharSequence {
        return when (title) {
            CleanerConstans.H_IMAGE -> {
                getString(R.string.sent_images)
            }
            CleanerConstans.H_VIDEOS -> {
                getString(R.string.sent_videos)
            }
            CleanerConstans.H_DOCUMENTS -> {
                getString(R.string.sent_documents)
            }
            CleanerConstans.H_AUDIO -> {
                getString(R.string.sent_audio)
            }
            CleanerConstans.H_VOICE -> {
                getString(R.string.sent_voice)
            }
            CleanerConstans.H_WALLPAPERS -> {
                getString(R.string.sent_wallpapers)
            }
            CleanerConstans.H_GIFS -> {
                getString(R.string.sent_gifs)
            }

            else -> ""
        }

    }


    private fun hSetPagerAdapter() {

        val hTabsAdapter = CleanerAdapterTabs(
            supportFragmentManager,
            lifecycle
        )

        hItemTabsBinding.viewpager.adapter = hTabsAdapter

        hItemTabsBinding.apply {
            viewpager.adapter = hTabsAdapter

            val tabLayout: TabLayout = tabs
            TabLayoutMediator(
                tabLayout,
                viewpager
            ) { tab: TabLayout.Tab, position: Int ->

                when (position) {

                    0 -> {
                        tab.text = hGetZeroTitle(title)
                    }
                    1 -> {
                        tab.text = hGet1stTitle(title)
                    }

                }

            }.attach()
            hItemTabsBinding.viewpager.currentItem = 0
        }
    }


    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.whatsapp_cleaner)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(hItemTabsBinding.topAdLayout, this)

        BaseApplication.showNativeBanner(
            hItemTabsBinding.nativebanner,
            hItemTabsBinding.shimmerViewContainer
        )

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cleaner_home, menu)
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_home) {
            val intent = Intent()
            intent.putExtra("showAd", false)
            intent.putExtra("home", true)
            setResult(RESULT_OK, intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroy() {
        super.onDestroy()
        val hSharedPreferences = MyAppSharedPrefs(this)
        hSharedPreferences.clearDetailItem()

    }
}