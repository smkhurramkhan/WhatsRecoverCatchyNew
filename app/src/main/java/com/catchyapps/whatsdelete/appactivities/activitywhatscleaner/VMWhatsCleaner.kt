package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.format.Formatter
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_AUDIO
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_DOCUMENTS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_GIFS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_IMAGE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_STATUS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VIDEOS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VOICE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_WALLPAPERS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hCheck11thUpSdk
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hFilesToExclude
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetCleanerPathList
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerVs
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerVs.OnLaunchIntent
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.Details
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerItemPath
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.SchemaHolder
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class VMWhatsCleaner(
    application: Application,
) : AndroidViewModel(application) {

    private var hTotalSize: Long = 0
    private val hDetailsList: MutableList<Details> = ArrayList()
    private val hDetailsListMLd = MutableLiveData<List<Details>>()
    private val hCleanerVsMLD = MutableLiveData<CleanerVs>()

    var treeUri: Uri? = DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        hWhatAppMainUri
    )

    val hCleanerVSLD = liveData {
        emitSource(hCleanerVsMLD)
    }


    val hDetailsListLd = liveData {
        emitSource(hDetailsListMLd)
    }

    private val hTotalSizeMld = MutableLiveData<String>()
    val hTotalSizeLd = liveData {
        emitSource(hTotalSizeMld)
    }

    fun hGetCleaningData() {
        viewModelScope.launch(Dispatchers.IO) {
            hFetchFiles()
        }
    }

    private fun hFetchFiles() {

        val hGetCleanerPathList = hGetCleanerPathList()
        hGetCleanerPathList.forEach { pathItem ->

            if (hCheck11thUpSdk()) {

                Timber.d(" hCheck11thUpSdk")

                if (MyAppPermissionUtils.hasPermissionPost10(getApplication())) {

                    pathItem.hPath?.let { uri ->

                        Timber.d("Path Uri : $uri")

                        val documentsTree = DocumentsContract.buildTreeDocumentUri(
                            EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
                            hWhatAppMainUri
                        )

                        Details(
                            hTitle = pathItem.hType!!,
                            hImage = hGetImageIcon(pathItem.hType)!!,
                            hColor = hGetColor(pathItem.hType)!!,
                            hPath = pathItem.hPath,
                            hSchemaType = SchemaHolder(
                                hMyAppSchemas1 = pathItem.hMyAppSchemas,
                            ),
                        ).also { details ->
                            val childDocuments = hGetFileDetails(documentsTree, details, uri)
                            hDetailsList.add(childDocuments)
                        }
                    }

                } else {

                    Timber.d("Permission not Granted")
                    hCleanerVsMLD.postValue(
                        CleanerVs.OnShowPermissionDialog(
                            true
                        )
                    )

                }

            } else {
                val hDirSize = hCalculateDirSize(
                    pathItem = pathItem,
                )
                Details(
                    hTitle = pathItem.hType!!,
                    hFileSizeString = Formatter.formatShortFileSize(
                        getApplication(),
                        hDirSize
                    ),
                    hPath = pathItem.hPath,
                    hSchemaType = SchemaHolder(
                        hMyAppSchemas1 = pathItem.hMyAppSchemas,
                    ),
                    hImage = hGetImageIcon(pathItem.hType)!!,
                    hColor = hGetColor(pathItem.hType)!!,
                    hFileSizeLong = hDirSize,
                    hFileCount = hGetFileCount(
                        pathItem = pathItem
                    ),
//                    hAllFilesUris = hGetFilesPath(
//                        pathItem = pathItem
//                    )
                ).also {
                    hDetailsList.add(it)
                }
            }
        }

        hDetailsList.groupBy {
            it.hTitle
        }.let { groupedMap ->
            hDetailsList.clear()
            groupedMap.keys.forEach { key ->
                var hDetails: Details? = null

                groupedMap[key]?.forEach { details ->
                    if (hDetails != null) {
                        val hTotalSize = hDetails?.hFileSizeLong?.plus(details.hFileSizeLong)
                        val hFileCount = hDetails?.hFileCount?.plus(details.hFileCount)
                        val hUriList = hDetails?.hAllFilesUris
                        hUriList?.addAll(details.hAllFilesUris)
                        hDetails?.apply {
                            if (hSchemaType?.hMyAppSchemas1 != null) {
                                if (details.hTitle != H_STATUS) {
                                    hSchemaType?.hMyAppSchemas2 = details.hSchemaType?.hMyAppSchemas1
                                }
                            }
                            hFileSizeString = Formatter.formatShortFileSize(
                                getApplication(),
                                hTotalSize!!
                            )
                            this.hFileSizeLong = hTotalSize
                            this.hFileCount = hFileCount!!
                            hAllFilesUris = hUriList!!
                        }
                    } else {
                        hDetails = details
                    }
                }
                hDetailsList.add(hDetails!!)
            }
        }
        hDetailsListMLd.postValue(hDetailsList)
        hTotalSizeMld.postValue(
            Formatter.formatShortFileSize(
                getApplication(),
                hTotalSize
            )
        )

    }

    private fun hGetFileCount(pathItem: CleanerItemPath): Int {
        val hRootDir = File(pathItem.hPath!!)

        var hFileCount = 0

        if (hRootDir.exists()) {
            hRootDir.listFiles()?.forEach {
                if (!hFilesToExclude.contains(it.name)) {
                    hFileCount++
                }
            }
        }

        return hFileCount
    }

    private fun hCalculateDirSize(
        pathItem: CleanerItemPath,
    ): Long {
        val hRootDir = File(pathItem.hPath!!)

        var hDirSize: Long = 0

        if (hRootDir.exists()) {
            hRootDir.listFiles()?.forEach {
                if (!hFilesToExclude.contains(it.name)) {
                    hDirSize += hGetFileSize(it, pathItem.hType)
                }
            }
        }

        hTotalSize += hDirSize
        return hDirSize
    }

    private fun hGetImageIcon(type: String): Int? {
        return when (type) {
            H_IMAGE -> R.drawable.ic_images_svg
            H_DOCUMENTS -> R.drawable.icon_white_folder
            H_VIDEOS -> R.drawable.ic_videos_svg
            H_AUDIO -> R.drawable.ic_audios_svg
            H_WALLPAPERS -> R.drawable.ic_wallpaper_svg
            H_GIFS -> R.drawable.ic_gif_svg
            H_VOICE -> R.drawable.ic_voices_svg
            H_STATUS -> R.drawable.ic_statuses
            else -> null
        }
    }

    private fun hGetColor(type: String): Int? {
        return when (type) {
            H_IMAGE ->R.color.orangeTransparent //R.color.green
            H_DOCUMENTS -> R.color.orangeTransparent
            H_VIDEOS -> R.color.blueTransparent
            H_AUDIO -> R.color.audioTransparentColor
            H_WALLPAPERS -> R.color.wallpaperransparentColor
            H_GIFS -> R.color.gifTransparentColor
            H_VOICE -> R.color.voiceTransparentColor
            H_STATUS -> R.color.statusTransparentColor
            else -> null
        }
    }


    private fun hGetFileSize(dir: File, hType: String?): Long {
        if (dir.exists()) {
            var hFileSize: Long = 0
            dir.listFiles()?.forEach {
                hFileSize += if (it.isDirectory) {
                    when (hType) {
                        H_VOICE -> hGetFileSize(it, hType)
                        else -> hFileSize
                    }
                } else {
                    it.length()
                }
            }
            hFileSize += dir.length()
            return hFileSize
        }
        return 0

    }

    private fun hDeleteFiles() {
        Timber.d("Deleting files")
        val hOnShowProgress = CleanerVs.OnShowProgress(
            hIsShowProgress = true
        )
        hCleanerVsMLD.postValue(hOnShowProgress)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (hCheck11thUpSdk()) {
                    Timber.d("Deleting Files Please wait")
                    hDetailsList.forEach { details ->
                        details.hAllFilesUris.forEach { uri ->
                            Timber.d("File uri : $uri")
                            DocumentFile.fromSingleUri(
                                getApplication(),
                                uri.toUri()
                            )?.delete()
                        }
                    }
                } else {
                    Timber.d("Deleting Files Please wait")
                    hDetailsList.forEach { details ->
                        details.hAllFilesUris.forEach {
                            File(it).delete()
                        }
                    }
                }
                hOnShowProgress.apply {
                    hIsShowProgress = false
                    hMessage = "Files Deleted Successfully"
                }.also {
                    hCleanerVsMLD.postValue(it)
                    hDetailsList.clear()
                    hTotalSize = 0
                    hFetchFiles()
                }
            } catch (e: Exception) {
                Timber.d("Exception ${e.message}")
                hOnShowProgress.apply {
                    hIsShowProgress = false
                    hMessage = e.message
                }.also {
                    hCleanerVsMLD.postValue(it)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hGetFileDetails(documentFile: Uri, details: Details, ANDROID_DOCID: String): Details {
        val resolver: ContentResolver = getApplication<BaseApplication>().contentResolver

        val childrenUri: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(
            documentFile,
            ANDROID_DOCID
        )

        var hDirSize: Long = 0

        var cursor: Cursor? = null
        try {
            cursor = resolver.query(
                childrenUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                ), null, null, null
            )


            while (cursor!!.moveToNext()) {
                val hDocIdCol =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val hDocSizeCol =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val hDocNameCol =
                    cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)


                val hId = cursor.getString(hDocIdCol)
                val hSize = cursor.getLong(hDocSizeCol)
                val hName = cursor.getString(hDocNameCol)


                val hDocUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                    documentFile,
                    hId.toString()
                )


                if (!hFilesToExclude.contains(hName)) {
                    hDirSize += hSize
                    details.hAllFilesUris.add(hDocUri.toString())
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.d("Failed query: $e")
        } finally {
            cursor?.close()
        }
        hTotalSize += hDirSize
        details.apply {
            hFileSizeLong = hDirSize
            hFileSizeString = Formatter.formatShortFileSize(
                getApplication(),
                hDirSize
            )
            hFileCount = hAllFilesUris.size
        }
        return details
    }

    fun hExecuteDeletion(detailItem: Details?) {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                detailItem != null -> {
                    hExecuteItemWisePermissionCheck(detailItem)
                }
                else -> {
                    hExecuteAllPermissionCheck()
                }
            }
        }

    }

    private fun hExecuteAllPermissionCheck() {

        hDeleteFiles()

    }

    private fun hExecuteItemWisePermissionCheck(detailItem: Details) {

        val gson = GsonBuilder().setPrettyPrinting().create()

        val type = object : TypeToken<Details>() {}.type

        MyAppSharedPrefs(getApplication()).also {
            it.saveDetailItem(gson.toJson(detailItem, type))

        }

        hCleanerVsMLD.postValue(
            OnLaunchIntent(
                detailItem.hTitle
            )
        )
    }

}