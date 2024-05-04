package com.catchyapps.whatsdelete.appactivities.activitystatussaver

import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.databinding.PreviewFullScreenBinding

class PreviewFullScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var hCheck: String? = null
    private var hImagePath: String? = null
    private lateinit var activityFullScreenImagePreview: PreviewFullScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFullScreenImagePreview = PreviewFullScreenBinding.inflate(layoutInflater)
        setContentView(activityFullScreenImagePreview.root)
        init()
    }

    private fun init() {
        hCheck = intent.getStringExtra("fromback")
        hImagePath = intent.getStringExtra("saveimage")

        Glide.with(this).load(hImagePath).into(
            activityFullScreenImagePreview.previewImage
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}