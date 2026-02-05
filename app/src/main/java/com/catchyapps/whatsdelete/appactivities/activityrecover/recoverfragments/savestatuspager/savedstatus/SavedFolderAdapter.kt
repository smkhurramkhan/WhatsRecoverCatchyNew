package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.savedstatus

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.util.SparseBooleanArray
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appactivities.activitydetailfolder.WASingleFolderDetailActivity
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.adapterstatusaver.statusaverviewholder.VHFolder
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.basicapputils.MyAppFolderListener
import com.catchyapps.whatsdelete.databinding.PlaylistItemVideoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class SavedFolderAdapter(
    private val list: MutableList<Any>,
    private val context: Context,
    listener: MyAppFolderListener,
    var activity: Activity
) : RecyclerView.Adapter<VHFolder>() {

    private val callback: MyAppFolderListener = listener
    private val hSelectedItems: SparseBooleanArray = SparseBooleanArray()
    var prefs: MyAppSharedPrefs = MyAppSharedPrefs(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHFolder {
        return VHFolder(
            PlaylistItemVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: VHFolder, position: Int) {
        val files = list[position] as EntityFolders
        viewHolder.playlistVideoItemBinding.tvPlaylistName.text = files.playlistName
        val video = files.noOfItems.toString()
        viewHolder.playlistVideoItemBinding.tvVideosCount.text = video
        Glide.with(context)
            .asBitmap()
            .load(files.playListLogo)
            .into(viewHolder.playlistVideoItemBinding.videoThumb)
        viewHolder.playlistVideoItemBinding.videoThumb.setOnClickListener { view: View? ->
            startFolderDetailIntent(position)
        }
        viewHolder.playlistVideoItemBinding.ivOption.setOnClickListener { v: View? ->
            val wrapper: Context = ContextThemeWrapper(context, R.style.PopupMenu)
            val menu = PopupMenu(wrapper, v!!)
            menu.menu.add(Menu.NONE, 1, 1, context.getString(R.string.rename))
            menu.menu.add(Menu.NONE, 2, 1, context.getString(R.string.delete))
            menu.show()
            menu.setOnMenuItemClickListener { item: MenuItem ->
                val i = item.itemId
                when (i) {
                    1 -> {
                        updateCollection(position)
                        return@setOnMenuItemClickListener true
                    }
                    2 -> {
                        deleteCollection(position)
                        return@setOnMenuItemClickListener true
                    }
                    else -> return@setOnMenuItemClickListener true
                }
            }
        }
        if (hSelectedItems[position, false]) {
            viewHolder.playlistVideoItemBinding.lytParent.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        R.color.colorControlActivated
                    )
                )
        } else {
            viewHolder.playlistVideoItemBinding.lytParent.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context, android.R.color.transparent
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is EntityFolders) {
            MENU_ITEM_VIEW_TYPE
        } else {
            NATIVE_EXPRESS_AD_VIEW_TYPE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteCollection(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val entity = list[position] as EntityFolders
            var tempList: List<EntityStatuses>? = null

            tempList = AppHelperDb.getFolderById(entity.id.toString())

            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_folder))
                .setMessage(context.getString(R.string.all_files_in_this_folder_will_be_deleted_permanently))
                .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            if (tempList?.isNotEmpty() == true) {
                                for (i in tempList.indices) {
                                    val path = tempList[i].savedPath
                                    val file = File(path!!)
                                    if (file.exists()) {
                                        val del = file.delete()
                                        if (del) {
                                            MediaScannerConnection.scanFile(
                                                context,
                                                arrayOf(path),
                                                null,
                                                null
                                            )
                                        }
                                        if (del && i == tempList.lastIndex) {
                                            AppHelperDb.removeFolder(entity.id)
                                            list.removeAt(position)
                                            notifyDataSetChanged()
                                        }
                                    } else {
                                        if (i == tempList.lastIndex) {
                                            AppHelperDb.removeFolder(entity.id)
                                            list.removeAt(position)
                                            notifyDataSetChanged()
                                        }
                                    }
                                }
                            } else {
                                AppHelperDb.removeFolder(entity.id)
                                list.removeAt(position)
                                notifyDataSetChanged()
                            }
                            callback.folderDeleted()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        dialog.dismiss()
                    }
                }
                .show()

        }
    }

    private fun updateCollection(position: Int) {
        val entity = list[position] as EntityFolders
        val builder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val view: View = inflater.inflate(R.layout.layout_edit_custom_, null)
        val etCollectionUpdate = view.findViewById<EditText>(R.id.etCollectionUpdate)
        etCollectionUpdate.setText(entity.playlistName)
        etCollectionUpdate.setSelection(entity.playlistName!!.length)
        etCollectionUpdate.requestFocus()
        builder.setView(view)
        val tvOk = view.findViewById<TextView>(R.id.tvCreate)
        val tvCancel = view.findViewById<TextView>(R.id.tvCancel)
        val dialog = builder.create()
        tvOk.setOnClickListener { v: View? ->
            if (Objects.requireNonNull(etCollectionUpdate.text).toString()
                    .trim { it <= ' ' } == ""
            ) {
                MyAppUtils.showToast(context, context.getString(R.string.enter_collection_name))
            } else {
                entity.playlistName = etCollectionUpdate.text.toString().trim { it <= ' ' }
                CoroutineScope(Dispatchers.Main).launch {
                    AppHelperDb.updateFolderByName(
                        entity.playlistName!!,
                        entity.id
                    )
                }

                (list[position] as EntityFolders).playlistName = entity.playlistName
                notifyDataSetChanged()
                dialog.dismiss()
            }
        }
        tvCancel.setOnClickListener { v: View? -> dialog.dismiss() }
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    fun toggleSelection(pos: Int) {
        if (hSelectedItems[pos, false]) {
            hSelectedItems.delete(pos)
        } else {
            hSelectedItems.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
        hSelectedItems.clear()
        notifyDataSetChanged()
    }

    val selectedItemCount: Int
        get() = hSelectedItems.size()
    val selectedItems: List<Int>
        get() {
            val items: MutableList<Int> = ArrayList(hSelectedItems.size())
            for (i in 0 until hSelectedItems.size()) {
                items.add(hSelectedItems.keyAt(i))
            }
            return items
        }

    private fun startFolderDetailIntent(position: Int) {
        val intent = Intent(context, WASingleFolderDetailActivity::class.java)
        intent.putExtra("model", list[position] as EntityFolders)
        context.startActivity(intent)
        ShowInterstitial.showInter(context as AppCompatActivity)
    }

    companion object {
        private const val MENU_ITEM_VIEW_TYPE = 0
        private const val NATIVE_EXPRESS_AD_VIEW_TYPE = 1
    }

}