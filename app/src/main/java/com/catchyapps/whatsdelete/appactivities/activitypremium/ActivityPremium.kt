package com.catchyapps.whatsdelete.appactivities.activitypremium

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.GoogleMobileAdsConsentManager
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.databinding.PremiumScreenBinding
import timber.log.Timber


class ActivityPremium : AppCompatActivity() {
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private lateinit var activityPremiumBinding: PremiumScreenBinding

    private val PRODUCT_ID = "lifetime_sub_catchy"
    private val LICENSE_KEY: String =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmjHeDNLDvWijxgWkmoCgymrZkHXaDFdSNeCl4p5lwY9z9X0UgKOCtjgL94+EBNTRL3TcP/bqSErTyBVmw22fnx9y70aGFS4SLy4XmQoDMDxrDXke8GvFuM1IM93/0evipmK6a/gOajR12YQ0UXsLKgVDVPVl6YDhkzQtdv7ZoH8Y33VXSjPafwMAYZOWpTBdnuwvS95hl2nbid+uRAKs/Lt8YcvztX2v0+Z3VV8r/rXE85CfLba3/IUeZKx0dnnE+VW04ESjzzIeNF1dB68bcFYno0dxgRdbm60GoqHoNebN9cNrtcWFdIOUfVPGCI9/mzt93o7WDLUWoUwlXBpJdQIDAQAB"
    private val MERCHANT_ID: String = "684585071710864016"


    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null

    private var appSharedPreferences: MyAppSharedPrefs? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPremiumBinding = PremiumScreenBinding.inflate(layoutInflater)
        setContentView(activityPremiumBinding.root)
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)


        appSharedPreferences = MyAppSharedPrefs(this)

        setupBillingClient()

        clickListeners()


    }


    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases() // Required for one-time purchases
            .setListener { billingResult, purchases ->
                handlePurchases(billingResult, purchases)
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                } else {
                    showPriceErrorFallback()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection
                setupBillingClient()
            }
        })
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    productDetailsList?.firstOrNull()?.let { details ->
                        productDetails = details
                        updatePriceUI(details)
                    } ?: showPriceErrorFallback()
                }

                else -> showPriceErrorFallback()
            }
        }
    }

    private fun updatePriceUI(productDetails: ProductDetails) {
        val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
        runOnUiThread {
            activityPremiumBinding.tvPrice.text =
                getString(R.string.premium_package_price_format, price)
        }
    }

    private fun showPriceErrorFallback() {
        runOnUiThread {
            activityPremiumBinding.tvPrice.text =
                getString(R.string.premium_package_default_price)
        }
    }

    private fun launchPurchaseFlow() {
        val productDetails = productDetails ?: run {
            Toast.makeText(this, "Product not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Toast.makeText(this, "Error starting purchase", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handlePurchases(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
            return
        }

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    // Acknowledge purchase and grant entitlement
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                runOnUiThread {
                    appSharedPreferences?.setPremium(true)
                    Toast.makeText(this, "Premium activated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun clickListeners() {
        activityPremiumBinding.apply {

            btnSubscribe.setOnClickListener {
                launchPurchaseFlow()
            }
            tvContinueWithAd.setOnClickListener {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    goToHome()
                } else {
                    showConsentDialog()
                }


            }

            ivBack.setOnClickListener {
                if (googleMobileAdsConsentManager.canRequestAds) {
                    goToHome()
                } else {
                    showConsentDialog()
                }
            }
        }
    }

    private fun goToHome() {
        finish()
        ShowInterstitial.showInter(this@ActivityPremium)
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        super.onDestroy()
        billingClient.endConnection()
    }

    private fun showConsentDialog() {
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