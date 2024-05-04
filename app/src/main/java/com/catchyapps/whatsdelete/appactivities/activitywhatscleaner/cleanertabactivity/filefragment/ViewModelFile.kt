package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanertabactivity.filefragment

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_AUDIO
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_DOCUMENTS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VIDEOS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VOICE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hCheck11thUpSdk
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hFilesToExclude
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hSetPathForFilesDetails
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.Details
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerDetailsFile
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerTypeFilter
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FilenameUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class ViewModelFile(application: Application) : AndroidViewModel(application) {

    private val hFileDetialsList: MutableList<CleanerDetailsFile> = mutableListOf()
    private val hFileDetialsListMLD = MutableLiveData<List<CleanerDetailsFile>>()
    private val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"

    val hFileDetialsListLD = liveData {
        emitSource(hFileDetialsListMLD)
    }

    private var hSelectedFilesList: MutableList<CleanerDetailsFile> = mutableListOf()
    private var hSelectedFilesSize: Long = 0
    private val hSelectedFilesSizeMLD = MutableLiveData<String>()
    val hSelectedFilesSizeLD = liveData {
        emitSource(hSelectedFilesSizeMLD)
    }


    /*This value shouldnt be changed.*/
    private lateinit var hDetailsItem: Details

    fun hFetchData(detailsItem: Details, hPostion: Int) {
        Timber.d("hFetchData")

        viewModelScope.launch(Dispatchers.IO) {
            hDetailsItem = detailsItem

            hDetailsItem.hPath = hSetPathForFilesDetails(
                hPostion, hDetailsItem.hTitle
            )?.hPath

            if (hCheck11thUpSdk())
                hGetDataForPostAndroid10()
            else
                hGetDataForPreAndroid11()

        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hGetDataForPostAndroid10() {

        Timber.d("hGetDataForPostAndroid10")

        hDetailsItem.hPath?.let { uri ->

            Timber.d("Uri: $uri")

            DocumentsContract.buildTreeDocumentUri(
                EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
                hWhatAppMainUri
            )?.also { documentFile ->

                hGetFileDetails(documentFile, uri)

            }

        }

        hFileDetialsListMLD.postValue(hFileDetialsList)

//            ?.let { uri ->
//            DocumentFile.fromTreeUri(
//                getApplication(),
//                uri
//            )?.also { documentFile ->
//                hGetFileDetails(documentFile)
//            }
//        }
//        hFileDetialsListMLD.postValue(hFileDetialsList)

    }


    private fun hGetDataForPreAndroid11() {
        val hFile = File(hDetailsItem.hPath!!)
        val listFiles = hFile.listFiles()
        listFiles?.forEach { file ->
            if (file.isFile)
                hSetFileDetails(file)
            else if (file.isDirectory)
                hSetFileDetailsFromDir(file)
        }
        hFileDetialsListMLD.postValue(hFileDetialsList)

    }

    private fun hSetFileDetailsFromDir(hDirFile: File?) {
        hDirFile?.listFiles()?.forEach { file ->
            if (!file.name.endsWith(".nomedia")) {
                hSetFileDetails(file)
            }
        }
    }


    private fun hSetFileDetails(
        file: File,
    ) {
        if (!file.name.endsWith(".nomedia")) {
            CleanerDetailsFile().apply {
                name = file.name
                hType = hDetailsItem.hTitle
                path = file.path
                mod = file.lastModified()
                hFormatedSize = Formatter.formatShortFileSize(
                    getApplication(),
                    file.length()
                )
                hSize = file.length()


                hSetIconsNcolors(this)

                hCheckMimeType(this, file)
                ext = FilenameUtils.getExtension(file.path)
                hFileDetialsList.add(this)
            }
        }
        Timber.d("FilesDetails size ${hFileDetialsList.size}")
    }

    private fun hSetIconsNcolors(fileDetails: CleanerDetailsFile) {
        when (fileDetails.hType) {
            H_VIDEOS -> {
                fileDetails.image = R.drawable.play_video_svg
                fileDetails.color = R.color.colorTransparent
            }
            H_AUDIO -> {
                fileDetails.image = R.drawable.mp3_img
            }
            H_VOICE -> {
                fileDetails.image = R.drawable.notes_voice_img
                fileDetails.color = R.color.orange
            }

            H_DOCUMENTS -> {
                hSetDocTypeIcons(fileDetails)
            }
        }
    }

    private fun hCheckMimeType(fileDetails: CleanerDetailsFile, file: File) {
        var mime = "*/*"
        val uri = FileProvider.getUriForFile(
            getApplication(),
            getApplication<BaseApplication>().packageName +
                    ".provider", file
        )
        val mimeTypeMap = MimeTypeMap.getSingleton()
        if (mimeTypeMap.hasExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            )
        ) {
            mime = Objects.requireNonNull(
                mimeTypeMap.getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                )
            )?.split("/")?.toTypedArray()?.get(0) ?: ""
        }
        when (mime) {
            "image" -> {
                fileDetails.color = R.color.green
            }
            "video" -> {
                fileDetails.color = R.color.blue
            }
            "text" -> {
                fileDetails.color = R.color.orange
            }
            "application" -> {
                fileDetails.color = R.color.red
            }
            "audio" -> {
                fileDetails.color = R.color.purple
            }
        }
    }

    private fun hSetDocTypeIcons(fileDetails: CleanerDetailsFile) {
        fileDetails.name?.let {
            when {
                fileDetails.name!!.endsWith(".pdf") -> {
                    fileDetails.image = R.drawable.pdf_img
                    fileDetails.color = R.color.grey
                }
                fileDetails.name!!.endsWith(".ppt") || fileDetails.name!!.endsWith(".pptx") -> {
                    fileDetails.image = R.drawable.point_power_img
                    fileDetails.color = R.color.grey
                }
                fileDetails.name!!.endsWith(".doc") || fileDetails.name!!.endsWith("docx") -> {
                    fileDetails.image = R.drawable.img_word_
                    fileDetails.color = R.color.grey
                }
                fileDetails.name!!.endsWith(".xls") || fileDetails.name!!.endsWith(".xlsx") -> {
                    fileDetails.image = R.drawable.excel_img
                    fileDetails.color = R.color.grey
                }
                fileDetails.name!!.endsWith(".txt") -> {
                    fileDetails.image = R.drawable.img_text
                    fileDetails.color = R.color.grey
                }
                else -> {
                    fileDetails.image = R.drawable.alldocumentpng
                    fileDetails.color = R.color.grey
                }
            }
        }

    }

    fun hApplyFiler(filterType: CleanerTypeFilter) {
        when (filterType) {
            CleanerTypeFilter.H_SIZE -> {
                hFileDetialsList.sortBy {
                    it.hFormatedSize
                }
            }
            CleanerTypeFilter.H_NAME -> {
                hFileDetialsList.sortBy {
                    it.name
                }
            }
            CleanerTypeFilter.H_DATE -> {
                hFileDetialsList.sortBy {
                    it.mod
                }
            }
        }
        hFileDetialsListMLD.postValue(hFileDetialsList)

    }

    fun hSelectUnSelectAll(checked: Boolean) {
        when (checked) {
            true -> {
                hFileDetialsList.forEach {
                    it.isSelected = true
                }
            }
            false ->
                hFileDetialsList.forEach {
                    it.isSelected = false
                }
        }
        hFileDetialsListMLD.postValue(hFileDetialsList)

        hAddRemoveAllFromSelectedList(checked)
    }

    private fun hAddRemoveAllFromSelectedList(checked: Boolean) {
        hSelectedFilesList = ArrayList(hFileDetialsList)
        hSelectedFilesSize = 0
        when (checked) {
            true -> {
                hSelectedFilesList.forEach {
                    hSelectedFilesSize += it.hSize!!
                }
            }
            false -> hSelectedFilesList.clear()
        }

        hSelectedFilesSizeMLD.postValue(
            Formatter.formatShortFileSize(
                getApplication(),
                hSelectedFilesSize
            )
        )
    }

    fun hAddRemoveSelectedItem(fileDetailsItem: CleanerDetailsFile) {
        if (hSelectedFilesList.contains(fileDetailsItem)) {
            hSelectedFilesList.remove(fileDetailsItem)
            hSelectedFilesSize -= fileDetailsItem.hSize!!
        } else {
            hSelectedFilesList.add(fileDetailsItem)
            hSelectedFilesSize += fileDetailsItem.hSize!!
        }
        hSelectedFilesSizeMLD.postValue(
            Formatter.formatShortFileSize(
                getApplication(),
                hSelectedFilesSize
            )
        )

    }

    fun hInitiateDeletion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                hSelectedFilesList.forEach { fileDetails ->
                    Timber.d("File ${fileDetails.path}")
                    DocumentFile.fromSingleUri(
                        getApplication(),
                        Uri.parse(fileDetails.path)
                    )?.delete().apply {
                        fileDetails.hIsDeleted = this
                    }
                }

                hSelectedFilesList.filter { fileDetails ->
                    fileDetails.hIsDeleted == true
                }.forEach {
                    hSelectedFilesList.remove(it)
                    hFileDetialsList.remove(it)
                }
            } catch (e: Exception) {
                Timber.d("Exception ${e.message}")
            }

        } else {
            hSelectedFilesList.forEach { fileDetails ->
                File(fileDetails.path!!).also { file ->
                    file.delete().apply {
                        fileDetails.hIsDeleted = this
                    }
                }
            }
            hSelectedFilesList.filter {
                it.hIsDeleted == true
            }.forEach { fileDetails ->
                hSelectedFilesList.remove(fileDetails)
                hFileDetialsList.remove(fileDetails)
            }
        }
        hFileDetialsListMLD.postValue(hFileDetialsList)
    }

    fun hHasFilesToDelete(): Boolean {
        return hSelectedFilesList.isNotEmpty()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hGetFileDetails(documentFile: Uri, uri: String): MutableList<CleanerDetailsFile> {

        Timber.d("hGetFileDetails")

        val hList = mutableListOf<CleanerDetailsFile>()

        val resolver: ContentResolver = getApplication<BaseApplication>().contentResolver

        val childrenUri: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(
            documentFile,
            uri
        )

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
                val hDocIdCol = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                )
                val hDocNameCol = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                )
                val hDocSizeCol = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_SIZE
                )
                val hDocLastModifiedCol = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED
                )


                val hId = cursor.getString(hDocIdCol)
                val hName = cursor.getString(hDocNameCol)
                val hSize = cursor.getLong(hDocSizeCol)
                val hLastModified = cursor.getLong(hDocLastModifiedCol)


                val hDocUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                    documentFile,
                    hId
                )

                if (!hFilesToExclude.contains(hName)) {
                    hList.add(
                        CleanerDetailsFile(
                            name = hName.toString(),
                            hSize = hSize,
                            mod = hLastModified,
                            path = hDocUri.toString(),
                            hFormatedSize = Formatter.formatShortFileSize(getApplication(), hSize),
                            hType = hDetailsItem.hTitle,
                        ).also {
                            hSetIconsNcolors(it)
                            hFileDetialsList.add(it)
                        }
                    )
                }

            }
        } catch (e: java.lang.Exception) {
            Timber.d("Failed query: $e")
            Timber.d("Uri: $uri")
        } finally {
            cursor?.close()
        }

        return hList
    }

}
