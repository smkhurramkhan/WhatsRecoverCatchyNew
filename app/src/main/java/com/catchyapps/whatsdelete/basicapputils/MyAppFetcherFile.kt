package com.catchyapps.whatsdelete.basicapputils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.MIME_TYPE_IS_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.NO_MEDIA_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.treeUri
import timber.log.Timber

object MyAppFetcherFile {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun fetchFilesForPost10(filesUri: String, context: Context): MutableList<EntityFiles>? {

        Timber.d("Uri: $filesUri")

        val tempList = mutableListOf<EntityFiles>()

        val resolver: ContentResolver = context.contentResolver

        val childrenUri: Uri = DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            filesUri
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
                    treeUri,
                    path
                )

                val fileSize = formatSize(hSize)

                val filePath = AppPathUtil.getPath(hDocUri, context)

                if (fileMime != MIME_TYPE_IS_DIRECTORY && fileMime != NO_MEDIA_DIRECTORY) {

                    val hStatusModel = EntityFiles()

                    hStatusModel.apply {

                        this.mimeType = fileMime

                        this.title = hName

                        this.filePath = hDocUri.toString()

                        Timber.d("File path: ${this.filePath}")

                        this.fileSize = fileSize

                        this.timeStamp = hLastModified.toString()

                        tempList.add(this)

                    }


                }
            }

            Timber.d("List Size : ${tempList.size}")

            return tempList

        } catch (e: java.lang.Exception) {
            Timber.d("Failed query: $e")
            Timber.d("Uri: $filesUri")
            return null
        } finally {
            cursor?.close()
        }

    }

    private fun formatSize(fileSize: Long): Long {
        var size = fileSize
        if (size >= 1024) {
            size /= 1024
            if (size >= 1024) {
                size /= 1024
            }
        }
        return size
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFileFromSpecificFolder(
        folder: String,
        context: Context,
        treeUri: Uri,
        docId: String
    ): MutableList<EntityFiles> {

        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        val listOfFiles = mutableListOf<EntityFiles>()

        val folderName = "%$folder%"

        val selectionArgs = arrayOf(folderName)

        val cursor = context.contentResolver.query(uri, null, null, selectionArgs, null)

        val idIndex = cursor?.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)

        val nameIndex =
            cursor?.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)

        val sizeIndex = cursor?.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)

        val mimeIndex = cursor?.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)

        val dateIndex =
            cursor?.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

        while (cursor?.moveToNext() == true) {

            val mimeType = mimeIndex?.let { cursor.getString(it) }

            val fileName = nameIndex?.let { cursor.getString(it) }

            val path = idIndex?.let { cursor.getString(it) }

            val hDocUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                path
            )

            val filePath = AppPathUtil.getPath(hDocUri, context)

            val timeStamp = dateIndex?.let { cursor.getLong(it) }

            Timber.d("File Name $fileName")

            Timber.d("File Path $filePath")

            Timber.d("File Uri $hDocUri")

            Timber.d("File Mime $mimeType")

            val size = sizeIndex?.let { cursor.getInt(it) }

//            val fileSize = size?.toLong()?.let { CleanerRepository.formatSize(it) }

            if (mimeType != MIME_TYPE_IS_DIRECTORY && mimeType != NO_MEDIA_DIRECTORY) {

                val hStatusModel = EntityFiles()

                hStatusModel.apply {

                    this.mimeType = mimeType

                    this.title = fileName

                    this.filePath = filePath

                    this.fileUri = hDocUri.toString()

                    this.timeStamp = timeStamp.toString()

                    listOfFiles.add(hStatusModel)

                }


            }
        }

        cursor?.close()

        return listOfFiles

    }


}