package com.catchyapps.whatsdelete.appactivities.activitypremium

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.catchyapps.whatsdelete.appactivities.activityhome.MainActivity
import com.catchyapps.whatsdelete.appadsmanager.GoogleMobileAdsConsentManager
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.databinding.PremiumScreenBinding
import timber.log.Timber


class ActivityPremium : AppCompatActivity() {
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private lateinit var activityPremiumBinding: PremiumScreenBinding

    private val PRODUCT_ID = ""
    private val LICENSE_KEY: String = ""
    private val MERCHANT_ID: String = ""

    private var bp: BillingProcessor? = null
    private var readyToPurchase = false
    private lateinit var appSharedPreferences: MyAppSharedPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPremiumBinding = PremiumScreenBinding.inflate(layoutInflater)
        setContentView(activityPremiumBinding.root)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)


        appSharedPreferences = MyAppSharedPrefs(this)

        initBilling()

        clickListeners()


    }


    private fun initBilling() {

        bp = BillingProcessor(this, LICENSE_KEY, MERCHANT_ID, object :
            BillingProcessor.IBillingHandler {
            override fun onProductPurchased(
                productId: String,
                purchaseInfo: PurchaseInfo?
            ) {
                appSharedPreferences.setPremium(true)
            }

            override fun onBillingError(errorCode: Int, error: Throwable?) {
                showToast("onBillingError: $errorCode")
            }

            override fun onBillingInitialized() {
                readyToPurchase = true
            }

            override fun onPurchaseHistoryRestored() {
                for (sku in bp!!.listOwnedProducts())
                    Timber.d("Owned Managed Product: $sku")
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }



    private fun clickListeners() {
        activityPremiumBinding.apply {

            btnSubscribe.setOnClickListener {
                applyForSubscription()
            }
            tvContinueWithAd.setOnClickListener {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    goToHome()
                }else{
                    showConsentDialog()
                }


            }

            ivBack.setOnClickListener {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    goToHome()
                }else{
                    showConsentDialog()
                }
            }
        }
    }

    private fun goToHome() {
       // startActivity(Intent(this@ActivityPremium, MainActivity::class.java))
        finish()
        ShowInterstitial.showAdmobInter(this@ActivityPremium)
    }

    private fun applyForSubscription() {
        bp?.purchase(this, PRODUCT_ID);
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        if (bp != null) bp!!.release()
        super.onDestroy()
    }

    private fun showConsentDialog(){
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                Timber.d("consent error is ${consentError.errorCode}: ${consentError.message}")
            }

            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                invalidateOptionsMenu()
            }

            // This sample attempts to load ads using consent obtained in the previous session.
            if (googleMobileAdsConsentManager.canRequestAds) {
                goToHome()
            }
        }

    }

}