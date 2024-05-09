package com.catchyapps.whatsdelete.appactivities.activitydetailfolder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus.StatusSaveAdapter
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appclasseshelpers.RVTouchListener
import com.catchyapps.whatsdelete.databinding.ScreenSingleDetailsFolderBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class WASingleFolderDetailActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {
    private var foldersEntity: EntityFolders? = null
    private var statusAdapter: StatusSaveAdapter? = null
    private var folderList: MutableList<EntityStatuses>? = mutableListOf()
    private var appSharedPrefs: MyAppSharedPrefs? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null

    private var isLoadedFirstTime = true
    private lateinit var activityFolderDetailBinding: ScreenSingleDetailsFolderBinding
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFolderDetailBinding = ScreenSingleDetailsFolderBinding.inflate(layoutInflater)
        setContentView(activityFolderDetailBinding.root)

        hInitAds()

        hInitVar()

        hSetView()

        hInitRecyclerView()


    }

    private fun hInitAds() {
        ShowInterstitial.hideNativeAndBanner(
            activityFolderDetailBinding.adViewContainer,
            this
        )
        ShowInterstitial.hideNativeAndBanner(
            activityFolderDetailBinding.topAdLayout,
            this
        )

        BaseApplication.showNativeBanner(
            activityFolderDetailBinding.nativeContainer,
            activityFolderDetailBinding.shimmerViewContainer
        )

    }

    private fun hInitRecyclerView() {
        statusAdapter = StatusSaveAdapter(this, true, this@WASingleFolderDetailActivity)


        activityFolderDetailBinding.rvCollectionDetail.apply {
            adapter = statusAdapter
            layoutManager = GridLayoutManager(this@WASingleFolderDetailActivity, 4)

            addOnItemTouchListener(
                RVTouchListener(
                    this@WASingleFolderDetailActivity,
                    this,
                    object :
                        RVTouchListener.ClickListener {
                        override fun onClick(view: View, position: Int) {
                            try {
                                if (folderList?.get(position) is EntityStatuses) {
                                    if (statusAdapter!!.selectedItemCount > 0) {
                                        enableActionMode(position)
                                    } else {
                                        //  startPreviewIntnet(position)
                                    }
                                }
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onLongClick(view: View, position: Int) {
                            enableActionMode(position)
                        }
                    }
                )
            )
        }

    }

    private fun hSetView() {
        setSupportActionBar(activityFolderDetailBinding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
    }

    private fun hInitVar() {

        appSharedPrefs = MyAppSharedPrefs(this)
        actionModeCallback = ActionModeCallback()


        if (intent.extras != null) {
            foldersEntity = intent.getSerializableExtra("model") as EntityFolders?
            if (foldersEntity != null) {
                activityFolderDetailBinding.toolbar.title = foldersEntity!!.playlistName
            }
        }

    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if (foldersEntity != null) {
                try {
                    isLoadedFirstTime = true
                    folderList?.clear()
                    title = foldersEntity?.playlistName
                    val templist =
                        AppHelperDb.hGetfolderById(foldersEntity!!.id.toString())?.toMutableList()

                    templist?.reverse()
                    templist?.let { folderList?.addAll(it) }
                    if (folderList?.isNotEmpty() == true) {
                        activityFolderDetailBinding.rvCollectionDetail.visibility = View.VISIBLE
                        activityFolderDetailBinding.layoutNotfound.visibility = View.GONE
                    } else {
                        activityFolderDetailBinding.rvCollectionDetail.visibility = View.GONE
                        activityFolderDetailBinding.layoutNotfound.visibility = View.VISIBLE
                    }
                    folderList?.toList()?.let { statusAdapter?.hSetData(it) }
                    statusAdapter?.notifyDataSetChanged()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_delete, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val id = item.itemId
            if (id == R.id.action_delete) {
                val alertDialogBuilder = AlertDialog.Builder(this@WASingleFolderDetailActivity)
                alertDialogBuilder.setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_status_the_selected_files))
                    .setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface?, _: Int ->
                        deleteItemFromPlaylist()

                        mode.finish()
                    }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        mode.finish()
                    }.show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            statusAdapter?.clearSelections()
            actionMode = null
        }
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback!!)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        statusAdapter?.toggleSelection(position)
        val count = statusAdapter?.selectedItemCount
        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = buildString {
                    append(count)
                        .append(getString(R.string.items_selected))
            }
            actionMode?.invalidate()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteItemFromPlaylist() {
        val selectedItemPositions = statusAdapter!!.selectedItems
        for (i in selectedItemPositions.indices.reversed()) {
            lifecycleScope.launch {
                deleteStatus(selectedItemPositions[i])

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun deleteStatus(position: Int) {
        val path = folderList?.get(position)?.savedPath
        val file = path?.let { File(it) }
            if (file?.exists() == true) {
                val del = file.delete()
                if (del) {
                    MediaScannerConnection.scanFile(
                        this@WASingleFolderDetailActivity,
                        arrayOf(path, path),
                        arrayOf("image/jpg", "video/mp4"),
                        object : MediaScannerConnection.MediaScannerConnectionClient {
                            override fun onMediaScannerConnected() {}
                            override fun onScanCompleted(path: String?, uri: Uri?) {
                                Timber.d(path)
                            }
                        })
                } else {
                    MyAppUtils.showToast(this@WASingleFolderDetailActivity, getString(R.string.something_went_wrong))
                }

            }
        if (foldersEntity != null) {

            folderList?.get(position)?.id?.let {
                AppHelperDb.hDeleteStatus(
                    it
                )
            }

            folderList?.removeAt(position)
            foldersEntity?.noOfItems = foldersEntity?.noOfItems!! - 1


            val temp = AppHelperDb.hGetfolderById(foldersEntity?.id.toString())
            if (temp?.isNotEmpty() == true) {
                foldersEntity?.playListLogo = temp[temp.size - 1].savedPath
                AppHelperDb.hUpdateFolderById(
                    foldersEntity?.playListLogo,
                    foldersEntity?.noOfItems!!,
                    foldersEntity?.id!!
                )
                withContext(Dispatchers.Main) {
                    statusAdapter?.notifyDataSetChanged()
                }
            } else {
                AppHelperDb.hUpdateFolderById(
                    "",
                    0,
                    foldersEntity!!.id
                )
                withContext(Dispatchers.Main) {
                    statusAdapter?.notifyDataSetChanged()
                }
            }


        }
    }
}
