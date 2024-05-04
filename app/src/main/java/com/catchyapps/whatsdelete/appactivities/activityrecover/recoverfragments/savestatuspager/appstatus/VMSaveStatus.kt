package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.basicapputils.AppPathUtil
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.MIME_TYPE_IS_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetStatusPath
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_TITLE_ARG
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_TITLE_ARRAY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class VMSaveStatus(application: Application) : AndroidViewModel(application) {
    private val hVideoFilesList: MutableList<EntityStatuses> = ArrayList()
    private val hImagesFileList: MutableList<EntityStatuses> = ArrayList()
    private val hImagesFileListMLD = MutableLiveData<List<EntityStatuses>>()
    private val hVideoFilesListMLD = MutableLiveData<List<EntityStatuses>>()

    val hImagesFileListLD: LiveData<List<EntityStatuses>>
        get() = hImagesFileListMLD

    val hVideoFilesListLD: LiveData<List<EntityStatuses>>
        get() = hVideoFilesListMLD


    var hMediaType: String? = null

    private fun hGetDataForPostAndroid10() {
        hImagesFileList.clear()
        hVideoFilesList.clear()

        val statusUri = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses"

        Timber.d("Uri: $statusUri")

        val documentsTree = DocumentsContract.buildTreeDocumentUri(
            CleanerConstans.EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
            CleanerConstans.hWhatAppMainUri
        )

        val resolver: ContentResolver = getApplication<BaseApplication>().contentResolver

        val childrenUri: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(
            documentsTree,
            statusUri
        )

        var cursor: Cursor? = null

        try {

            cursor = resolver.query(
                childrenUri, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                ), null, null, null
            )

            while (cursor?.moveToNext() == true) {

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
                val hDocMimeType = cursor.getColumnIndexOrThrow(
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                )

                val path = cursor.getString(hDocIdCol)

                val hName = cursor.getString(hDocNameCol)

                val hLastModified = cursor.getLong(hDocLastModifiedCol)

                val hSize = cursor.getLong(hDocSizeCol)

                val fileMime = cursor.getString(hDocMimeType)

                val hDocUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                    documentsTree,
                    path
                )

                val filePath = AppPathUtil.getPath(hDocUri, getApplication())

                Timber.d("File Name $hName")

                Timber.d("File Path $path")

                Timber.d("File Mime $fileMime")

               // Timber.d("File Size $hSize")
                Timber.d("File Size $itemSize")



                if (fileMime != MIME_TYPE_IS_DIRECTORY) {

                    if (fileMime == StatusSaveAdapter.hImageType || fileMime == StatusSaveAdapter.hVideoType) {



                        val hStatusesEntity = EntityStatuses()

                        hStatusesEntity.apply {
                            this.filename = "NBM Story Saver: $hName"
                            this.name = filename
                            this.path = filePath
                            this.type = fileMime
                            this.fileSize = hSize
                            this.uri = hDocUri
                        }

                        when (fileMime) {
                            StatusSaveAdapter.hVideoType -> {
                                hVideoFilesList.add(hStatusesEntity)
                                itemSize = hSize

                            }
                            StatusSaveAdapter.hImageType -> {
                                hImagesFileList.add(hStatusesEntity)
                                itemSize = hSize
                                Timber.d("size1213: $hSize")
                            }
                        }
                    }

                }
            }
        } catch (e: java.lang.Exception) {
            Timber.d("Failed query: $e")
            Timber.d("Uri: $statusUri")
        } finally {
            cursor?.close()
        }

        hImagesFileListMLD.postValue(hImagesFileList)
        hVideoFilesListMLD.postValue(hVideoFilesList)
    }

    private fun hGetDataForPreAndroid11() {

        hImagesFileList.clear()
        hVideoFilesList.clear()
        val hDirectory = File(hGetStatusPath)
        Timber.d("Uri new is  ${hDirectory.toUri()}")

        val files = hDirectory.listFiles()
        var hStatusMode: EntityStatuses
        try {
            if (files != null) {
                files.sortWith { o1, o2 -> o2.lastModified().compareTo(o1.lastModified()) }
                for (i in files.indices) {
                    val file = files[i]
                    Timber.d("File size is ${file.length()}")
                    hStatusMode = EntityStatuses()
                    hStatusMode.name = "NBM Story Saver: " + (i + 1)
                    hStatusMode.path = files[i].absolutePath
                    hStatusMode.filename = file.name
                    if (!hStatusMode.path?.endsWith(".nomedia")!!) {
                        if (
                            hMediaType?.lowercase(Locale.getDefault()) ==getApplication<Application>()
                                .getString( H_TITLE_ARRAY[0]).lowercase(
                                Locale.getDefault()
                            )
                            && !hStatusMode.path?.endsWith(".mp4")!!
                        ) {
                            hStatusMode.type = StatusSaveAdapter.hImageType
                            hImagesFileList.add(hStatusMode)
                        } else if (
                            hMediaType!!.lowercase(Locale.getDefault()) ==getApplication<Application>()
                                .getString( H_TITLE_ARRAY[1]).lowercase(
                                Locale.getDefault()
                            )
                            && hStatusMode.path?.endsWith(".mp4")!!
                        ) {
                            hStatusMode.type = StatusSaveAdapter.hVideoType
                            hVideoFilesList.add(hStatusMode)
                        }
                    }
                }
            }
            hImagesFileListMLD.postValue(hImagesFileList)
            hVideoFilesListMLD.postValue(hVideoFilesList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hSetData(arguments: Bundle?) {
        arguments?.let {
            hMediaType = arguments.getString(H_TITLE_ARG)
            Timber.d("Media Type $hMediaType")
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                hGetDataForPostAndroid10()
            } else {
                hGetDataForPreAndroid11()
            }
        }
    }

    companion object{
        var itemSize : Long? = null
    }
}