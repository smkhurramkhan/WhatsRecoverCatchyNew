package com.catchyapps.whatsdelete.appactivities.activitypreview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.MediaController
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.PreviewFullScreen
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.databinding.ScreenPreviewMediaBinding
import java.io.File

class MediaPreviewScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var isVideo = false
    private var filePath: String? = null
    private var isToolbarShowing = true
    lateinit var hActivityMediaPreviewBinding: ScreenPreviewMediaBinding
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityMediaPreviewBinding = ScreenPreviewMediaBinding.inflate(layoutInflater)
        setContentView(hActivityMediaPreviewBinding.root)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.preview)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }


        hActivityMediaPreviewBinding.ivPreview.setOnClickListener {
            if (isToolbarShowing) {
                toolbar.visibility = View.INVISIBLE
                isToolbarShowing = false
            } else {
                toolbar.visibility = View.VISIBLE
                isToolbarShowing = true
            }
        }
        hActivityMediaPreviewBinding.videoView.setOnClickListener {
            if (isToolbarShowing) {
                toolbar.visibility = View.INVISIBLE
                isToolbarShowing = false
            } else {
                toolbar.visibility = View.VISIBLE
                isToolbarShowing = true
            }
        }
        if (intent.extras != null) {
            filePath = intent.getStringExtra("file_path")
            if (filePath != null) {
                isVideo = filePath!!.endsWith(".mp4")
                if (isVideo) {
                    hActivityMediaPreviewBinding.ivPreview.visibility = View.GONE
                    hActivityMediaPreviewBinding.videoView.visibility = View.VISIBLE
                    hActivityMediaPreviewBinding.videoView.setVideoPath(filePath)
                    hActivityMediaPreviewBinding.videoView.setMediaController(MediaController(this))
                    hActivityMediaPreviewBinding.videoView.seekTo(100)
                    //   videoView.start();
                    hActivityMediaPreviewBinding.videoView.setOnInfoListener { mediaPlayer: MediaPlayer?, i: Int, i1: Int ->
                        when (i) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> hActivityMediaPreviewBinding.progressBarPreview.visibility =
                                View.VISIBLE
                            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> hActivityMediaPreviewBinding.progressBarPreview.visibility =
                                View.GONE
                        }
                        false
                    }
                    hActivityMediaPreviewBinding.videoView.setOnCompletionListener { mediaPlayer: MediaPlayer? -> }
                } else {
                    hActivityMediaPreviewBinding.videoView.visibility = View.GONE
                    hActivityMediaPreviewBinding.ivPreview.visibility = View.VISIBLE
                    if (filePath!!.contains("http")) {
                        hActivityMediaPreviewBinding.progressBarPreview.visibility = View.VISIBLE
                        Glide.with(this).asBitmap().load(filePath)
                            .into(object : CustomTarget<Bitmap?>() {


                                override fun onLoadCleared(placeholder: Drawable?) {}
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap?>?
                                ) {
                                    try {
                                        hActivityMediaPreviewBinding.ivPreview.setImage(
                                            ImageSource.bitmap(
                                                resource
                                            )
                                        )
                                        hActivityMediaPreviewBinding.progressBarPreview.isIndeterminate =
                                            false
                                        hActivityMediaPreviewBinding.progressBarPreview.visibility =
                                            View.GONE
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                    } else hActivityMediaPreviewBinding.ivPreview.setImage(ImageSource.uri(filePath!!))
                    hActivityMediaPreviewBinding.ivPreview.setImage(ImageSource.uri(filePath!!))
                }
            }
        }
    }

    private fun shareImage(imgPath: String?) {
        try {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val imageUri = FileProvider.getUriForFile(this, "$packageName.provider", File(imgPath))
            intentShareFile.type = "application/octet-stream"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, imageUri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...")
            intentShareFile.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            startActivity(Intent.createChooser(intentShareFile, getString(R.string.share)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.preview_menu, menu)
        menu.findItem(R.id.action_download).isVisible = false
        menu.findItem(R.id.action_photo_editor).isVisible = false
        menu.findItem(R.id.action_restatus).isVisible = false
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.action_share) {
            shareImage(filePath)
            return true
        } else if (item.itemId == R.id.action_fullscreen) {
            startActivity(
                Intent(
                    this@MediaPreviewScreen,
                    PreviewFullScreen::class.java
                ).putExtra("saveimage", filePath)
            )
        }
        return super.onOptionsItemSelected(item)
    }
}