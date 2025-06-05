package com.catchyapps.whatsdelete.appactivities.activitydirectchat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.catchyapps.whatsdelete.BaseApplication.Companion.showNativeBanner
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.countrytfragment.FragmentCountryCodeSelection.Companion.newInstance
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.namesetlistener.SetMyName
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.databinding.ScreenDirectScreenChatBinding
import java.util.Locale
import java.util.Objects

class DirectChatScreenActivity : BaseActivity(),
    SetMyName {
    private var number: String? = null
    var message: String? = null
    private lateinit var directChatBinding: ScreenDirectScreenChatBinding

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        directChatBinding = ScreenDirectScreenChatBinding.inflate(layoutInflater)
        setContentView(directChatBinding.root)
        initToolbar()
        initAds()
        initClickListeners()
    }


    private fun initClickListeners() {
        directChatBinding.tvcountrycode.setOnClickListener {
            val fragment = newInstance()
            fragment.show(supportFragmentManager, "dialog")
        }

        directChatBinding.btnSend.setOnClickListener {
            when {
                directChatBinding.etnumber.text.toString().isEmpty() -> {
                    directChatBinding.etnumber.error = getString(R.string.enter_number)
                }

                directChatBinding.tvcountrycode.text.toString().isEmpty() -> {
                    directChatBinding.tvcountrycode.error = getString(R.string.select_country_code)
                }

                Objects.requireNonNull(directChatBinding.etmessage.text).toString().isEmpty() -> {
                    directChatBinding.etmessage.error = getString(R.string.enter_message)
                }

                else -> {
                    message = directChatBinding.etmessage.text.toString()
                    number =
                        directChatBinding.tvcountrycode.text.toString() + directChatBinding.etnumber.text.toString()
                    openWhatsApp(number!!, message!!)
                }
            }
        }

        directChatBinding.opendirectChat.setOnClickListener {
            when {
                directChatBinding.etnumber.text.toString().isEmpty() -> {
                    directChatBinding.etnumber.error = getString(R.string.enter_number)
                }

                directChatBinding.tvcountrycode.text.toString().isEmpty() -> {
                    directChatBinding.tvcountrycode.error = getString(R.string.select_country_code)
                }

                Objects.requireNonNull(directChatBinding.etmessage.text).toString().isEmpty() -> {
                    directChatBinding.etmessage.error = getString(R.string.enter_message)
                }

                else -> {
                    message = directChatBinding.etmessage.text.toString()
                    number =
                        directChatBinding.tvcountrycode.text.toString() + directChatBinding.etnumber.text.toString()
                    openWhatsApp(number!!, message!!)
                }
            }
        }
        if (countryZipCode().isNotEmpty()) directChatBinding.tvcountrycode.text =
            countryZipCode() else directChatBinding.tvcountrycode.setText(R.string._922)
    }

    private fun openWhatsApp(smsNumber: String, msg: String) {
        val i = Intent(Intent.ACTION_VIEW)
        try {
            val url = "https://wa.me/$smsNumber?text=$msg"
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.whatsapp_not_found), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun initToolbar() {
        directChatBinding.toolbar.apply {
            toolbar.background = ContextCompat.getDrawable(
                this@DirectChatScreenActivity,
                R.drawable.bottom_corner_round
            )
            toolbarTitle.text = getString(R.string.direct_whats_chat)
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener {
                startActivity(Intent(this@DirectChatScreenActivity, ActivityPremium::class.java))
            }

        }
    }


    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(directChatBinding.topAdLayout, this)
        ShowInterstitial.hideNativeAndBanner(directChatBinding.bannercontainer, this)

        showNativeBanner(
            directChatBinding.nativebanner,
            directChatBinding.shimmerViewContainer
        )
    }

    private fun countryZipCode(): String {
        val countryID: String
        var countryZipCode = ""
        val manager = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        countryID = manager.simCountryIso.uppercase(Locale.getDefault())
        val rl = this.resources.getStringArray(R.array.CountryCodes)
        for (s in rl) {
            val g = s.split(",").toTypedArray()
            if (g[1].trim { it <= ' ' } == countryID.trim { it <= ' ' }) {
                countryZipCode = g[0]
                break
            }
        }
        return countryZipCode
    }

    override fun setMyName(string: String?) {
        directChatBinding.tvcountrycode.text = string
    }

}