package com.catchyapps.whatsdelete.appactivities.activitytexttoemoji

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.basicapputils.AppTextToEmojiHelper.textToEmojiConverter
import com.catchyapps.whatsdelete.basicapputils.MyAppShareUtils
import com.catchyapps.whatsdelete.databinding.ScreenTextToEmojiBinding


class TextToEmojiScreen : BaseActivity() {
    private lateinit var textToEmojiBinding: ScreenTextToEmojiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textToEmojiBinding = ScreenTextToEmojiBinding.inflate(layoutInflater)
        setContentView(textToEmojiBinding.root)


        val unicode = 0x1F60A
        textToEmojiBinding.etEmoji.setText(getEmoji(unicode))

        textToEmoji()
        initToolbar()

        textToEmojiBinding.btnCopy.setOnClickListener {
            if (textToEmojiBinding.tvTextToEmoji.text.toString().isNotEmpty()) {
                MyAppShareUtils.copyText(textToEmojiBinding.tvTextToEmoji.text.toString(), this)
            } else {
                Toast.makeText(
                    this@TextToEmojiScreen, getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        textToEmojiBinding.btnShare.setOnClickListener {
            if (textToEmojiBinding.tvTextToEmoji.text.toString().isNotEmpty()) {
                MyAppShareUtils.shareText(
                    text = textToEmojiBinding.tvTextToEmoji.text.toString(),
                    title = getString(R.string.share),
                    activity = this
                )
            } else {
                Toast.makeText(
                    this@TextToEmojiScreen, getString(R.string.something_went_wrong),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun getEmoji(unicode: Int): String {
        return String(Character.toChars(unicode))
    }

    private fun textToEmoji() {
        textToEmojiBinding.tvTextToEmoji.movementMethod = ScrollingMovementMethod()

        textToEmojiBinding.etTextToEmoji.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (textToEmojiBinding.etTextToEmoji.text.toString().isNotEmpty()) {
                    val textToEmoji = s?.toString()?.uppercase() ?: ""
                    val emoji = textToEmojiBinding.etEmoji.text.toString()
                    val textWithEmoji = textToEmojiConverter(textToEmoji, emoji)
                    textToEmojiBinding.tvTextToEmoji.text = textWithEmoji
                }


            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }

    private fun initToolbar() {
        textToEmojiBinding.toolbar.apply {
            toolbarTitle.text = getString(R.string.text_to_emoji)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(
                    Intent(
                        this@TextToEmojiScreen,
                        ActivityPremium::class.java
                    )
                )
            }

        }
    }

}