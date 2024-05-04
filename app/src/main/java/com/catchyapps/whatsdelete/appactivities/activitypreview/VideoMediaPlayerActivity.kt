package com.catchyapps.whatsdelete.appactivities.activitypreview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.MediaController
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.databinding.ScreenImagePreviewBinding

class VideoMediaPlayerActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var sharedPreferences: MyAppSharedPrefs? = null
    private var isVideo = false
    private var mediaController: MediaController? = null
    private var isedited: String? = null
    private var sharingPath: String? = null
    private var fromCollection: String? = null
    var path: String? = null
    private lateinit var imagePreviewActivityBinding: ScreenImagePreviewBinding
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent()
        intent.putExtra("showAd", true)
        setResult(RESULT_OK, intent)
        finish()
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePreviewActivityBinding = ScreenImagePreviewBinding.inflate(layoutInflater)
        setContentView(imagePreviewActivityBinding.root)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        setupToolbar()
        sharedPreferences = MyAppSharedPrefs(this)
        if (intent.extras != null) {
            path = intent.getStringExtra("path")
            isVideo = path!!.endsWith(".mp4")
            imagePreviewActivityBinding.playButton.visibility = View.VISIBLE
            imagePreviewActivityBinding.playButton.setOnClickListener {
                if (!imagePreviewActivityBinding.videoView.isPlaying) {
                    imagePreviewActivityBinding.playButton.visibility = View.GONE
                    imagePreviewActivityBinding.videoView.start()
                }
            }
            if (mediaController == null) {
                mediaController = MediaController(this@VideoMediaPlayerActivity)
            }
            imagePreviewActivityBinding.videoView.setMediaController(mediaController)
            imagePreviewActivityBinding.videoView.visibility = View.VISIBLE
            imagePreviewActivityBinding.videoView.setVideoPath(path)
            imagePreviewActivityBinding.videoView.seekTo(1)
            imagePreviewActivityBinding.videoView.setOnTouchListener(object :
                View.OnTouchListener {
                var flag = true

                @SuppressLint("ClickableViewAccessibility")
                override fun onTouch(
                    view: View,
                    @SuppressLint("ClickableViewAccessibility") motionEvent: MotionEvent
                ): Boolean {
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        if (flag) {
                            imagePreviewActivityBinding.videoView.pause()
                            imagePreviewActivityBinding.playButton.visibility = View.GONE
                        } else {
                            mediaController?.show(10000)
                            imagePreviewActivityBinding.videoView.start()
                        }
                        flag = !flag
                        return true
                    }
                    return false
                }
            })
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.preview)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun shareVideoFile() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        if (isedited != null) {
            sharingPath = path
        } else {
            sharingPath = if (fromCollection != null) {
                path
            } else {
                path
            }
        }
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(sharingPath))
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!isVideo) {
            sharingIntent.type = "image/*"
        } else {
            sharingIntent.type = "video/*"
        }
        sharingIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=$packageName"
        )
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share)))
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_preview_activity, menu)
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_share) {
            if (imagePreviewActivityBinding.videoView.isPlaying) {
                imagePreviewActivityBinding.videoView.pause()
            }
            shareVideoFile()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("showAd", true)
        setResult(RESULT_OK, intent)
        finish()
    }
}