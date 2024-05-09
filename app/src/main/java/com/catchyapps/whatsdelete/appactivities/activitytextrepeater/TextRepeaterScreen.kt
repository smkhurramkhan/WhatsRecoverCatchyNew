package com.catchyapps.whatsdelete.appactivities.activitytextrepeater

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.databinding.ScreenTextRepeaterBinding

class TextRepeaterScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var textRepeaterBinding: ScreenTextRepeaterBinding
    private var textRepeatCount = 0
    private var newLine  = false
    private var addSpace  = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textRepeaterBinding = ScreenTextRepeaterBinding.inflate(layoutInflater)
        setContentView(textRepeaterBinding.root)

        loadAds()
        initToolbar()

     // newLine  =   textRepeaterBinding.newLine.isChecked == true
        onClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun onClickListeners() {

      //  newLine = textRepeaterBinding.newLine.isChecked

        textRepeaterBinding.btnRepeatText.setOnClickListener {
            if (textRepeaterBinding.etNumberText.text.isNullOrEmpty().not()
                && textRepeaterBinding.etNumberText.text.toString().toInt() < 1000) {
                if (textRepeaterBinding.etmessageText.text.isNullOrEmpty().not()) {
                   // newLine = textRepeaterBinding.switchForNewLine.isChecked
                  //  newLine = textRepeaterBinding.newLine.isChecked
                  //  addSpace = textRepeaterBinding.space.isChecked

                    textRepeatCount = textRepeaterBinding.etNumberText.text.toString().toInt()
                    repeatText(textRepeaterBinding.newLine.isChecked, textRepeaterBinding.space.isChecked, textRepeaterBinding.etmessageText.text.toString(),textRepeatCount)

                } else {
                    Toast.makeText(
                        this, getString(R.string.please_enter_the_text_you_want_to_repeat),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_enter_the_number_of_times_you_want_to_repeat_the_text),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        textRepeaterBinding.btnClear.setOnClickListener {
            clearData()
        }

        textRepeaterBinding.btnCopy.setOnClickListener {
           if(textRepeaterBinding.etRepeatedText.text.toString().isNotEmpty()){
               copyToClipboard(textRepeaterBinding.etRepeatedText.text.toString())
           }
        }

        textRepeaterBinding.btnShare.setOnClickListener {
            if(textRepeaterBinding.etRepeatedText.text.toString().isNotEmpty()){
                shareText(textRepeaterBinding.etRepeatedText.text.toString(),getString(R.string.share))
            }
        }
    }

    private fun clearData() {
        textRepeaterBinding.etRepeatedText.text = ""
        textRepeaterBinding.etmessageText.setText("")
        textRepeaterBinding.etNumberText.setText("")
    }

    private fun repeatText(newLine: Boolean,addspace: Boolean, text: String, repeatCount: Int) {
        val repeatedText = buildString {
            repeat(repeatCount) {
                append(text)
                if (newLine && it != repeatCount - 1) {
                    append("\n")
                }
                else if (addspace && it != repeatCount - 1){
                    append(" ")
                }
            }
        }
        textRepeaterBinding.etRepeatedText.text = repeatedText
    }


    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(textRepeaterBinding.topAdLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            textRepeaterBinding.nativebanner,
            textRepeaterBinding.shimmerViewContainer
        )
    }

    private fun initToolbar() {
        textRepeaterBinding.toolbar.apply {

            toolbarTitle.text = getString(R.string.text_repeater)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(Intent(this@TextRepeaterScreen, ActivityPremium::class.java))
            }

        }
    }

    private fun copyToClipboard( text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.text_copied),Toast.LENGTH_LONG).show()
    }
    private fun shareText(text: String, title: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        startActivity(shareIntent)
    }

}