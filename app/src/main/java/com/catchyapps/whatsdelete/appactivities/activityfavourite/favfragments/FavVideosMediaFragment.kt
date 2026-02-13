package com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.getMimeType
import com.catchyapps.whatsdelete.appactivities.activitypreview.PreviewScreen
import com.catchyapps.whatsdelete.appactivities.activityfavourite.ActivityFavorite
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favadapters.FavoriteMediaAdapter
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.databinding.MediaFavouriteBinding
import org.apache.commons.io.comparator.LastModifiedFileComparator
import java.io.File
import java.util.*

class FavVideosMediaFragment : Fragment(), ActionMode.Callback {
    private var mediaFavAdapter: FavoriteMediaAdapter? = null
    private lateinit var objectList: MutableList<EntityFiles>
    private lateinit var binding: MediaFavouriteBinding
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MediaFavouriteBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        objectList = ArrayList()
        data
        setUpRecyclerView()


        return binding.root
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = mediaFavAdapter!!.getItem(position)
            if (actionMode != null) {
                if (selectedIds.indexOfKey(position) > -1) selectedIds.remove(position) else selectedIds.put(
                    position,
                    data.title
                )
                if (selectedIds.size() > 0) actionMode!!.title = selectedIds.size()
                    .toString() + "  Selected" //show selected item count on action mode.
                else {
                    actionMode!!.title = "" //remove item count from action mode.
                    actionMode!!.finish() //hide action mode.
                }
                mediaFavAdapter!!.setSelectedIds(selectedIds)
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.file_menu_select_fragment, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            alertDeleteConfirmation()
            return true
        } else if (item.itemId == R.id.action_share) {
            shareMultipleFiles()
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        isMultiSelect = false
        selectedIds = SparseArray()
        mediaFavAdapter!!.setSelectedIds(selectedIds)
    }

    private fun alertDeleteConfirmation() {
        val builder = AlertDialog.Builder(activity)

        //  if (selectedIds.size() > 1)
        builder.setMessage("Delete " + selectedIds.size() + " selected Videos")
        //        else
//            builder.setMessage("Delete selected video?");
        builder.setPositiveButton("DELETE") { dialog: DialogInterface, which: Int ->
            try {
                for (i in 0 until selectedIds.size()) {
                    val recoverFileEntity = objectList[selectedIds.keyAt(i)]
                    val fdelete = File(recoverFileEntity.filePath!!)
                    if (fdelete.exists()) {
                        fdelete.delete()
                    }
                    objectList.removeAt(selectedIds.keyAt(i))
                }
                if (objectList.size > 0) {
                    mediaFavAdapter!!.notifyDataSetChanged()
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNotFound.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.cancel()
            actionMode!!.title = "" //remove item count from action mode.
            actionMode!!.finish() //
        }.setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 2)
        mediaFavAdapter = FavoriteMediaAdapter(objectList,
            requireContext(),
            onClick = { position, view ->
                if (isMultiSelect) {
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position)
                } else {
                    if (position < objectList.size) {
                        try {
                            val intent = Intent(context, PreviewScreen::class.java)
                            intent.putExtra("file_path", objectList[position].filePath)
                            requireActivity().startActivityForResult(intent, 101)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            onLongClick = { position, view ->
                if (!isMultiSelect) {
                    selectedIds = SparseArray()
                    isMultiSelect = true
                    if (actionMode == null) {
                        try {
                            actionMode =
                                (requireActivity() as ActivityFavorite).startSupportActionMode(
                                    this@FavVideosMediaFragment
                                ) //show ActionMode.
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                multiSelect(position)
            }

        )
        binding.recyclerView.adapter = mediaFavAdapter
    }

    private val data: Unit
        @SuppressLint("NotifyDataSetChanged")
        get() {
            objectList.clear()
            var recoverFilesEntity: EntityFiles
            val targetPath: String
            val cw = ContextWrapper(requireContext())
            val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
            val dir = File(directory, MyAppConstants.WA_FAV_VIDEOS)
            targetPath = dir.path
            binding.tvNotFound.text = getString(R.string.no_video_detected_yet)
            binding.tvNotFound.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.nofiles_img, 0, 0)
            val targetDirector = File(targetPath)
            val files = targetDirector.listFiles()
            try {
                if (files != null) {
                    Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                    for (file in files) {
                        if (file.name.contains(".")) {
                            recoverFilesEntity = EntityFiles()
                            recoverFilesEntity.title = file.name
                            recoverFilesEntity.filePath = file.absolutePath
                            recoverFilesEntity.timeStamp = file.lastModified().toString()
                            recoverFilesEntity.mimeType = getMimeType(file.absolutePath)
                            recoverFilesEntity.fileSize = file.length()
                            objectList.add(recoverFilesEntity)
                        }
                    }
                }
                if (objectList.size > 0) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.tvNotFound.visibility = View.GONE
                    mediaFavAdapter!!.notifyDataSetChanged()
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNotFound.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        R.drawable.nofiles_img,
                        0,
                        0
                    )
                    binding.tvNotFound.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun shareMultipleFiles() {
        try {
            val selecteduri = ArrayList<Uri>()
            for (i in 0 until selectedIds.size()) {
                val uri = objectList[selectedIds.keyAt(i)].filePath
                val u = FileProvider.getUriForFile(
                    requireContext(),
                    requireActivity().packageName + ".provider",
                    File(uri!!)
                )
                selecteduri.add(u)
            }
            actionMode?.title = "" //remove item count from action mode.
            actionMode?.finish()
            shareMultiple(selecteduri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultiple(files: ArrayList<Uri>?) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, "Share files"))
    }
}