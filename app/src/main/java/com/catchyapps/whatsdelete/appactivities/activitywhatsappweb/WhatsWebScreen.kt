package com.catchyapps.whatsdelete.appactivities.activitywhatsappweb

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.databinding.ScreenWebWhatsBinding

class WhatsWebScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private lateinit var binding: ScreenWebWhatsBinding
    var url = "https://web.whatsapp.com"

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreenWebWhatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadWebView()
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {

        binding.webView.loadUrl(url)
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.settings.userAgentString =
            "Mozilla/5.0 (Linux; Win64; x64; rv:46.0) Gecko/20100101 Firefox/68.0"
        binding.webView.settings.setGeolocationEnabled(true)
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.databaseEnabled = true
        binding.webView.settings.setSupportMultipleWindows(true)
        binding.webView.settings.setNeedInitialFocus(true)
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webView.settings.blockNetworkImage = true
        binding.webView.settings.builtInZoomControls = true
        binding.webView.setInitialScale(100)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun setupToolbar() {
        val title = getString(R.string.whatsapp_web_)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

}