package com.catchyapps.whatsdelete.appactivities.activitystylishtext

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils.shareText
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils.shareToWhatsApp
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils
import com.catchyapps.whatsdelete.appactivities.activitystylishtext.textadapter.TextAdapter
import com.catchyapps.whatsdelete.appactivities.activitystylishtext.textdata.TextFont
import com.catchyapps.whatsdelete.databinding.ScreenStylishTextBinding

class StylishTextActivity : AppCompatActivity() {
    private lateinit var stylishTextBinding: ScreenStylishTextBinding
    private var textFontList = listOf<TextFont>()
    private var textAdapter: TextAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stylishTextBinding = ScreenStylishTextBinding.inflate(layoutInflater)
        setContentView(stylishTextBinding.root)

        initToolbar()
        loadAds()
        textFontList = AppStylishFontUtils.getFontsList()
        setUpAdapter()
        enterData()


    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(stylishTextBinding.topAdLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            stylishTextBinding.nativebanner,
            stylishTextBinding.shimmerViewContainer
        )
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initToolbar() {

        stylishTextBinding.toolbar.apply {
            toolbarTitle.text = getString(R.string.stylish_text)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener { startActivity(Intent(this@StylishTextActivity, ActivityPremium::class.java))
                finish()
            }

        }
    }

    private fun setUpAdapter() {
        stylishTextBinding.fontRecycler.layoutManager = LinearLayoutManager(this)
        textAdapter = TextAdapter(
            this,
            textFontList,
            onClick = { fontItem, action ->
                when (action) {
                    "copy" -> {
                        MyAppShareUtils.copyText(fontItem.previewText,this)
                    }

                    "whatsapp" -> {
                        shareToWhatsApp(fontItem.previewText,this)
                    }

                    "share" -> {
                        shareText(fontItem.previewText, getString(R.string.share),this)
                    }
                }

            }
        )
        stylishTextBinding.fontRecycler.adapter = textAdapter
    }

    private fun enterData() {
        stylishTextBinding.fontStylesEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textFontList.forEach {
                    if (s.isNullOrEmpty().not()) {
                        it.previewText = s.toString()
                    } else {
                        it.previewText = getString( R.string.app_name)
                    }
                }
                textAdapter?.setFontsData(textFontList)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }




}