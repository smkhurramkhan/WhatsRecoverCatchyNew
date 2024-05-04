package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.ActivityStatusMain
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.ActivityPreviewStatusScreen
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.adapterstatusaver.AdaptersaverPlayList
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.adapterstatusaver.statusaverviewholder.VHStatus
import com.catchyapps.whatsdelete.basicapputils.getPath
import com.catchyapps.whatsdelete.databinding.StatusesItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


class StatusSaveAdapter(
    private val context: Context,
    private val isShow: Boolean,
    var activity: Activity
) : RecyclerView.Adapter<VHStatus>() {

    private var filesList = listOf<Any>()
    private val selected_items: SparseBooleanArray = SparseBooleanArray()
    private var folderId: Long = 0
    private var ids: List<Int>
    var prefs: MyAppSharedPrefs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHStatus {
        return VHStatus(
            StatusesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        viewHolder: VHStatus,
        position: Int
    ) {
        val hStatusesEntity = filesList[position] as EntityStatuses


            viewHolder.itemStatusesBinding.itemSize.setText(VMSaveStatus.itemSize?.let {
                formatSize(
                    it
                )
            })





        val path = if (hStatusesEntity.savedPath != null) hStatusesEntity.savedPath
        else hStatusesEntity.path

        if (hStatusesEntity.type.equals(hImageType)) {
            viewHolder.itemStatusesBinding.playButtonImage.visibility = View.INVISIBLE

        } else {
            viewHolder.itemStatusesBinding.playButtonImage.visibility = View.VISIBLE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var hImageUri = hStatusesEntity.uri
            if (hImageUri == null) {
                hImageUri = hStatusesEntity.path?.toUri()
            }

            Timber.d("Image Uri$hImageUri")
            Glide.with(context)
                .load(hImageUri)
                .into(
                    viewHolder
                        .itemStatusesBinding.mainImageView
                )


        } else {
            Glide.with(context)
                .load(path)
                .into(
                    viewHolder
                        .itemStatusesBinding.mainImageView
                )
        }

        if (selected_items[position, false]) {
            viewHolder.itemStatusesBinding.layoutParent.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    R.color.colorControlActivated
                )
            )
        } else {
            viewHolder.itemStatusesBinding.layoutParent.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            )
        }
        viewHolder.itemView.setOnClickListener {
            Timber.d("hStatus model ${hStatusesEntity.filename}")
            val intent = Intent(context, ActivityPreviewStatusScreen::class.java)
            intent.putExtra("statusObject", hStatusesEntity)
            context.startActivity(intent)
            ShowInterstitial.showAdmobInter(context as AppCompatActivity)
        }
        if (isShow) {
            viewHolder.itemStatusesBinding.download.visibility = View.GONE
            viewHolder.itemStatusesBinding.reshare.visibility = View.GONE
        } else {
            viewHolder.itemStatusesBinding.download.visibility = View.VISIBLE
            viewHolder.itemStatusesBinding.reshare.visibility = View.VISIBLE
        }
        viewHolder.itemStatusesBinding.download.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                hShowPlaylistDialog(hStatusesEntity)
            }
        }
        viewHolder.itemStatusesBinding.reshare.setOnClickListener {
            if (hStatusesEntity.filename?.endsWith(".mp4") == true) {
                reWAStatus(true, hStatusesEntity)
            } else {
                reWAStatus(false, hStatusesEntity)
            }
        }
    }


    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (filesList[position] is EntityStatuses) {
            MENU_ITEM_VIEW_TYPE
        } else {
            NATIVE_EXPRESS_AD_VIEW_TYPE
        }
    }

    fun toggleSelection(pos: Int) {
        if (selected_items[pos, false]) {
            selected_items.delete(pos)
        } else {
            selected_items.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
        selected_items.clear()
        notifyDataSetChanged()
    }

    val selectedItemCount: Int
        get() = selected_items.size()
    val selectedItems: List<Int>
        get() {
            val items: MutableList<Int> = ArrayList(selected_items.size())
            for (i in 0 until selected_items.size()) {
                items.add(selected_items.keyAt(i))
            }
            return items
        }

    private fun reWAStatus(isVideo: Boolean, hStatusesEntity: EntityStatuses) {
        Timber.d("reWAStatus ${hStatusesEntity.filename}")
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        Timber.d("sharing path null")
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, hStatusesEntity.uri)
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (!isVideo) {
            whatsappIntent.type = "image/*"
        } else {
            whatsappIntent.type = "video/*"
        }
        whatsappIntent.setPackage("com.whatsapp")
         try {
            context.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            MyAppUtils.showToast(context, context.getString(R.string.whatsapp_not_found))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createPlayListDialog(hStatusesEntity: EntityStatuses) {
        Timber.d("createPlayListDialog ${hStatusesEntity.filename}")

        val inflater = (context as ActivityStatusMain).layoutInflater
        val builder = AlertDialog.Builder(context)
        val sheetView: View = inflater.inflate(
            R.layout.dialog_playlist_create_layout,
            context.window.decorView.rootView as ViewGroup,
            false
        )
        builder.setView(sheetView)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        val etPlayListName = dialog.findViewById<EditText>(R.id.et_playlist_name)
        val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val tvCreate = dialog.findViewById<TextView>(R.id.tvCreate)!!
        tvCreate.setOnClickListener { view: View? ->
            CoroutineScope(Dispatchers.Main).launch {
                assert(etPlayListName != null)
                val playlistName = etPlayListName!!.text.toString()
                var foldersEntityList: List<EntityFolders>? = null

                foldersEntityList = AppHelperDb.hGetfolderByName(playlistName)
                if (playlistName.isEmpty()) MyAppUtils.snackBar(
                    context,
                    context.getString(R.string.folder_name_can_t_be_empty)
                ) else if (foldersEntityList?.isNotEmpty() == true) {
                    MyAppUtils.showToast(context,
                        context.getString(R.string.folder_already_exist_with_the_same_name))
                } else {
                    dialog.dismiss()
                    val foldersEntity =
                        EntityFolders()
                    foldersEntity.noOfItems = 0
                    foldersEntity.playlistName = playlistName

                    folderId = AppHelperDb.hInsertFolder(foldersEntity)
                    foldersEntity.id = folderId.toInt()
                    if (folderId > 0) {
                        hShowPlaylistDialog(hStatusesEntity)
                    } else MyAppUtils.showToast(context,
                        context.getString(R.string.failed_to_create_folder))
                }
            }
        }

        tvCancel?.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                dialog.dismiss()
                hShowPlaylistDialog(hStatusesEntity)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun hShowPlaylistDialog(hStatusesEntity: EntityStatuses) {
        ids = ArrayList()
        val inflater = (context as ActivityStatusMain).layoutInflater
        val builder = AlertDialog.Builder(context)
        val sheetView: View = inflater.inflate(
            R.layout.playlist_dialog_,
            context.window.decorView.rootView as ViewGroup,
            false
        )
        builder.setView(sheetView)
        val layoutCreatePlayList: LinearLayout? =
            sheetView.findViewById(R.id.layout_create_playlist)
        val tvOk = sheetView.findViewById<TextView>(R.id.tvOk)
        val tvCancel = sheetView.findViewById<TextView>(R.id.tvCancel)
        val recyclerViewPlayList: RecyclerView = sheetView.findViewById(R.id.recycler_view_playlist)
        var playListEntities: List<EntityFolders>? = null

        playListEntities = AppHelperDb.hGetAllFolders()
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerViewPlayList.layoutManager = mLayoutManager
        recyclerViewPlayList.itemAnimator = DefaultItemAnimator()
        if (playListEntities?.isNotEmpty() == true)
            playListEntities[0].isCheck = true
        recyclerViewPlayList.adapter = AdaptersaverPlayList(playListEntities, context)
        val dialog = builder.create()
        if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        tvOk.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (playListEntities?.isNotEmpty() == true) {
                    var isCheck = false
                    for (i in playListEntities.indices) {
                        if (playListEntities[i].isCheck) {
                            dialog.dismiss()
                            val folderEntity = playListEntities[i]
                            var isAvailable = false
                            isCheck = true
                            folderId = folderEntity.id.toLong()
                            val list: List<EntityStatuses>? = null

                            AppHelperDb.hGetfolderById(
                                folderEntity.id.toString()
                            )

                            if (list?.isNotEmpty() == true) {
                                for (j in list.indices) {
                                    if (list[j].path == hStatusesEntity.path) {
                                        isAvailable = true
                                    }
                                }
                            }
                            if (!isAvailable) {
                                downloadStatus(
                                    COLLECTION_ALREADY_CREATED,
                                    folderId.toInt(),
                                    hStatusesEntity
                                )
                            } else {

                                if (hStatusesEntity.type == hVideoType) {
                                    MyAppUtils.showToast(context,
                                        context.getString(R.string.video_already_added))
                                } else {
                                    MyAppUtils.showToast(context,
                                        context.getString(R.string.image_already_added)
                                    )
                                }
                            }
                        }
                    }
                    if (!isCheck) {
                        MyAppUtils.showToast(context,
                            context.getString(R.string.create_or_select_collection_first))
                    }
                } else {
                    MyAppUtils.showToast(context,
                        context.getString(R.string.create_or_select_collection_first))
                }
            }
        }

        tvCancel?.setOnClickListener { dialog.dismiss() }
        assert(layoutCreatePlayList != null)
        layoutCreatePlayList?.setOnClickListener {
            dialog.dismiss()
            CoroutineScope(Dispatchers.Main).launch {
                createPlayListDialog(hStatusesEntity)
            }
        }
    }

    private fun checkFolder() {
        val path = Environment.getExternalStorageDirectory()
            .toString() + "/" + MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_STATUS


        val dir = File(path)
        var isDirectoryCreated = dir.exists()
        if (!isDirectoryCreated) {
            isDirectoryCreated = dir.mkdir()
        }
        if (isDirectoryCreated) {
            Timber.d("Already Created ${dir.absolutePath}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downloadStatus(flag: Int, fid: Int, hStatusesEntity: EntityStatuses) {
        Timber.d("downloadStatus ${hStatusesEntity.filename}")

        val foldersEntity =
            EntityFolders()
        val filename = hStatusesEntity.path?.substring(hStatusesEntity.path!!.lastIndexOf("/") + 1)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                hWriteDataForPostAndroid10(hStatusesEntity)
            } else {
                hWriteDataForPreAndroid11(hStatusesEntity)

            }
        } catch (e: Exception) {
            Timber.d("Write Exception ${e.message}")
        }

        if (flag == NEW_COLLECTION_CREATED) {
            foldersEntity.playListLogo = hStatusesEntity.savedPath
            hStatusesEntity.folderId = fid.toString()

            AppHelperDb.hInsertSattus(hStatusesEntity)
            AppHelperDb.hUpdateFolderById(
                foldersEntity.playListLogo,
                foldersEntity.noOfItems,
                folderId.toInt()
            )
        } else {
            foldersEntity.noOfItems = foldersEntity.noOfItems + 1
            foldersEntity.playListLogo = hStatusesEntity.savedPath
            val statusesEntity = EntityStatuses()
            statusesEntity.filename = filename
            statusesEntity.uri = hStatusesEntity.uri
            statusesEntity.type = hStatusesEntity.type
            statusesEntity.folderId = fid.toString()
            statusesEntity.path = hStatusesEntity.savedPath
            statusesEntity.name = filename
            statusesEntity.fileSize = hStatusesEntity.fileSize
            statusesEntity.savedPath = hStatusesEntity.savedPath
            statusesEntity.sharedPath = hStatusesEntity.sharedPath

            AppHelperDb.hInsertSattus(statusesEntity)
            AppHelperDb.hUpdateFolderById(
                foldersEntity.playListLogo,
                foldersEntity.noOfItems,
                fid
            )
        }
        showToastOnUi()
    }

    private fun hWriteDataForPreAndroid11(hStatusesEntity: EntityStatuses) {
        Timber.d("hWriteDataForPreAndroid11 ${hStatusesEntity.filename}")

        checkFolder()
        val file = File(hStatusesEntity.path!!)
        val filename = hStatusesEntity.path!!.substring(hStatusesEntity.path!!.lastIndexOf("/") + 1)
        val destPath = Environment.getExternalStorageDirectory()
            .toString() + "/" + MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_STATUS
        val destFile = File(destPath)
        try {
            FileUtils.copyFileToDirectory(file, destFile)
            //ShowInterstitial.showInter(activity)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        hStatusesEntity.savedPath = destPath + filename

    }
    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hWriteDataForPostAndroid10(hStatusesEntity: EntityStatuses) {

        Timber.d("hWriteDataForPostAndroid10 ${hStatusesEntity.filename}")

        val contentResolver = context.contentResolver
        var hItemUri: Uri?

        hStatusesEntity.uri?.let {
            context.contentResolver.openFileDescriptor(it, "r")?.use { fileDescriptor ->
                val hFIPS = FileInputStream(fileDescriptor.fileDescriptor)

                val hValues = ContentValues()
                hValues.put(MediaStore.MediaColumns.DISPLAY_NAME, hStatusesEntity.filename)
                hValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/" + MyAppUtils.WA_STATUS
                )
                hValues.put(MediaStore.MediaColumns.MIME_TYPE, hImageType)
                hItemUri =
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, hValues)

                contentResolver.openOutputStream(hItemUri!!).use {
                    it?.write(hFIPS.readBytes())
                }
                hStatusesEntity.savedPath = getPath(context, hItemUri!!)
                Timber.d("Path is ${getPath(context, hItemUri!!)}")
            }
        }
    }

    private fun showToastOnUi() {
        Toast.makeText(context, context.getString(R.string.saved_status), Toast.LENGTH_SHORT).show()
    }


    companion object {
        private const val MENU_ITEM_VIEW_TYPE = 0
        private const val NATIVE_EXPRESS_AD_VIEW_TYPE = 1
        const val NEW_COLLECTION_CREATED = 100
        const val COLLECTION_ALREADY_CREATED = 200
        const val hImageType = "image/jpeg"
        const val hVideoType = "video/mp4"
    }

    init {
        ids = ArrayList()
        prefs = MyAppSharedPrefs(context)
    }

    fun hSetData(list: List<Any>) {
        filesList = list
    }
}
