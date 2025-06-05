package com.catchyapps.whatsdelete.appactivities.activityasciifaces

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityasciifaces.asciifacesadapter.AdapterAsciiFaces
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppDataUtils.getAsciiList
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils
import com.catchyapps.whatsdelete.databinding.ScreenAsciiBinding

class ActivityAsciiFaces : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    lateinit var binding: ScreenAsciiBinding
    private var asciiFilesList = getAsciiList()
    private lateinit var asciiFileAdapter: AdapterAsciiFaces
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenAsciiBinding.inflate(layoutInflater)
        setContentView(binding.root)


        asciiFileAdapter = AdapterAsciiFaces(this,
            asciiFilesList,
            onItemClick = { item, type ->
                when (type) {
                    "whatsapp" -> {
                        MyAppShareUtils.shareToWhatsApp(
                            message = item,
                            activity = this
                        )
                    }

                    "share" -> {
                        MyAppShareUtils.shareText(
                            text = item,
                            title = getString(R.string.share),
                            activity = this
                        )
                    }
                }
            }
        )

        binding.rvAscii.adapter = asciiFileAdapter
        binding.rvAscii.layoutManager = LinearLayoutManager(this)

        loadAds()
        initToolbar()

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(binding.topAdLayout, this)
        BaseApplication.showNativeBanner(
            binding.nativebanner,
            binding.shimmerViewContainer
        )
    }

    private fun initToolbar() {
        binding.toolbar.apply {
            toolbarTitle.text = getString(R.string.ascii_faces)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(
                    Intent(
                        this@ActivityAsciiFaces,
                        ActivityPremium::class.java
                    )
                )
            }

        }
    }
}