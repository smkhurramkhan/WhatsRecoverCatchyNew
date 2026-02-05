package com.catchyapps.whatsdelete.appactivities.activitystatussaver

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus.StatusSaveAdapter
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.adapterstatusaver.AdaptersaverPlayList
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.basicapputils.getPath
import com.catchyapps.whatsdelete.databinding.ScreenStatusPreviewBinding
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ActivityPreviewStatusScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var isVideo = false
    private var hStatusesEntity: EntityStatuses? = null

    private var folderId: Long = 0
    private var isDownloaded = false
    private var menuItemDownload: MenuItem? = null
    private var ids: List<Int>? = null
    private var prefs: MyAppSharedPrefs? = null
    private var isedited: String? = null
    private var sharingPath: String? = null
    private var fromCollection: String? = null

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var hActivityStatusPreviewBinding: ScreenStatusPreviewBinding

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.apply {
            release()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        simpleExoPlayer?.apply {
            stop()
            release()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hActivityStatusPreviewBinding = ScreenStatusPreviewBinding.inflate(layoutInflater)
        setContentView(hActivityStatusPreviewBinding.root)

        initAds()
        initVars()
        setIntentData()
        setupView()

    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(hActivityStatusPreviewBinding.topAdLayout, this)
        ShowInterstitial.hideNativeAndBanner(hActivityStatusPreviewBinding.bannerAd, this)

        BaseApplication.showNativeBanner(
            hActivityStatusPreviewBinding.nativeContainer,
            hActivityStatusPreviewBinding.shimmerViewContainer
        )

    }

    private fun initVars() {
        prefs = MyAppSharedPrefs(this)
        val trackSelector = DefaultTrackSelector(this)
        simpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()
    }


    private fun setupView() {

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        hActivityStatusPreviewBinding.toolbar.title = getString(R.string.preview)
        setSupportActionBar(hActivityStatusPreviewBinding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun setIntentData() {

        if (intent.extras != null) {
            fromCollection = intent.getStringExtra("fromFolderDetails")
        }
        if (intent.extras != null) {
            hStatusesEntity =
                intent.getParcelableExtra<Parcelable>("statusObject") as EntityStatuses?
            if (hStatusesEntity != null) {
                isDownloaded = hStatusesEntity?.savedPath != null
                isVideo = hStatusesEntity?.type == StatusSaveAdapter.hVideoType
                if (isVideo) {
                    playVideo()
                } else {
                    hActivityStatusPreviewBinding.simpleExoPlayerTrim.visibility = View.GONE
                    hActivityStatusPreviewBinding.ivPreview.visibility = View.VISIBLE
                    if (isDownloaded) hActivityStatusPreviewBinding.ivPreview.setImage(
                        ImageSource.uri(
                            hStatusesEntity!!.savedPath!!
                        )
                    ) else {
                        hSetPreviewImage()
                    }
                }
            }
        }

    }

    private fun hSetPreviewImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hActivityStatusPreviewBinding.ivPreview.setImage(
                ImageSource.uri(
                    hStatusesEntity?.uri!!
                )
            )
        } else {
            hActivityStatusPreviewBinding.ivPreview.setImage(
                ImageSource.uri(
                    hStatusesEntity?.path.toString()
                )
            )
        }


    }

    private fun playVideo() {
        try {
            val dataSourceFactory = DefaultDataSource.Factory(this)
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        hStatusesEntity?.uri!!
                    } else {
                        hStatusesEntity?.path?.toUri()!!
                    }
                ))

            hActivityStatusPreviewBinding.simpleExoPlayerTrim.player = simpleExoPlayer
            simpleExoPlayer?.setMediaSource(mediaSource)
            simpleExoPlayer?.prepare()
            simpleExoPlayer?.playWhenReady = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun reWAStatus() {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        if (isedited != null) {
            sharingPath = hStatusesEntity!!.sharedPath
            Timber.d("sharing path not null")
        } else {
            sharingPath = if (fromCollection != null) {
                hStatusesEntity!!.savedPath
            } else {
                hStatusesEntity!!.path
            }
            Timber.d("sharing path null")
        }
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(sharingPath))
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!isVideo) {
            whatsappIntent.type = "image/*"
        } else {
            whatsappIntent.type = "video/*"
        }
        whatsappIntent.setPackage("com.whatsapp")
        try {
            startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            MyAppUtils.showToast(this@ActivityPreviewStatusScreen, getString(R.string.whatsapp_not_found))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun hDialogReShareStatus() {
        val builder = AlertDialog.Builder(this)
        builder.setIcon(resources.getDrawable(R.mipmap.ic_launcher))
        builder.setTitle(getString(R.string.share_status))
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_share_this_status_on_whatsapp))
        builder.setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int -> reWAStatus() }
        builder.setNegativeButton(getString(R.string.no)) { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    private fun shareImage(imgPath: String?) {
        try {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val imageUri = FileProvider.getUriForFile(this, "$packageName.provider", File(imgPath))
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
        menuInflater.inflate(R.menu.status_preview_menu, menu)
        menuItemDownload = menu.findItem(R.id.action_download)
        if (isDownloaded) {
            menuItemDownload?.isVisible = false
        }
        if (isVideo) {
            menu.findItem(R.id.action_photo_editor).isVisible = false
            menu.findItem(R.id.action_fullscreen).isVisible = false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_download -> {
                simpleExoPlayer?.playWhenReady = false
                lifecycleScope.launch {
                    hShowPlaylistDialog(hStatusesEntity!!)
                }
                true
            }

            R.id.action_share -> {
                simpleExoPlayer?.playWhenReady = false
                shareImage(hStatusesEntity!!.path)
                true
            }

            R.id.action_restatus -> {
                simpleExoPlayer?.playWhenReady = false
                hDialogReShareStatus()
                true
            }

            R.id.action_fullscreen -> {
                if (hStatusesEntity?.path != null && !isVideo) {
                    val intent = Intent(
                        this@ActivityPreviewStatusScreen,
                        PreviewFullScreen::class.java
                    )
                    Timber.d("path is ${hStatusesEntity?.path}")
                    intent.putExtra("saveimage", hStatusesEntity?.path)
                    startActivity(intent)
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10001) {
            if (resultCode == RESULT_OK) {
                hStatusesEntity =
                    data!!.getParcelableExtra<Parcelable>("statusObject") as EntityStatuses?
                isedited = data.getStringExtra("isedited")
                if (hStatusesEntity != null) {
                    try {
                        hActivityStatusPreviewBinding.simpleExoPlayerTrim.visibility = View.GONE
                        hActivityStatusPreviewBinding.ivPreview.visibility = View.VISIBLE
                        hActivityStatusPreviewBinding.ivPreview.setImage(
                            ImageSource.uri(
                                hStatusesEntity!!.savedPath!!
                            )
                        )
                        menuItemDownload!!.isVisible = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createPlayListDialog(hStatusesEntity: EntityStatuses) {
        Timber.d("createPlayListDialog ${hStatusesEntity.filename}")

        val inflater = layoutInflater
        val builder = AlertDialog.Builder(this)
        val sheetView: View = inflater.inflate(
            R.layout.dialog_playlist_create_layout,
            window.decorView.rootView as ViewGroup,
            false
        )
        builder.setView(sheetView)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val etPlayListName = dialog.findViewById<EditText>(R.id.et_playlist_name)
        val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val tvCreate = dialog.findViewById<TextView>(R.id.tvCreate)!!
        tvCreate.setOnClickListener { view: View? ->
            CoroutineScope(Dispatchers.Main).launch {
                assert(etPlayListName != null)
                val playlistName = etPlayListName!!.text.toString()
                var foldersEntityList: List<EntityFolders>? = null

                foldersEntityList = AppHelperDb.getFolderByName(playlistName)
                if (playlistName.isEmpty()) MyAppUtils.snackBar(
                    this@ActivityPreviewStatusScreen,
                    getString(R.string.folder_name_can_t_be_empty)
                ) else if (foldersEntityList?.isNotEmpty() == true) {
                    MyAppUtils.showToast(
                        this@ActivityPreviewStatusScreen,
                        getString(R.string.folder_already_exist_with_the_same_name)
                    )
                } else {
                    dialog.dismiss()
                    val foldersEntity =
                        EntityFolders()
                    foldersEntity.noOfItems = 0
                    foldersEntity.playlistName = playlistName

                    folderId = AppHelperDb.insertFolder(foldersEntity)
                    foldersEntity.id = folderId.toInt()
                    if (folderId > 0) {
                        hShowPlaylistDialog(hStatusesEntity)
                    } else MyAppUtils.showToast(
                        this@ActivityPreviewStatusScreen,
                        getString(R.string.failed_to_create_folder)
                    )
                }
            }
        }

        tvCancel?.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                dialog.dismiss()
                hShowPlaylistDialog(hStatusesEntity)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun hShowPlaylistDialog(hStatusesEntity: EntityStatuses) {
        ids = ArrayList()
        val inflater = layoutInflater
        val builder = AlertDialog.Builder(this)
        val sheetView: View = inflater.inflate(
            R.layout.playlist_dialog_,
            window.decorView.rootView as ViewGroup,
            false
        )
        builder.setView(sheetView)
        val layoutCreatePlayList: LinearLayout? =
            sheetView.findViewById(R.id.layout_create_playlist)
        val tvOk = sheetView.findViewById<TextView>(R.id.tvOk)
        val tvCancel = sheetView.findViewById<TextView>(R.id.tvCancel)
        val recyclerViewPlayList: RecyclerView = sheetView.findViewById(R.id.recycler_view_playlist)
        var playListEntities: List<EntityFolders>? = null

        playListEntities = AppHelperDb.getAllFolders()
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        recyclerViewPlayList.layoutManager = mLayoutManager
        recyclerViewPlayList.itemAnimator = DefaultItemAnimator()
        if (playListEntities?.isNotEmpty() == true)
            playListEntities[0].isCheck = true
        recyclerViewPlayList.adapter = AdaptersaverPlayList(playListEntities, this)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        tvOk.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (playListEntities?.isNotEmpty() == true) {
                    var isCheck = false
                    for (i in playListEntities.indices) {
                        if (playListEntities[i].isCheck) {
                            dialog.dismiss()
                            val folderEntity = playListEntities[i]
                            var isAvailable = false
                            isCheck = true
                            folderId = folderEntity.id.toLong()
                            val list: List<EntityStatuses>? = null

                            AppHelperDb.getFolderById(
                                folderEntity.id.toString()
                            )

                            if (list?.isNotEmpty() == true) {
                                for (j in list.indices) {
                                    if (list[j].path == hStatusesEntity.path) {
                                        isAvailable = true
                                    }
                                }
                            }
                            if (!isAvailable) {
                                downloadStatus(
                                    StatusSaveAdapter.COLLECTION_ALREADY_CREATED,
                                    folderId.toInt(),
                                    hStatusesEntity
                                )
                            } else {

                                if (hStatusesEntity.type == StatusSaveAdapter.hVideoType) {
                                    MyAppUtils.showToast(
                                        this@ActivityPreviewStatusScreen,
                                        getString(R.string.video_already_added)
                                    )
                                } else {
                                    MyAppUtils.showToast(
                                        this@ActivityPreviewStatusScreen,
                                        getString(R.string.image_already_added)
                                    )
                                }
                            }
                        }
                    }
                    if (!isCheck) {
                        MyAppUtils.showToast(
                            this@ActivityPreviewStatusScreen,
                            getString(R.string.create_or_select_collection_first)
                        )
                    }
                } else {
                    MyAppUtils.showToast(
                        this@ActivityPreviewStatusScreen,
                        getString(R.string.create_or_select_collection_first)
                    )
                }
            }
        }

        tvCancel?.setOnClickListener { dialog.dismiss() }
        assert(layoutCreatePlayList != null)
        layoutCreatePlayList?.setOnClickListener {
            dialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                createPlayListDialog(hStatusesEntity)
            }
        }
    }

    private fun checkFolder() {
        val path = Environment.getExternalStorageDirectory()
            .toString() + "/" + MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_STATUS


        val dir = File(path)
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
        }
        if (isDirectoryCreated) {
            Timber.d("Already Created ${dir.absolutePath}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downloadStatus(flag: Int, fid: Int, hStatusesEntity: EntityStatuses) {
        Timber.d("downloadStatus ${hStatusesEntity.filename}")

        val foldersEntity =
            EntityFolders()
        val filename = hStatusesEntity.path?.substring(hStatusesEntity.path!!.lastIndexOf("/") + 1)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                hWriteDataForPostAndroid10(hStatusesEntity)
            } else {
                hWriteDataForPreAndroid11(hStatusesEntity)

            }
        } catch (e: Exception) {
            Timber.d("Write Exception ${e.message}")
        }

        if (flag == StatusSaveAdapter.NEW_COLLECTION_CREATED) {
            foldersEntity.playListLogo = hStatusesEntity.savedPath
            hStatusesEntity.folderId = fid.toString()

            AppHelperDb.insertStatus(hStatusesEntity)
            AppHelperDb.updateFolderById(
                foldersEntity.playListLogo,
                foldersEntity.noOfItems,
                folderId.toInt()
            )
        } else {
            foldersEntity.noOfItems = foldersEntity.noOfItems + 1
            foldersEntity.playListLogo = hStatusesEntity.savedPath
            val statusesEntity = EntityStatuses()
            statusesEntity.filename = filename
            statusesEntity.uri = hStatusesEntity.uri
            statusesEntity.type = hStatusesEntity.type
            statusesEntity.folderId = fid.toString()
            statusesEntity.path = hStatusesEntity.savedPath
            statusesEntity.name = filename
            statusesEntity.savedPath = hStatusesEntity.savedPath
            statusesEntity.sharedPath = hStatusesEntity.sharedPath

            AppHelperDb.insertStatus(statusesEntity)
            AppHelperDb.updateFolderById(
                foldersEntity.playListLogo,
                foldersEntity.noOfItems,
                fid
            )
        }
        showToastOnUi()
    }

    private fun showToastOnUi() {
        runOnUiThread {
            MyAppUtils.showToast(
                this@ActivityPreviewStatusScreen,
                getString(R.string.saved_status)
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hWriteDataForPostAndroid10(hStatusesEntity: EntityStatuses) {
        val contentResolver = contentResolver
        var hItemUri: Uri?

        hStatusesEntity.uri?.let {
            contentResolver.openFileDescriptor(it, "r")?.use { fileDescriptor ->
                val hFIPS = FileInputStream(fileDescriptor.fileDescriptor)

                val hValues = ContentValues()
                hValues.put(MediaStore.MediaColumns.DISPLAY_NAME, hStatusesEntity.filename)
                hValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + MyAppUtils.WA_STATUS
                )
                hValues.put(MediaStore.MediaColumns.MIME_TYPE, StatusSaveAdapter.hImageType)
                hItemUri =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, hValues)

                contentResolver.openOutputStream(hItemUri!!).use {
                    it?.write(hFIPS.readBytes())
                }
                hStatusesEntity.savedPath = getPath(this, hItemUri!!)
                Timber.d("Path is ${getPath(this, hItemUri!!)}")
            }
        }
    }

    private fun hWriteDataForPreAndroid11(hStatusesEntity: EntityStatuses) {
        Timber.d("hWriteDataForPreAndroid11 ${hStatusesEntity.filename}")

        checkFolder()
        val file = File(hStatusesEntity.path!!)
        val filename = hStatusesEntity.path!!.substring(hStatusesEntity.path!!.lastIndexOf("/") + 1)
        val destPath = Environment.getExternalStorageDirectory()
            .toString() + "/" + MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_STATUS
        val destFile = File(destPath)
        try {
            FileUtils.copyFileToDirectory(file, destFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        hStatusesEntity.savedPath = destPath + filename

    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer?.release()
        simpleExoPlayer = null
    }


}
