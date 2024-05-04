package com.catchyapps.whatsdelete.appactivities.activitystickers


import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.multidex.BuildConfig
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppDataUtils.loadStickersByCategory
import com.catchyapps.whatsdelete.databinding.ScreenStickersBinding
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activitystickers.fragments.AllStickersFragment
import com.catchyapps.whatsdelete.appactivities.activitystickers.fragments.BirthdayStickesFragment
import com.catchyapps.whatsdelete.appactivities.activitystickers.fragments.EidStickersFragment
import com.catchyapps.whatsdelete.appactivities.activitystickers.fragments.EmojiStickersFragment
import com.catchyapps.whatsdelete.appactivities.activitystickers.fragments.LoveStickerFragment
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickeradapter.AdapterSticker
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.ListModelStickers
import timber.log.Timber


class ActivityStickersScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var stickersBinding: ScreenStickersBinding
    private lateinit var stickerPackName: String
    private var stickersList = listOf<ListModelStickers>()

    val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"


    private val stickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val validationError = data.getStringExtra("validation_error")
                    if (validationError != null) {
                        if (BuildConfig.DEBUG) {
                            // Validation error should be shown to the developer only, not users.
                           Timber.d("validation error")
                        }
                        Timber.e( "Validation failed:$validationError")
                    }
                } else {
                    Timber.d("stickers pack not added")
                }

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stickersBinding = ScreenStickersBinding.inflate(layoutInflater)
        setContentView(stickersBinding.root)


        initToolbar()
        stickersList = loadStickersByCategory(assets)
        setUpStickersRV()
        setupViewPager()
        loadAds()

        stickersBinding.tabs.setupWithViewPager(stickersBinding.viewpager)
        stickersBinding.viewpager.offscreenPageLimit = 5


    }

    private fun addStickerPackToWhatsApp(sp: ListModelStickers) {
        val intent = Intent()
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK")

        intent.putExtra(EXTRA_STICKER_PACK_ID, "sp.category1")
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY,  packageName + ".provider")
        intent.putExtra(EXTRA_STICKER_PACK_NAME, sp.category)
        try {
            stickerLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show()
        }
    }

    private fun initToolbar() {
        stickersBinding.toolbar.apply {

            toolbar.background = getDrawable(R.drawable.bottom_corner_round)
            toolbarTitle.text = getString(R.string.stickers)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener { startActivity(Intent(this@ActivityStickersScreen, ActivityPremium::class.java))
                finish()
            }

        }
    }


    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(stickersBinding.topAdLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            stickersBinding.nativebanner,
            stickersBinding.shimmerViewContainer
        )
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(AllStickersFragment(), "All Stickers")
        adapter.addFragment(LoveStickerFragment(), "Love")
        adapter.addFragment(BirthdayStickesFragment(), "Birthday")
        adapter.addFragment(EidStickersFragment(), "Eid")
        adapter.addFragment(EmojiStickersFragment(), "Emoji")

        stickersBinding.viewpager.adapter = adapter
    }

    internal class ViewPagerAdapter(manager: FragmentManager?) : FragmentStatePagerAdapter(
        manager!!
    ) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()
        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    private fun setUpStickersRV() {

        val stickersAdapter = AdapterSticker(this,
            stickersList,
            onAddStickerClick = {stickerItem->
                addStickerPackToWhatsApp(
                    stickerItem
                )
            })

        stickersBinding.rvStickers.adapter = stickersAdapter
        stickersBinding.rvStickers.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


}