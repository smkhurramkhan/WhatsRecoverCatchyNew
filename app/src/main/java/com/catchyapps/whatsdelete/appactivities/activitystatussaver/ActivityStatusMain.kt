package com.catchyapps.whatsdelete.appactivities.activitystatussaver

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.FragmentStatusesPager
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.databinding.ScreenStatusHomeBinding

class ActivityStatusMain : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var position = 0

    private val viewModel: VMStatusShared by viewModels()

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private lateinit var statusBinding: ScreenStatusHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBinding = ScreenStatusHomeBinding.inflate(layoutInflater)
        setContentView(statusBinding.root)

        position = intent.getIntExtra("tab", 0)

        initToolbar()

        initAds()

        viewModel.position.value = position

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.mainContent, FragmentStatusesPager())

        transaction.commit()
    }

    private fun initToolbar() {
        statusBinding.toolbar.apply {

            // toolbar.background = getDrawable(R.drawable.bottom_corner_round)
            toolbarTitle.text = getString(R.string.whatsapp_status)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(Intent(this@ActivityStatusMain, ActivityPremium::class.java))
            }
        }

    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(statusBinding.topAdLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            statusBinding.nativeContainer,
            statusBinding.shimmerViewContainer
        )
    }


}