package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils.Companion.getFileDateTime
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils.Companion.showToast
import com.catchyapps.whatsdelete.databinding.ItemAudioMediaLayoutBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import timber.log.Timber
import java.io.*
import java.util.*

class AudioMediaAdapter(
    private val context: Context,
    private var filesList: List<EntityFiles?>,
    var listener: CallBack,
    var activity: Activity,
) : RecyclerView.Adapter<VhRecoverAudio>(), Filterable {
    private var filterList: MutableList<EntityFiles?>
    private var selectedIds = SparseArray<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): VhRecoverAudio {

        return VhRecoverAudio(
            ItemAudioMediaLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        )
    }

    fun hAddItems(list: List<EntityFiles?>) {
        filesList = list
        filterList = list.toMutableList()
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: VhRecoverAudio, position: Int) {
        val files = filterList[position]
        val path = files!!.filePath
        if (files.isPlaying) {
            holder.itemView.setBackgroundColor(
                context.resources.getColor(
                    R.color.colorControlActivated
                )
            )
        } else {
            holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
        }
        holder.hCustomDocumentLayoutBinding.tvSizeTime.text = files.timeStamp?.toLong()
            ?.let { getFileDateTime(it) }
        holder.hCustomDocumentLayoutBinding.tvDocumentName.text = files.title
        if (files.filePath?.endsWith(".mp3") == true) {
            holder.hCustomDocumentLayoutBinding.ivDocumentType.setImageResource(R.drawable.ic_audio_media)
        } else {
            holder.hCustomDocumentLayoutBinding.ivDocumentType.setImageResource(R.drawable.notes_voice_img)
        }
       /* holder.hCustomDocumentLayoutBinding.root.setOnClickListener {
            changeTintColor()
            files.isPlaying = true
            notifyDataSetChanged()
            listener.playAudio(position)
        }*/

        holder.hCustomDocumentLayoutBinding.iconPlayer.setOnClickListener {
            changeTintColor()
            files.isPlaying = true
            notifyDataSetChanged()
            listener.playAudio(position)
        }


        holder.hCustomDocumentLayoutBinding.ivMore.setOnClickListener {
            val popup = PopupMenu(context, holder.hCustomDocumentLayoutBinding.ivMore)
            //Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.popup_audio_menu, popup.menu)


            popup.menu.getItem(0).setOnMenuItemClickListener {
                val name = files.filePath?.substring(files.filePath!!.lastIndexOf("/") + 1)
                files.setIsfav(true)
                if (files.filePath!!.endsWith(".opus")) {
                    checkVoiceFolder()


                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_VOICE)

                    copyFile(path, dir.absolutePath + "/" + name)
                    Toast.makeText(
                        context,
                        context.getString(R.string.add_to_favourite),
                        Toast.LENGTH_LONG
                    ).show()

                    ShowInterstitial.showInter(context as AppCompatActivity)


                } else {
                    checkAudioFolder()
                    val cw = ContextWrapper(context)
                    val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
                    val dir = File(directory, MyAppConstants.WA_FAV_AUDIO)

                    copyFile(path, dir.absolutePath + "/" + name)
                    Toast.makeText(
                        context,
                        context.getString(R.string.add_to_favourite),
                        Toast.LENGTH_LONG
                    ).show()
                }


                true
            }

            if (files.isIsfav) {
                popup.menu.findItem(R.id.item_favorite).isVisible = false
                popup.menu.findItem(R.id.item_unfavorite).isVisible = true
            }

            popup.menu.getItem(1).setOnMenuItemClickListener {
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
                context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share)))
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
        if (selectedIds.indexOfKey(position) > -1) {
            holder.hCustomDocumentLayoutBinding.parent.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context, R.color
                        .colorControlActivated
                )
            )
        } else {
            holder.hCustomDocumentLayoutBinding.parent.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context, android.R.color.transparent
                    )
                )
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
                    val filteredList: MutableList<EntityFiles?> = ArrayList()
                    for (row in filesList) {
                        if (row != null) {
                            if (row.filePath?.lowercase(Locale.ROOT)
                                    ?.contains(charString.lowercase(Locale.ROOT)) == true
                            ) {
                                filteredList.add(row)
                            }
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterList = filterResults.values as MutableList<EntityFiles?>
                notifyDataSetChanged()
            }
        }
    }

    fun hGetLists(): List<EntityFiles?> {
        return filterList
    }


    private fun deleteDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.are_you_sure_you_want_to_delete))
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.delete)) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            val files = filterList[position]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                try {
                    Timber.d("File ${files?.fileUri}")
                    val delete = DocumentFile.fromSingleUri(
                        context,
                        Uri.parse(files?.fileUri)
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
                delete(position)
            }
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun infoDialog(position: Int) {
        try {
            if (filterList[position] != null) {
                val files = filterList[position]
                val builder = AlertDialog.Builder(context)
                val inflater = (context as AppCompatActivity).layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_info_layout, null)
                builder.setView(dialogView)
                val tvFileName = dialogView.findViewById<TextView>(R.id.tvFileName)
                val tvFullName = dialogView.findViewById<TextView>(R.id.tvFullName)
                val tvPath = dialogView.findViewById<TextView>(R.id.tvPath)
                val tvModificationDate = dialogView.findViewById<TextView>(R.id.tvModificationDate)
                val tvSize = dialogView.findViewById<TextView>(R.id.tvSize)
                tvFileName.text = files!!.title
                tvFullName.text = files.title
                tvPath.text = files.filePath
                tvModificationDate.text = files.timeStamp?.toLong()?.let { getFileDateTime(it) }
                val file = File(files.filePath)
                var file_size = (file.length() / 1024).toString().toInt()
                if (file_size < 1024) {
                    tvSize.text = "$file_size Kb"
                } else {
                    file_size /= 1024
                    tvSize.text = "$file_size Mb"
                }
                val alertDialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(true)
                alertDialog.show()
            }
        } catch (ignored: Exception) {
        }
    }

    private fun delete(position: Int) {
        if (filterList[position] != null) {
            val files = filterList[position]
            val path = files?.filePath
            val file = File(path)
            try {
                if (file.exists()) {
                    val del = file.delete()
                    if (del) {
                        try {
                            MediaScannerConnection.scanFile(
                                context, arrayOf(path, path), arrayOf("application/audio"),
                                object : MediaScannerConnection.MediaScannerConnectionClient {
                                    override fun onMediaScannerConnected() {}
                                    override fun onScanCompleted(path: String, uri: Uri) {}
                                })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        showToast(context, context.getString(R.string.something_went_wrong))
                    }
                }
                filterList.removeAt(position)
                notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface CallBack {
        fun playAudio(itemPos: Int)
    }

    fun getItem(position: Int): EntityFiles {
        return getItem(position)
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    private fun changeTintColor() {
        for (i in filterList.indices) {
            if (filterList[i]?.isPlaying == true) {
                filterList[i]?.isPlaying = false
            }
        }
    }

    init {
        filterList = filesList.toMutableList()
    }

    private fun checkAudioFolder() {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)


        val dir = File(directory, MyAppConstants.WA_FAV_AUDIO)


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


    private fun checkVoiceFolder() {
        val cw = ContextWrapper(context)
        val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
        val dir = File(directory, MyAppConstants.WA_FAV_VOICE)
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