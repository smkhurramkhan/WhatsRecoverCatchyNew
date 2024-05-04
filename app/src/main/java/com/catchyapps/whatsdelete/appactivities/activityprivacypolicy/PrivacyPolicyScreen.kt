package com.catchyapps.whatsdelete.appactivities.activityprivacypolicy

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.databinding.ScreenWebWhatsBinding

class PrivacyPolicyScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var privacyBinding: ScreenWebWhatsBinding


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        privacyBinding = ScreenWebWhatsBinding.inflate(layoutInflater)
        setContentView(privacyBinding.root)
        setupToolbar()

        loadWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        privacyBinding.webView.settings.javaScriptEnabled = true
        privacyBinding.webView.loadUrl(getString(R.string.privacy_url))
        privacyBinding.webView.isHorizontalScrollBarEnabled = false
        privacyBinding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                privacyBinding.progressBar.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                privacyBinding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setupToolbar() {
        val title = getString(R.string.privacy_policy)
        privacyBinding.toolbar.title = title
        setSupportActionBar(privacyBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
}