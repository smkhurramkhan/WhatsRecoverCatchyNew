package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.MIME_TYPE_IS_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetDocPath
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetUriFromSchema
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File


class VMFragmentDoc(application: Application) : AndroidViewModel(application) {

    private var hDocsListMLD: MutableLiveData<MutableList<EntityFiles>?>? = null
    var hDocsListLD: LiveData<MutableList<EntityFiles>?>? = null

    private var hLastFetchedDocsLocation = 0
    private var hPageNo = 1
    private var hFetchedDocsList: List<File>? = null
    private var hTotalPages = 0


    private fun hInit() {
        hDocsListMLD = MutableLiveData()
        hDocsListLD = hDocsListMLD
        hFetchedDocsList = ArrayList()
    }

    private fun hGetDataForPreAndroid11() {
        if (hFetchedDocsList!!.isEmpty()) {
            val targetDirector = File(hGetDocPath)
            val files = targetDirector.listFiles()
            if (files != null) {
                files.sortedArrayWith(Comparator { file1, file2 ->
                    return@Comparator file1.lastModified().compareTo(file2.lastModified())
                })
                files.reverse()
                hFetchedDocsList = listOf(*files)
                hTotalPages = hFetchedDocsList?.size?.div(hFilesToLoad) ?: 0
            }
        }
        val hTempDocsList: MutableList<EntityFiles> = ArrayList()
        var i = hLastFetchedDocsLocation
        while (i < hFilesToLoad * hPageNo && i < hFetchedDocsList!!.size - 1) {
            val file = hFetchedDocsList!![i]
            if (file.name.contains(".")) {
                val filesEntity = EntityFiles()
                filesEntity.title = file.name
                filesEntity.filePath = file.absolutePath
                filesEntity.timeStamp = file.lastModified().toString()
                filesEntity.mimeType = MyAppUtils.getMimeType(file.absolutePath)
                filesEntity.fileSize = file.length()
                filesEntity.favourite = 0
                hTempDocsList.add(filesEntity)
            }
            hLastFetchedDocsLocation = i
            i++
        }

        var value1 = hDocsListMLD!!.value
        if (value1 != null) {
            value1.addAll(hTempDocsList)
        } else {
            value1 = hTempDocsList
        }
        hDocsListMLD?.postValue(value1)
    }

    fun hLoadMoreItems(pageNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("Loading more items page No %s  Total pages  %s", hPageNo, hTotalPages)
            if (hTotalPages == 0) {
                if (pageNumber <= 1) {
                    hPageNo = pageNumber
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        hGetDataForPostAndroid10()
                    } else {
                        hGetDataForPreAndroid11()
                    }
                }
            } else if (hPageNo <= hTotalPages) {
                hPageNo = pageNumber


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    hGetDataForPostAndroid10()
                } else {
                    hGetDataForPreAndroid11()
                }
            } else {
                Timber.d("Last page")
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hGetDataForPostAndroid10() {
        val hTempDocsList: MutableList<EntityFiles> = ArrayList()

        hGetUriFromSchema(
            MyAppSchemas.H_DOCUMENT_TYPE
        ).let {

            val docUri = "primary:Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents"

            Timber.d("Uri: $docUri")

            val documentsTree = DocumentsContract.buildTreeDocumentUri(
                EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
                hWhatAppMainUri
            )

            val resolver: ContentResolver = getApplication<BaseApplication>().contentResolver

            val childrenUri: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(
                documentsTree,
                docUri
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

                    Timber.d("File Name $hName")

                    Timber.d("File Path $path")

                    Timber.d("File Mime $fileMime")

                    Timber.d("File Size $hSize")


                    if (fileMime != MIME_TYPE_IS_DIRECTORY) {
                        val hStatusModel = EntityFiles()
                        hStatusModel.apply {

                            mimeType = fileMime

                            title = hName

                            filePath = path

                            fileUri = hDocUri.toString()

                            timeStamp = hLastModified.toString()

                            hTempDocsList.add(hStatusModel)

                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.d("Failed query: $e")
                Timber.d("Uri: $it")
            } finally {
                cursor?.close()
            }
        }

        hDocsListMLD?.postValue(hTempDocsList)

    }


    companion object {
        private const val hFilesToLoad = 50
    }

    init {
        hInit()
    }
}
