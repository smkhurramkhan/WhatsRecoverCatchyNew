package com.catchyapps.whatsdelete.appactivities.activityfavourite.favadapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
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
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.showToast
import com.catchyapps.whatsdelete.appactivities.activityfavourite.faviewholder.VHFavMedia
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.catchyapps.whatsdelete.databinding.ItemImgFavVidLayoutBinding
import java.io.File

class FavoriteMediaAdapter(
    private var recoverFilesEntityList: MutableList<EntityFiles>,
    private val context: Context,
    val onClick: (position: Int, view: View) -> Unit,
    val onLongClick: (position: Int, view: View) -> Unit,

    ) : RecyclerView.Adapter<VHFavMedia>() {
    private var selectedIds = SparseArray<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHFavMedia {
        return VHFavMedia(
            ItemImgFavVidLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: VHFavMedia, position: Int) {
        val files = recoverFilesEntityList[position]
        val path = files.filePath

        if (path!!.endsWith(".mp4")) {
            holder.binding.playButtonImage.visibility = View.VISIBLE
        } else {
            holder.binding.playButtonImage.visibility = View.INVISIBLE
        }


        Glide.with(context).asBitmap()
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).dontAnimate())
            .load(path)
            .centerCrop()
            .into(object : BitmapImageViewTarget(holder.binding.mainImageView) {
                override fun setResource(resource: Bitmap?) {
                    val circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.resources, resource)
                    circularBitmapDrawable.cornerRadius = 25f
                    circularBitmapDrawable.setAntiAlias(true)

                    holder.binding.mainImageView.setImageDrawable(circularBitmapDrawable)
                }
            })

        holder.binding.ivOption.setOnClickListener {
            val popup = PopupMenu(context, holder.binding.ivOption)
            //Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.popup_images_videos, popup.menu)


            popup.menu.getItem(0).setOnMenuItemClickListener {
                true
            }

            popup.menu.getItem(1).setOnMenuItemClickListener {
                val file = File(
                    context.getExternalFilesDir(null)
                        .toString() + "/" + MyAppConstants.ROOT_FOLDER + "/" + MyAppConstants.WA_FAV_IMAGES
                            + "/" + files.title
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

            //registering popup with OnMenuItemClickListener
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
                deleteDialog(position)
                true
            }
            popup.show()
        }


        holder.binding.layoutParent.setOnClickListener {
            /* val intent = Intent(context, PreviewScreen::class.java)
             intent.putExtra("file_path", recoverFileEntityList[position]!!.filePath)
             context.startActivity(intent)*/

            onClick(position, it)

        }

        holder.binding.layoutParent.setOnLongClickListener {
            onLongClick(position, it)
            true
        }

        if (selectedIds.indexOfKey(position) > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.layoutParent.foreground =
                    ColorDrawable(ContextCompat.getColor(context, R.color.colorControlActivated))
            } else holder.binding.layoutParent.background =
                ColorDrawable(ContextCompat.getColor(context, R.color.colorControlActivated))
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                holder.binding.layoutParent.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        android.R.color.transparent
                    )
                )
            else holder.binding.layoutParent.background = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deleteDialog(position: Int) {
        try {
            if (recoverFilesEntityList[position].mimeType != null) {
                val builder = AlertDialog.Builder(context)
                val imageVideo =
                    if (recoverFilesEntityList[position].mimeType == "image/jpeg") "Image" else "Video"
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

    override fun getItemCount(): Int {
        return recoverFilesEntityList.size
    }


    private fun delete(position: Int) {
        val files = recoverFilesEntityList[position]
        val path = files.filePath
        val file = File(path!!)
        try {
            if (file.exists()) {
                val del = file.delete()
                if (del) {
                    showToast(context, "file deleted")
                } else {
                    showToast(context, "file removed from favourites")
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