package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.databinding.RowImagesVideoBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activitypreview.PreviewScreen
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
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

    override fun onBindViewHolder(holder: VhRecoverMedia, position: Int) {
        val files = filterList[position]
        val path = files.filePath

        if (path?.endsWith(".mp4") == true) {
            holder.hRowVideoImagesBinding.playButtonImage.visibility = View.VISIBLE
            holder.hRowVideoImagesBinding.tvPlaylistName.visibility = View.VISIBLE
        } else {
            holder.hRowVideoImagesBinding.playButtonImage.visibility = View.INVISIBLE
            holder.hRowVideoImagesBinding.tvPlaylistName.visibility = View.INVISIBLE
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
        holder.hRowVideoImagesBinding.tvPlaylistName.text = files.title
        if (selectedIds.indexOfKey(position) > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.hRowVideoImagesBinding.layoutParent.foreground =
                    ColorDrawable(ContextCompat.getColor(context, R.color.colorControlActivated))
            } else holder.hRowVideoImagesBinding.layoutParent.background =
                ColorDrawable(ContextCompat.getColor(context, R.color.colorControlActivated))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) holder.hRowVideoImagesBinding.layoutParent.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context, android.R.color.transparent
                    )
                ) else holder.hRowVideoImagesBinding.layoutParent.background =
                ColorDrawable(
                    ContextCompat.getColor(
                        context, android.R.color.transparent
                    )
                )
        }
        holder.hRowVideoImagesBinding.ivOption.setOnClickListener {
            Timber.d("Click")
            val popup = PopupMenu(context, holder.hRowVideoImagesBinding.ivOption)

            popup.menuInflater.inflate(R.menu.popup_images_videos, popup.menu)

            popup.menu.getItem(0).setOnMenuItemClickListener {
                val name: String = path!!.substring(path.lastIndexOf("/") + 1)
                files.setIsfav(true)
                if (path.endsWith(".mp4")) {
                    checkVIDEOFolder()

                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_VIDEOS)

                    copyFile(path, dir.absolutePath + "/" + name)
                    Toast.makeText(
                        context,
                        context.getString(R.string.added_to_favourites),
                        Toast.LENGTH_LONG
                    ).show()

                    ShowInterstitial.showInter(context as AppCompatActivity)
                } else {
                    checkImageFolder()

                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_IMAGES)

                    copyFile(path, dir.absolutePath + "/" + name)
                    Toast.makeText(
                        context,
                        context.getString(R.string.added_to_favourites),
                        Toast.LENGTH_LONG
                    ).show()
                    ShowInterstitial.showInter(context as AppCompatActivity)
                }


                true
            }

            if (files.isIsfav) {
                popup.menu.findItem(R.id.item_favorite).isVisible = false
                popup.menu.findItem(R.id.item_unfavorite).isVisible = true
            }

            popup.menu.getItem(1).setOnMenuItemClickListener {
                val file: File = if (path!!.endsWith(".mp4")) {
                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_VIDEOS)

                    File(dir.absolutePath + "/" + files.title)
                } else {
                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_IMAGES)

                    File(dir.absolutePath + "/" + files.title)

                }
                val deleted = file.delete()
                if (deleted) {
                    files.setIsfav(false)
                    Toast.makeText(
                        context, context.getString(R.string.removed_from_favourites),
                        Toast.LENGTH_LONG
                    ).show()
                }
                true
            }



            popup.menu.getItem(2).setOnMenuItemClickListener {
                files.filePath?.let { shareImage(it) }
                true
            }
            popup.menu.getItem(3).setOnMenuItemClickListener {
                deleteDialog(position)
                true
            }
            popup.show()
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PreviewScreen::class.java)
            intent.putExtra("file_path", filesList[position].filePath)
            context.startActivity(intent)
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
                            Timber.d("File ${file.filePath}")
                            val delete = DocumentFile.fromSingleUri(
                                context,
                                Uri.parse(file.filePath)
                            )?.delete()
                            Timber.d("Deleted $delete")
                            if (delete == true) {
                                filterList.removeAt(position)
                                notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            Timber.d("Exception ${e.message}")
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
        val fdelete = File(files.filePath)
        if (fdelete.exists()) {
            val delete = fdelete.delete()
            if (delete) {
                filterList.removeAt(position)
                notifyDataSetChanged()
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


    private fun checkVIDEOFolder() {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)


        val dir = File(directory, MyAppConstants.WA_FAV_VIDEOS)


        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
            Timber.d("Created")

            Timber.d("_----------------------------------------------------------------------------_")
            Timber.d("create directory path ${dir.absolutePath}")
        }
        if (isDirectoryCreated) {
            Timber.d("Already Created")
        }
    }


    private fun checkImageFolder() {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
        val dir = File(directory, MyAppConstants.WA_FAV_IMAGES)
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
            Timber.d("Created")

            Timber.d("_----------------------------------------------------------------------------_")
            Timber.d("create directory path ${dir.absolutePath}")

        }
        if (isDirectoryCreated) {
            Timber.d("Already Created")
        }
    }


    private fun copyFile(inputPath: String?, outputPath: String?) {
        val `in`: InputStream
        val out: OutputStream
        try {
            `in` = FileInputStream(inputPath)
            out = FileOutputStream(outputPath)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()

            // write the output file (You have now copied the file)
            out.flush()
            out.close()
        } catch (fnfe1: java.lang.Exception) {
            fnfe1.printStackTrace()
        }
    }

}