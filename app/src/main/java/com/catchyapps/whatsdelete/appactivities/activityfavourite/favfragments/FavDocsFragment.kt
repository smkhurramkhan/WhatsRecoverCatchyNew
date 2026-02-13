package com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments

import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.*
import android.widget.ImageView
import androidx.appcompat.view.ActionMode
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activityfavourite.ActivityFavorite
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favadapters.FavoriteDocsAdapter
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.getMimeType
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.logCat
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.showToast
import com.catchyapps.whatsdelete.databinding.DocsFavouriteLayoutBinding
import org.apache.commons.io.comparator.LastModifiedFileComparator
import timber.log.Timber
import java.io.File
import java.util.*

class FavDocsFragment : Fragment(), ActionMode.Callback {
    var adapterFavMedia: FavoriteDocsAdapter? = null
    private lateinit var objectList: MutableList<EntityFiles>
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    lateinit var binding: DocsFavouriteLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DocsFavouriteLayoutBinding.inflate(layoutInflater)
        objectList = ArrayList()

        data
        setUpRecyclerView()


        return binding.root
    }

    private fun openFile(url: String?) {
        logCat(url)
        if (context != null) {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireActivity().packageName + ".provider",
                File(url!!)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            if (url.contains(".doc") || url.contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword")
            } else if (url.contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf")
            } else if (url.contains(".ppt") || url.contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
            } else if (url.contains(".xls") || url.contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel")
            } else if (url.contains(".zip") || url.contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(uri, "application/x-wav")
            } else if (url.contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf")
            } else if (url.contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain")
            } else {
                intent.setDataAndType(uri, "*/*")
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                showToast(context, "you cannot open this file")
            }
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        adapterFavMedia = FavoriteDocsAdapter(objectList,
            requireContext(),
            onClick = { position ->
                if (isMultiSelect) {
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position)
                } else {
                    if (position < objectList.size) {
                        try {
                            openFile(objectList[position].filePath)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            onLongClick = { position, view ->
                val ivoptions = view.findViewById<ImageView>(R.id.ivMore)
                if (!isMultiSelect) {
                    selectedIds = SparseArray()
                    isMultiSelect = true
                    if (actionMode == null) {
                        try {
                            ivoptions.visibility = View.GONE
                            actionMode =
                                (requireActivity() as ActivityFavorite).startSupportActionMode(
                                    this@FavDocsFragment
                                ) //show ActionMode.
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                multiSelect(position)
            }

        )
        binding.recyclerView.adapter = adapterFavMedia
    }

    private val data: Unit
        get() {
            objectList.clear()
            var recoverFilesEntity: EntityFiles
            val targetPath: String
            val cw = ContextWrapper(requireContext())
            val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
            val dir = File(directory, MyAppConstants.WA_FAV_DOC)
            targetPath = dir.path
            binding.tvNotFound.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.nofiles_img, 0, 0)
            val targetDirector = File(targetPath)

            Timber.d("target directory is ${targetDirector.absolutePath}")
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
                    adapterFavMedia!!.notifyDataSetChanged()
                } else {
                    binding.recyclerView.visibility = View.GONE
                    binding.tvNotFound.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = adapterFavMedia!!.getItem(position)
            if (data != null) {
                multi = true
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
                    adapterFavMedia!!.setSelectedIds(selectedIds)
                } else {
                    Timber.d("Action mode is null")
                }
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
        multi = false
        selectedIds = SparseArray()
        adapterFavMedia!!.setSelectedIds(selectedIds)
    }

    private fun alertDeleteConfirmation() {
        val builder = AlertDialog.Builder(activity)

        // if (selectedIds.size() > 1)
        builder.setMessage("Delete " + selectedIds.size() + "selected Images?")

//        else
//            builder.setMessage("Delete selected image?");
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
                    adapterFavMedia!!.notifyDataSetChanged()
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

    private fun shareMultipleFiles() {
        try {
            val selecteduri = ArrayList<Uri>()
            //List<Integer> selectedItemPositions = recyclerViewAdapter.getSelectedItems();
            for (i in 0 until selectedIds.size()) {
                val recoverFileEntity = objectList[selectedIds.keyAt(i)]
                val uri = recoverFileEntity.filePath
                val u = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    File(uri!!)
                )
                selecteduri.add(u)
            }
            actionMode!!.title = ""
            actionMode!!.finish()
            shareMultiple(selecteduri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareMultiple(files: ArrayList<Uri>?) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, "Share files"))
    }

    companion object {
        var multi = false
    }
}