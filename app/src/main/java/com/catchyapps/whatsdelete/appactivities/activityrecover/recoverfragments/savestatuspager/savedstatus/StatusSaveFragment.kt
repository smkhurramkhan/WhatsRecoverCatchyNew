package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.savedstatus

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppFolderListener
import com.catchyapps.whatsdelete.appclasseshelpers.RVTouchListener
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.databinding.FragmentSavedScreenBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class StatusSaveFragment : Fragment(),
    MyAppFolderListener {

    private var isLoadFirstTime = true
    private var folderList: MutableList<Any>? = null
    private var hFolderAdapter: SavedFolderAdapter? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null
    private var prefs: MyAppSharedPrefs? = null

    private lateinit var hSavedFragmentBinding: FragmentSavedScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hSavedFragmentBinding = FragmentSavedScreenBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hSavedFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hInitVar()
        hInitRecyclerView()
    }

    private fun hInitRecyclerView() {
        hFolderAdapter = folderList?.let {
            SavedFolderAdapter(
                it,
                requireActivity(),
                this,
                requireActivity()
            )
        }
        hSavedFragmentBinding.recyclerView.apply {
            adapter = hFolderAdapter
            layoutManager = GridLayoutManager(requireActivity(), 2)
            addOnItemTouchListener(
                RVTouchListener(
                    requireActivity(),
                    this,
                    object :
                        RVTouchListener.ClickListener {
                        override fun onClick(view: View, position: Int) {
                            if (hFolderAdapter!!.selectedItemCount > 0) {
                                enableActionMode(position)
                            }
                        }

                        override fun onLongClick(view: View, position: Int) {
                            enableActionMode(position)
                        }
                    })
            )
        }
    }

    private fun hInitVar() {
        actionModeCallback = ActionModeCallback()
        prefs = MyAppSharedPrefs(requireActivity())
        folderList = ArrayList()
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try {
                isLoadFirstTime = true
                folderList!!.clear()

                AppHelperDb.hGetAllFolders()?.let {
                    folderList!!.addAll(it)
                    Timber.d("Data returened ${it.size}")
                }


                if (folderList!!.size > 0) {
                    hFolderAdapter?.notifyDataSetChanged()
                    hSavedFragmentBinding.layoutNotfound.visibility = View.GONE
                    hSavedFragmentBinding.recyclerView.visibility = View.VISIBLE
                } else {
                    hSavedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
                    hSavedFragmentBinding.recyclerView.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun folderDeleted() {
        if (hFolderAdapter!!.itemCount == 0) {
            hSavedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
            hSavedFragmentBinding.recyclerView.visibility = View.GONE
        } else {
            hSavedFragmentBinding.layoutNotfound.visibility = View.GONE
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
                val alertDialogBuilder = android.app.AlertDialog.Builder(requireActivity())
                alertDialogBuilder.setTitle("Confirm Delete")
                    .setMessage("Are you sure yout want to status the selected folders?")
                    .setPositiveButton("Delete") { dialog: DialogInterface?, which: Int ->
                        try {
                            //todo status selected folders
                            deleteSelectedFolders()
                            mode.finish()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
                        dialog.dismiss()
                        mode.finish()
                    }.show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            hFolderAdapter!!.clearSelections()
            actionMode = null
        }
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            try {
                actionMode =
                    (requireActivity() as MainRecoverActivity).startSupportActionMode(
                        actionModeCallback!!
                    ) //show ActionMode. //show ActionMode.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        try {      /// fix no 4.........................
            hFolderAdapter!!.toggleSelection(position)
            val count = hFolderAdapter!!.selectedItemCount
            if (count == 0) {
                actionMode!!.finish()
            } else {
                actionMode!!.title = "$count items selected"
                actionMode!!.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSelectedFolders() {
        try {
            val selectedItemPositions = hFolderAdapter!!.selectedItems
            for (i in selectedItemPositions.indices.reversed()) {
                Timber.d(selectedItemPositions.size.toString() + "size of the selected items")
                deleteFolder(selectedItemPositions[i])
            }
            hFolderAdapter?.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteFolder(position: Int) {
        val entity = folderList!![position] as EntityFolders
        lifecycleScope.launch(Dispatchers.Main) {
            val tempList = AppHelperDb.hGetfolderById(entity.id.toString())
            try {
                if (tempList?.isNotEmpty() == true) {
                    for (i in tempList.indices) {
                        val path = tempList[i].savedPath
                        val file = File(path!!)
                        if (file.exists()) {
                            val del = file.delete()
                            if (del) {
                                MediaScannerConnection.scanFile(
                                    requireActivity(),
                                    arrayOf(path, path),
                                    arrayOf("image/jpg", "video/mp4"),
                                    object : MediaScannerConnection.MediaScannerConnectionClient {
                                        override fun onMediaScannerConnected() {}
                                        override fun onScanCompleted(path: String, uri: Uri) {
                                            Timber.d(path)
                                        }
                                    })
                            }
                            if (del && i == tempList.size - 1) {
                                AppHelperDb.hRemoveFolder(entity.id)
                                folderList!!.removeAt(position)
                                withContext(Dispatchers.Main) {
                                    hFolderAdapter!!.notifyDataSetChanged()
                                }
                            }
                        } else {
                            if (i == tempList.size - 1) {
                                AppHelperDb.hRemoveFolder(entity.id)
                                folderList!!.removeAt(position)

                                withContext(Dispatchers.Main) {
                                    hFolderAdapter!!.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    if (folderList!!.size > 0) {
                        hSavedFragmentBinding.recyclerView.visibility = View.GONE
                    } else {
                        hSavedFragmentBinding.layoutNotfound.visibility = View.VISIBLE
                    }
                } else {
                    AppHelperDb.hRemoveFolder(entity.id)
                    folderList!!.removeAt(position)
                    withContext(Dispatchers.Main) {
                        hFolderAdapter!!.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (actionMode != null) {
            actionMode!!.finish()
        }
    }
}
