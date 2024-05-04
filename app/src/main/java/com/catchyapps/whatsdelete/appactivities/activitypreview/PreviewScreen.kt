package com.catchyapps.whatsdelete.appactivities.activitypreview

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import androidx.core.net.toUri
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.PreviewFullScreen
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber
import java.io.File

class PreviewScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var isVideo = false
    private lateinit var ivPreview: SubsamplingScaleImageView

    //  VideoView videoView;
    private var filePath: String? = null
    private lateinit var progressBar: ProgressBar
    private var isToolbarShowing = true
    private lateinit var simpleExoPlayerView: PlayerView
    private var simpleExoPlayer: SimpleExoPlayer? = null
    private var extractorsFactory: ExtractorsFactory? = null
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

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        progressBar = findViewById(R.id.progressBar_preview)

        ivPreview = findViewById(R.id.ivPreview)


        simpleExoPlayerView = findViewById(R.id.simpleExoPlayerTrim)

        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()

        val trackSelector: TrackSelector =
            DefaultTrackSelector(AdaptiveTrackSelection.Factory(bandwidthMeter))

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector)
        extractorsFactory = DefaultExtractorsFactory()


        val bannerContainer = findViewById<FrameLayout>(R.id.bannerContainer)
        val nativeContainer = findViewById<FrameLayout>(R.id.nativeContainer)
        val shimmer = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        val topAdLayout = findViewById<ConstraintLayout>(R.id.topAdLayout)
        BaseApplication.showNativeBanner(nativeContainer, shimmer)



        ShowInterstitial.hideNativeAndBanner(bannerContainer, this)
        ShowInterstitial.hideNativeAndBanner(topAdLayout, this)

        ivPreview.setOnClickListener {
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
            Timber.d("File Path : $filePath")
            if (filePath != null) {
                isVideo = filePath!!.endsWith(".mp4")
                if (isVideo) {
                    playVideo()
                } else {
                    simpleExoPlayerView.visibility = View.GONE
                    ivPreview.visibility = View.VISIBLE
                    if (filePath!!.contains("http")) {
                        progressBar.visibility = View.VISIBLE
                        Glide.with(this).asBitmap().load(filePath)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    try {
                                        ivPreview.setImage(ImageSource.bitmap(resource))
                                        progressBar.isIndeterminate = false
                                        progressBar.visibility = View.GONE
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {}
                            })
                    } else ivPreview.setImage(ImageSource.uri(filePath!!))
                    ivPreview.setImage(ImageSource.uri(filePath!!))
                }
            }
        }

    }

    private fun playVideo() {
        try {
            val playerInfo = Util.getUserAgent(this, "VEditor")
            val dataSourceFactory = DefaultDataSourceFactory(this, playerInfo)
            val mediaSource: MediaSource = ExtractorMediaSource(
                filePath?.toUri(),
                dataSourceFactory,
                extractorsFactory,
                null,
                null
            )
            simpleExoPlayerView.player = simpleExoPlayer
            simpleExoPlayer!!.prepare(mediaSource)
            simpleExoPlayer!!.playWhenReady = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareImage(imgPath: String?) {
        try {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val imageUri =
                FileProvider.getUriForFile(this, "$packageName.provider", File(imgPath!!))
            intentShareFile.type = "application/octet-stream"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, imageUri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share))
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
        menuInflater.inflate(R.menu.preview_menu, menu)
        val download = menu.findItem(R.id.action_download)
        val fullscreen = menu.findItem(R.id.action_fullscreen)
        val reshare = menu.findItem(R.id.action_restatus)
        download.isVisible = false
        reshare.isVisible = false
        if (filePath != null && isVideo) {
            fullscreen.isVisible = false
        }
        return true
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        if (item.itemId == R.id.action_share) {
            shareImage(filePath)
            return true
        } else if (item.itemId == R.id.action_fullscreen) {
            if (filePath != null && !isVideo) {
                val intent =
                    Intent(this@PreviewScreen, PreviewFullScreen::class.java)
                intent.putExtra("saveimage", filePath)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}