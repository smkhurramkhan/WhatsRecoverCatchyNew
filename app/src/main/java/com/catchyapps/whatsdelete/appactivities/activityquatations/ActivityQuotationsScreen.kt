package com.catchyapps.whatsdelete.appactivities.activityquatations

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityquatations.quatationsadapter.AdapterQuotations
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppDataUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils.shareText
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils.shareToWhatsApp
import com.catchyapps.whatsdelete.databinding.ScreenQuatationsBinding

class ActivityQuotationsScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var quotationsBinding: ScreenQuatationsBinding
    private var quotesList = MyAppDataUtils.setQuotes()
    private var adapterQuotations: AdapterQuotations? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quotationsBinding = ScreenQuatationsBinding.inflate(layoutInflater)
        setContentView(quotationsBinding.root)

        setUpAdapter()
        loadAds()
        initToolbar()

        quotationsBinding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                adapterQuotations?.filter(s.toString())
                //  adapterQuotations?.notifyDataSetChanged()
            }
        })

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(quotationsBinding.topAdLayout, this)
        BaseApplication.showNativeBanner(
            quotationsBinding.nativebanner,
            quotationsBinding.shimmerViewContainer
        )
    }

    private fun initToolbar() {
        quotationsBinding.toolbar.apply {
            toolbar.background = ContextCompat.getDrawable(
                this@ActivityQuotationsScreen,
                R.drawable.bottom_corner_round
            )
            toolbarTitle.text = getString(R.string.text_to_emoji)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(
                    Intent(
                        this@ActivityQuotationsScreen,
                        ActivityPremium::class.java
                    )
                )
            }

        }
    }


    private fun setUpAdapter() {
        quotationsBinding.rvQuatations.layoutManager = LinearLayoutManager(this)
        adapterQuotations = AdapterQuotations(
            quotesList,
            onclick = { quoteItem, action ->
                when (action) {
                    "copy" -> {
                        MyAppShareUtils.copyText(
                            text = quoteItem.quotes,
                            activity = this
                        )
                    }

                    "whatsapp" -> {
                        shareToWhatsApp(
                            message = quoteItem.quotes,
                            activity = this
                        )
                    }

                    "share" -> {
                        shareText(
                            text = quoteItem.quotes,
                            title = getString(R.string.share),
                            activity = this
                        )
                    }
                }

            }
        )
        quotationsBinding.rvQuatations.adapter = adapterQuotations
    }


}