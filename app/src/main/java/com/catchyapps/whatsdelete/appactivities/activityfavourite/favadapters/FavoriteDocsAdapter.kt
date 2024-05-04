package com.catchyapps.whatsdelete.appactivities.activityfavourite.favadapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activityfavourite.faviewholder.VHFavDocs
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.getFileDateTime
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.showToast
import com.catchyapps.whatsdelete.databinding.ItemListDocumentsLayoutBinding
import timber.log.Timber
import java.io.File

class FavoriteDocsAdapter(
    private var recoverFilesEntityList: MutableList<EntityFiles>,
    private val context: Context,
    val onClick: (position: Int) -> Unit,
    val onLongClick: (position: Int, view: View) -> Unit,


    ) : Adapter<VHFavDocs>() {

    private var selectedIds = SparseArray<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHFavDocs {
        return VHFavDocs(
            ItemListDocumentsLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: VHFavDocs, position: Int) {

        val files = recoverFilesEntityList[position]
        holder.binding.ivDocumentType.setImageResource(getRecourseImage(files.filePath))

        holder.binding.tvDocumentName.text = files.title

        holder.binding.tvSizeTime.text = getFileDateTime(files.timeStamp!!.toLong())


        holder.binding.ivMore.setOnClickListener {
            val popup = PopupMenu(context, holder.binding.ivMore)
            //Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.popup_audio_menu, popup.menu)
            popup.menu.findItem(R.id.item_favorite).isVisible = false
            popup.menu.findItem(R.id.item_unfavorite).isVisible = false

            popup.menu.getItem(0).setOnMenuItemClickListener {
                true
            }

            popup.menu.getItem(1).setOnMenuItemClickListener {
                val file = File(
                    context.getExternalFilesDir(null)
                        .toString() + "/" + MyAppConstants.ROOT_FOLDER + "/" + MyAppConstants.WA_FAV_DOC + "/" + files.title
                )
                val deleted = file.delete()
                if (deleted) {
                    files.setIsfav(false)
                    recoverFilesEntityList.removeAt(position)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Removed from favourites", Toast.LENGTH_LONG).show()
                }
                true
            }

            popup.menu.getItem(2).setOnMenuItemClickListener {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(files.filePath))
                sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                sharingIntent.type = "audio/*"
                sharingIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=" + context.packageName
                )
                context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
                true
            }


            popup.menu.getItem(3).setOnMenuItemClickListener {
                infoDialog(position)
                true
            }


            popup.menu.getItem(4).setOnMenuItemClickListener {
                deleteDialog(position)
                true
            }

            popup.show()
        }

        holder.binding.parent.setOnClickListener {
            onClick(position)
        }

        holder.binding.parent.setOnLongClickListener {
            onLongClick(position, it)
            true
        }

        if (selectedIds.indexOfKey(position) > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.parent.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        R.color.colorControlActivated
                    )
                )
            } else holder.binding.parent.background = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.colorControlActivated
                )
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                holder.binding.parent.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        android.R.color.transparent
                    )
                ) else holder.binding.parent.background = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        }

    }

    @SuppressLint("SetTextI18n")
    private fun infoDialog(position: Int) {
        try {
            val files = recoverFilesEntityList[position]
            val builder = AlertDialog.Builder(context, R.style.CustomAlertDialog)
            val inflater = (context as AppCompatActivity).layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_info_layout, null)
            builder.setView(dialogView)
            val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
            val tvFullName = dialogView.findViewById<TextView>(R.id.tvFullName)
            val tvPath = dialogView.findViewById<TextView>(R.id.tvPath)
            val tvModificationDate = dialogView.findViewById<TextView>(R.id.tvModificationDate)
            val tvSize = dialogView.findViewById<TextView>(R.id.tvSize)
            tvFileName.text = files.title
            tvFullName.text = files.title
            tvPath.text = files.filePath
            tvModificationDate.text = getFileDateTime(files.timeStamp!!.toLong())
            val file = File(files.filePath!!)
            var file_size = (file.length() / 1024).toString().toInt()
            if (file_size < 1024) {
                tvSize.text = "$file_size Kb"
                Timber.d(file_size.toString() + "")
            } else {
                file_size /= 1024
                tvSize.text = "$file_size Mb"
                Timber.d(file_size.toString() + "")
            }
            val alertDialog = builder.create()
            alertDialog.setCanceledOnTouchOutside(true)
            alertDialog.show()
        } catch (ignored: Exception) {
        }
    }

    override fun getItemCount(): Int {
        return recoverFilesEntityList.size
    }

    private fun getRecourseImage(url: String?): Int {
        return if (url!!.contains(".doc") || url.contains(".docx")) {
            R.drawable.img_word_
        } else if (url.contains(".pdf")) {
            // PDF file
            R.drawable.pdf_img
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            R.drawable.point_power_img
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            R.drawable.excel_img
        } else if (url.contains(".zip") || url.contains(".rar")) {
            // WAV audio file
            R.drawable.img_zip
        } else if (url.contains(".txt")) {
            // Text file
            R.drawable.img_text
        } else {
            R.drawable.alldocumentpng
        }
    }


    private fun deleteDialog(position: Int) {
        try {
            if (recoverFilesEntityList[position]!!.mimeType != null) {
                val builder = android.app.AlertDialog.Builder(context)
                val imageVideo =
                    if (recoverFilesEntityList[position]!!.mimeType == "image/jpeg") "Image" else "Video"
                builder.setMessage("Delete selected $imageVideo?")
                builder.setPositiveButton("DELETE") { dialog: DialogInterface, which: Int ->
                    delete(position)
                    notifyDataSetChanged()
                    dialog.cancel()
                }
                    .setNegativeButton("CANCEL") { dialog: DialogInterface, which: Int -> dialog.cancel() }
                builder.create().show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delete(position: Int) {
        val files = recoverFilesEntityList[position]
        val path = files.filePath
        val file = File(path!!)
        try {
            if (file.exists()) {
                val del = file.delete()
                if (del) {
                    showToast(context, "file removed from favourites")
                } else {
                    showToast(context, "unable to delete file")
                }
            }
            recoverFilesEntityList.removeAt(position)
            notifyDataSetChanged()
        } catch (e: Exception) {
            // TODO let the user know the file couldn't be deleted
            e.printStackTrace()
        }
    }

    fun getItem(position: Int): EntityFiles {
        return recoverFilesEntityList[position]
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }


}