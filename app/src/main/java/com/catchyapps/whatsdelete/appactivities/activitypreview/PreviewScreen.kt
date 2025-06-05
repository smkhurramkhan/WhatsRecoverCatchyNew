package com.catchyapps.whatsdelete.appactivities.activitypreview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.PreviewFullScreen
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import timber.log.Timber
import java.io.File

class PreviewScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var isVideo = false
    private lateinit var ivPreview: SubsamplingScaleImageView
    private var filePath: String? = null
    private lateinit var progressBar: ProgressBar
    private var isToolbarShowing = true
    private lateinit var simpleExoPlayerView: PlayerView
    private var simpleExoPlayer: ExoPlayer? = null

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_preview)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.preview)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        progressBar = findViewById(R.id.progressBar_preview)
        ivPreview = findViewById(R.id.ivPreview)
        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerTrim)

        val bannerContainer = findViewById<FrameLayout>(R.id.bannerContainer)
        val nativeContainer = findViewById<FrameLayout>(R.id.nativeContainer)
        val shimmer = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        val topAdLayout = findViewById<ConstraintLayout>(R.id.topAdLayout)
        BaseApplication.showNativeBanner(nativeContainer, shimmer)

        ShowInterstitial.hideNativeAndBanner(bannerContainer, this)
        ShowInterstitial.hideNativeAndBanner(topAdLayout, this)

        ivPreview.setOnClickListener {
            toolbar.visibility = if (isToolbarShowing) View.INVISIBLE else View.VISIBLE
            isToolbarShowing = !isToolbarShowing
        }

        filePath = intent.getStringExtra("file_path")
        filePath?.let {
            Timber.d("File Path : $filePath")
            isVideo = it.endsWith(".mp4")
            if (isVideo) {
                playVideo(it)
            } else {
                simpleExoPlayerView.visibility = View.GONE
                ivPreview.visibility = View.VISIBLE

                if (it.contains("http")) {
                    progressBar.visibility = View.VISIBLE
                    Glide.with(this).asBitmap().load(it)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                try {
                                    ivPreview.setImage(ImageSource.bitmap(resource))
                                    progressBar.visibility = View.GONE
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                } else {
                    ivPreview.setImage(ImageSource.uri(it))
                }
            }
        }
    }

    private fun playVideo(path: String) {
        try {
            val uri = Uri.parse(path)
            val dataSourceFactory = DefaultDataSource.Factory(this)
            val mediaItem = MediaItem.fromUri(uri)

            simpleExoPlayer = ExoPlayer.Builder(this).build()
            simpleExoPlayerView.player = simpleExoPlayer
            simpleExoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareImage(imgPath: String?) {
        try {
            val imageUri =
                FileProvider.getUriForFile(this, "$packageName.provider", File(imgPath!!))

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share))
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=$packageName"
                )
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.preview_menu, menu)
        val download = menu.findItem(R.id.action_download)
        val fullscreen = menu.findItem(R.id.action_fullscreen)
        val reshare = menu.findItem(R.id.action_restatus)
        download.isVisible = false
        reshare.isVisible = false
        fullscreen.isVisible = !(filePath != null && isVideo)
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareImage(filePath)
                true
            }

            R.id.action_fullscreen -> {
                if (!isVideo && filePath != null) {
                    startActivity(
                        Intent(this, PreviewFullScreen::class.java).putExtra("saveimage", filePath)
                    )
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        simpleExoPlayer?.release()
        simpleExoPlayer = null
        super.onDestroy()
    }
}
