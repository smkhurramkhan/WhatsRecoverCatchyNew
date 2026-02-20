package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypreview.PreviewScreen
import com.catchyapps.whatsdelete.basicapputils.getFilenameFromPath
import com.catchyapps.whatsdelete.basicapputils.invisible
import com.catchyapps.whatsdelete.basicapputils.show
import com.catchyapps.whatsdelete.databinding.RowImagesVideoBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import timber.log.Timber
import java.io.File
import java.util.Locale

class RecoverMediaAdapter(
    private val context: Context,
    private var filesList: List<EntityFiles>,
    var activity: Activity,
) : RecyclerView.Adapter<VhRecoverMedia>(), Filterable {
    private var filterList: MutableList<EntityFiles>
    private var selectedIds = SparseArray<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VhRecoverMedia {
        return VhRecoverMedia(
            RowImagesVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VhRecoverMedia, @SuppressLint("RecyclerView") position: Int) {
        val files = filterList[position]
        val path = files.filePath

        if (path?.endsWith(".mp4") == true) {
            holder.hRowVideoImagesBinding.playButtonImage.show()
        } else {
            holder.hRowVideoImagesBinding.playButtonImage.invisible()
        }
        Glide.with(context).asBitmap()
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontAnimate())
            .load(path)
            .centerCrop()
            .into(object : BitmapImageViewTarget(holder.hRowVideoImagesBinding.mainImageView) {
                override fun setResource(resource: Bitmap?) {
                    val circularBitmapDrawable = RoundedBitmapDrawableFactory.create(
                        context.resources, resource
                    )
                    circularBitmapDrawable.cornerRadius = 25f
                    circularBitmapDrawable.setAntiAlias(true)
                    holder.hRowVideoImagesBinding.mainImageView.setImageDrawable(
                        circularBitmapDrawable
                    )
                }
            })
        holder.hRowVideoImagesBinding.imageVideoName.text = path?.getFilenameFromPath()
        if (selectedIds.indexOfKey(position) > -1) {
            holder.hRowVideoImagesBinding.layoutParent.foreground =
                ContextCompat.getColor(context, R.color.colorControlActivated).toDrawable()
        } else {
            holder.hRowVideoImagesBinding.layoutParent.foreground =
                ContextCompat.getColor(
                    context, android.R.color.transparent
                ).toDrawable()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PreviewScreen::class.java)
            intent.putExtra("file_path", filesList[position].filePath)
            context.startActivity(intent)
        }

        holder.hRowVideoImagesBinding.ivDelete.setOnClickListener {
            deleteDialog(position)
        }

        holder.hRowVideoImagesBinding.ivShare.setOnClickListener {
            shareImage(path)
        }

    }

    private fun shareImage(imgPath: String?) {
        try {
            val intentShareFile = Intent(Intent.ACTION_SEND)
            val imageUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                File(imgPath)
            )
            intentShareFile.type = "application/octet-stream"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, imageUri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...")
            intentShareFile.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=" + context.packageName
            )
            context.startActivity(Intent.createChooser(intentShareFile, context.getString(R.string.share)))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun deleteDialog(position: Int) {
        try {
            if (filterList[position].mimeType != null) {
                val builder = AlertDialog.Builder(context)
                val imageVideo =
                    if (filterList[position].mimeType == "image/jpeg") "Image" else "Video"
                builder.setMessage("${context.getString(R.string.delete)} $imageVideo?")
                builder.setPositiveButton(context.getString(R.string.delete)) { dialog: DialogInterface, which: Int ->
                    val file = filterList[position]
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            val path = file.filePath
                            Timber.d("File $path")

                            val deleted = path?.let {
                                val fileObj = File(it)
                                if (fileObj.exists()) {
                                    fileObj.delete()
                                } else {
                                    false
                                }
                            }

                            Timber.d("Deleted $deleted")

                            if (deleted == true) {
                                filterList.removeAt(position)
                                notifyDataSetChanged()
                            }

                        } catch (e: Exception) {
                            Timber.e(e, "Exception while deleting file")
                        }
                    } else {
                        delete(position, file)
                        dialog.cancel()
                    }
                }
                    .setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
                builder.create().show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delete(position: Int, files: EntityFiles) {
        files.filePath?.let {
            val fDelete = File(it)
            if (fDelete.exists()) {
                val delete = fDelete.delete()
                if (delete) {
                    filterList.removeAt(position)
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterList = if (charString.isEmpty()) {
                    filesList.toMutableList()
                } else {
                    val filteredList: MutableList<EntityFiles> = ArrayList()
                    for (row in filesList) {

                        if (row.title?.lowercase(Locale.getDefault())
                                ?.contains(charString.lowercase(Locale.getDefault())) == true
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterList = filterResults.values as MutableList<EntityFiles>
                notifyDataSetChanged()
            }
        }
    }

    fun hAddItems(list: List<EntityFiles>) {
        filesList = list
        filterList = list.toMutableList()
        notifyDataSetChanged()
    }

    fun hGetLists(): List<EntityFiles> {
        return filterList
    }


    fun getItem(position: Int): EntityFiles {
        return filterList[position]
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    init {
        filterList = filesList.toMutableList()
    }


}