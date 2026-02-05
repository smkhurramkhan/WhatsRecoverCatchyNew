package com.catchyapps.whatsdelete.appactivities.activitycollection

import android.app.AlertDialog
import android.content.DialogInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.RVTouchListener
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.savedstatus.SavedFolderAdapter
import com.catchyapps.whatsdelete.basicapputils.MyAppFolderListener
import com.catchyapps.whatsdelete.databinding.FragmentSavedScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class StatusSavedCollectionFragment : Fragment(),
    MyAppFolderListener {
    private var isLoadFirstTime = true
    var appSharedPrefs: MyAppSharedPrefs? = null
    private var folderList: MutableList<Any>? = null
    private var folderAdapter: SavedFolderAdapter? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null
    private lateinit var savedFragmentBinding: FragmentSavedScreenBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        savedFragmentBinding = FragmentSavedScreenBinding.inflate(
            inflater,
            container,
            false,
        )
        return savedFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionModeCallback = ActionModeCallback()
        appSharedPrefs = MyAppSharedPrefs(requireActivity())
        folderList = ArrayList()
        folderAdapter = SavedFolderAdapter(
            folderList!!,
            requireActivity(),
            this,
            requireActivity()
        )
        savedFragmentBinding.recyclerView.apply {
            adapter = folderAdapter
            layoutManager = GridLayoutManager(requireActivity(), 2)
            addOnItemTouchListener(
                RVTouchListener(
                    requireActivity(),
                    savedFragmentBinding.recyclerView,
                    object :
                        RVTouchListener.ClickListener {
                        override fun onClick(view: View, position: Int) {
                            if (folderAdapter?.selectedItemCount!! > 0) {
                                enableActionMode(position)
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

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                isLoadFirstTime = true
                folderList?.clear()

                AppHelperDb.getAllFolders()?.toMutableList()?.let {
                    folderList?.addAll(it)
                }

                if (folderList!!.size > 0) {
                    folderAdapter?.notifyDataSetChanged()
                    savedFragmentBinding.layoutNotfound.visibility = View.GONE
                    savedFragmentBinding.recyclerView.visibility = View.VISIBLE
                } else {
                    savedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
                    savedFragmentBinding.recyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun folderDeleted() {
        if (folderAdapter!!.itemCount == 0) {
            savedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
            savedFragmentBinding.recyclerView.visibility = View.GONE
        } else {
            savedFragmentBinding.layoutNotfound.visibility = View.GONE
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
                val alertDialogBuilder = AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle(getString(R.string.confirm_delete))
                    .setMessage(getString(R.string.are_you_sure_you_want_to_status_the_selected_files))
                    .setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface?, which: Int ->
                        try {
                            deleteSelectedFolders()
                            mode.finish()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                        mode.finish()
                    }.show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            folderAdapter?.clearSelections()
            actionMode = null
        }
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            try {
                actionMode =
                    (requireActivity() as ActivityStatusSavedCollections).startSupportActionMode(
                        actionModeCallback!!
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        try {      /// fix no 4.........................
            folderAdapter!!.toggleSelection(position)
            val count = folderAdapter!!.selectedItemCount
            if (count == 0) {
                actionMode?.finish()
            } else {
                actionMode?.title = buildString {
                    append(count)
                        .append(getString(R.string.items_selected))
                }
                actionMode?.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSelectedFolders() {
        try {
            val selectedItemPositions = folderAdapter!!.selectedItems
            for (i in selectedItemPositions.indices.reversed()) {
                deleteFolder(selectedItemPositions[i])
            }
            folderAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteFolder(position: Int) {
        val entity = folderList!![position] as EntityFolders

        lifecycleScope.launch {
            val tempList = AppHelperDb.getFolderById(entity.id.toString())
            try {
                if (tempList != null) {
                    if (tempList.isNotEmpty()) {
                        for (i in tempList.indices) {
                            val path = tempList[i].savedPath
                            val file = File(path)
                            if (file.exists()) {
                                val del = file.delete()
                                if (del) {
                                    MediaScannerConnection.scanFile(
                                        requireActivity(),
                                        arrayOf(path, path),
                                        arrayOf("image/jpg", "video/mp4"),
                                        object :
                                            MediaScannerConnection.MediaScannerConnectionClient {
                                            override fun onMediaScannerConnected() {}
                                            override fun onScanCompleted(path: String, uri: Uri) {
                                                Timber.d(path)
                                            }
                                        })
                                }
                                if (del && i == tempList.size - 1) {
                                    AppHelperDb.removeFolder(entity.id)
                                    folderList!!.removeAt(position)
                                    withContext(Dispatchers.Main) {
                                        folderAdapter!!.notifyDataSetChanged()
                                    }
                                }
                            } else {
                                if (i == tempList.size - 1) {
                                    AppHelperDb.removeFolder(entity.id)
                                    folderList!!.removeAt(position)
                                    withContext(Dispatchers.Main) {
                                        folderAdapter!!.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                        if (folderList!!.size > 0) {
                            savedFragmentBinding.recyclerView.visibility = View.GONE
                        } else {
                            savedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
                        }
                    } else {
                        AppHelperDb.removeFolder(entity.id)
                        folderList!!.removeAt(position)
                        withContext(Dispatchers.Main) {
                            folderAdapter!!.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }
}