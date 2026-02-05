package com.catchyapps.whatsdelete.appactivities.activityshotscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.SparseBooleanArray
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityScreenShots
import com.catchyapps.whatsdelete.appactivities.activitypreview.MediaPreviewScreen
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.catchyapps.whatsdelete.databinding.ViewScreenshotsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ShotsAdapter(
    private val list: MutableList<Any>,
    private val context: Context,
    var activity: Activity,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val hSelectedItems: SparseBooleanArray = SparseBooleanArray()
    private val actionModeCallback: ActionModeCallback
    private var actionMode: ActionMode? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ScreenShotViewHolder(
            ViewScreenshotsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }


    override fun getItemViewType(position: Int): Int {
        return if (list[position] is EntityScreenShots) {
            MENU_ITEM_VIEW_TYPE
        } else {
            NATIVE_EXPRESS_AD_VIEW_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ScreenShotViewHolder
        val screenShotsEntity = list[position] as EntityScreenShots
        Glide.with(context).load(screenShotsEntity.path).transform(CenterCrop(), RoundedCorners(25))
            .into(viewHolder.screenshotsViewBinding.imgscreenshots)
        toggleCheckedIcon(viewHolder, position)
        holder.itemView.setOnClickListener { view: View? ->
            if (selectedItemCount > 0) {
                enableActionMode(position)
            } else {
                val intent = Intent(context, MediaPreviewScreen::class.java)
                intent.putExtra("file_path", screenShotsEntity.path)
                context.startActivity(intent)
                ShowInterstitial.showInter(context as AppCompatActivity)
            }
        }
        viewHolder.itemView.setOnLongClickListener { view: View? ->
            enableActionMode(position)
            true
        }
        viewHolder.screenshotsViewBinding.btnscreenshot.setOnClickListener { view: View? ->
            shareImage(
                screenShotsEntity.path!!
            )
        }
        viewHolder.screenshotsViewBinding.btndeletechat.setOnClickListener { view: View? ->
            val builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.delete_selected_image))
            builder.setPositiveButton(context.getString(R.string.delete)) { dialog: DialogInterface, which: Int ->
                CoroutineScope(Dispatchers.IO).launch {
                    dialog.cancel()

                    deleteImage(position)
                    notifyDataSetChanged()
                }
            }
                .setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
            builder.create().show()
        }
    }

    private fun shareImage(imgPath: String) {
        try {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val imageUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                File(imgPath)
            )
            intentShareFile.type = "application/octet-stream"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, imageUri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share))
            intentShareFile.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=" + context.packageName
            )
            context.startActivity(Intent.createChooser(intentShareFile, context.getString(R.string.share)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


    private fun toggleCheckedIcon(holder: ScreenShotViewHolder, position: Int) {
        if (hSelectedItems[position, false]) {
            holder.screenshotsViewBinding.cardscreenshots.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.colorControlActivated
                )
            )
        } else {
            holder.screenshotsViewBinding.cardscreenshots.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context, android.R.color.transparent
                    )
                )
        }
    }

    private fun toggleSelection1(pos: Int) {
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

    private val selectedItemCount: Int
        get() = hSelectedItems.size()
    private val selectedItems: List<Int>
        get() {
            val items: MutableList<Int> = ArrayList(hSelectedItems.size())
            for (i in 0 until hSelectedItems.size()) {
                items.add(hSelectedItems.keyAt(i))
            }
            return items
        }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = (context as AppCompatActivity).startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        toggleSelection1(position)
        val count = selectedItemCount
        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = buildString {
                append(count)
                    .append(context.getString(R.string.items_selected))
            }
            actionMode?.invalidate()
        }
    }

    private inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.screenshot_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        @SuppressLint("NonConstantResourceId")
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (item.itemId == R.id.action_delete) {
                val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                builder.setMessage(context.getString(R.string.are_you_sure_want_to_status_this_file))
                builder.setPositiveButton(context.getString(R.string.yes)) { dialog: DialogInterface, which: Int ->
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteVODs()
                    }
                    mode.finish()
                    dialog.dismiss()
                }
                builder.setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface, which: Int ->
                    mode.finish()
                    dialog.dismiss()
                }
                builder.create().show()
                return true
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            clearSelections()
            actionMode = null
        }
    }

    private suspend fun deleteVODs() {
        val selectedItemPositions = selectedItems
        for (i in selectedItemPositions.indices.reversed()) {
            deleteImage(selectedItemPositions[i])
        }
        notifyDataSetChanged()
    }

    private suspend fun deleteImage(position: Int) {
        if (list[position] is EntityScreenShots) {
            val screenShotsEntity = list[position] as EntityScreenShots
            val file = File(screenShotsEntity.path)
            if (file.exists()) {
                if (file.delete()) {
                    AppHelperDb.deleteScreenShotById(screenShotsEntity.id)
                    list.removeAt(position)
                    //  notifyDataSetChanged();
                } else {
                    AppHelperDb.deleteScreenShotById(screenShotsEntity.id)
                    list.removeAt(position)
                    // notifyDataSetChanged();
                }
            } else {
                AppHelperDb.deleteScreenShotById(screenShotsEntity.id)
                list.removeAt(position)
                //notifyDataSetChanged();
            }
        }
    }

    companion object {
        private const val MENU_ITEM_VIEW_TYPE = 0
        private const val NATIVE_EXPRESS_AD_VIEW_TYPE = 1
    }

    init {
        actionModeCallback = ActionModeCallback()
    }
}