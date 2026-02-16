package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.MIME_TYPE_IS_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.NO_MEDIA_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetAudioPath
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetVoicePath
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appnotifications.MediaBackupHelper
import com.catchyapps.whatsdelete.basicapputils.AppPathUtil
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class VoiceMediaVMFragment(application: Application) : AndroidViewModel(application) {
    private val hVoiceListMLD = MutableLiveData<MutableList<EntityFiles>?>()
    private val hAudioListMLD = MutableLiveData<MutableList<EntityFiles>?>()
    val hAudioListLD: LiveData<MutableList<EntityFiles>?>
        get() = hAudioListMLD
    val hVoiceListLD: LiveData<MutableList<EntityFiles>?>
        get() = hVoiceListMLD

    var treeUri: Uri = DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        hWhatAppMainUri
    )

    private var hIsAudioFragment = false
    private var hLastFetchedAudioLocation = 0
    private var hPageNo = 1
    private var hFetchedAudioList: List<File>? = null
    private var hTotalPages = 0
    private var hFecthedVoiceList: List<File>? = null
    private var hTempFilesList: MutableList<EntityFiles>? = null
    private var hDirectoryIndex = 0
    private var hInnerIndex = 0
    private var hBreakLoop: AtomicBoolean? = null


    private fun hInit() {
        hFetchedAudioList = ArrayList()
        hFecthedVoiceList = ArrayList()
        hBreakLoop = AtomicBoolean(false)
    }

    fun hSetFragmentType(arguments: Bundle?) {
        if (arguments != null) {
            hIsAudioFragment = arguments.getBoolean("Audio", true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            hGetData()
        }
    }

    private fun hGetData() {
        Timber.d("Thread is ${Thread.currentThread().name}")
        if (hIsAudioFragment) {
            hGetAudioList()
        } else {
            hGetVoiceList()
        }
    }

    private fun hGetVoiceNotes(dir: File) {
        val files = dir.listFiles()
        files?.let {

            it.sortedArrayWith(Comparator { file1, file2 ->
                return@Comparator file1.lastModified().compareTo(file2.lastModified())
            })
            it.reverse()
            val hDirList = listOf(*it)
            for (i in hInnerIndex until hDirList.size) {
                hInnerIndex = i
                if (hTempFilesList!!.size < 50) {
                    val file = hDirList[i]
                    if (file.name.contains(".")) {
                        val filesEntity = EntityFiles()
                        filesEntity.title = file.name
                        filesEntity.filePath = file.absolutePath
                        filesEntity.timeStamp = file.lastModified().toString()
                        filesEntity.mimeType = MyAppUtils.getMimeType(file.absolutePath)
                        filesEntity.fileSize = file.length()
                        if (!hTempFilesList!!.contains(filesEntity)) {
                            hTempFilesList!!.add(filesEntity)
                        }
                    }
                } else {
                    hBreakLoop!!.set(true)
                    return
                }
            }
        }


    }

    private fun hGetVoiceList() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            fetchFilesForPost10(true)

        } else {

            fetchVoiceFilesForPre10()

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchFilesForPost10(voice: Boolean) {
        // Only show recovered deleted audio from app-private storage
        // Both voice and audio tabs use the same audio bucket
        val recoveredAudio = MediaBackupHelper.getRevealedFilesAsEntities(
            getApplication(), MediaBackupHelper.TYPE_AUDIO
        )

        if (voice) {
            hVoiceListMLD.postValue(recoveredAudio)
        } else {
            hAudioListMLD.postValue(recoveredAudio)
        }
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

            val filePath = AppPathUtil.getPath(hDocUri, getApplication())

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


    private fun fetchVoiceFilesForPre10() {
        if (hFecthedVoiceList!!.isEmpty()) {
            val targetDirector = File(hGetVoicePath)
            val files = targetDirector.listFiles()
            if (files != null) {
                files.sortedArrayWith(Comparator { file1, file2 ->
                    return@Comparator file1.lastModified().compareTo(file2.lastModified())
                })
                files.reverse()
                hFecthedVoiceList = listOf(*files)
                hTotalPages = hFecthedVoiceList?.size?.div(hFilesToLoad) ?: 0
            }
        }
        hTempFilesList = ArrayList()
        hCheckForDirectories(hDirectoryIndex)
        var value1 = hVoiceListMLD.value

        if (value1 != null) {
            value1.addAll(hTempFilesList!!)
        } else {
            value1 = hTempFilesList
        }

        value1?.sortWith { o1: EntityFiles, o2: EntityFiles ->
            o2.timeStamp?.let { o1.timeStamp?.compareTo(it) }!!
        }
        hVoiceListMLD.postValue(value1)
    }

    private fun hCheckForDirectories(hDirectoryIndex: Int) {
        for (i in hDirectoryIndex until hFecthedVoiceList!!.size) {
            val file = hFecthedVoiceList!![i]
            if (file.isDirectory) {
                this.hDirectoryIndex = i
                hGetVoiceNotes(file)
                if (hBreakLoop!!.get()) {
                    hBreakLoop!!.set(false)
                    return
                }
            }
        }
    }

    private fun hGetAudioList() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            fetchFilesForPost10(false)

        } else {

            fetchAudioFilesForPre10()

        }

    }

    private fun fetchAudioFilesForPre10() {
        if (hFetchedAudioList?.isEmpty() == true) {
            val targetDirector = File(hGetAudioPath)
            val files = targetDirector.listFiles()
            if (files != null) {
                files.sortedArrayWith(Comparator { file1, file2 ->
                    return@Comparator file1.lastModified().compareTo(file2.lastModified())
                })
                files.reverse()
                hFetchedAudioList = listOf(*files)
                hTotalPages = hFetchedAudioList?.size?.div(hFilesToLoad) ?: 0
            }
        }
        val hImageList: MutableList<EntityFiles> = ArrayList()
        var i = hLastFetchedAudioLocation
        while (i < hFilesToLoad * hPageNo && i < hFetchedAudioList!!.size - 1) {
            val file = hFetchedAudioList!![i]
            if (file.name.contains(".")) {
                EntityFiles().apply {
                    title = file.name
                    filePath = file.absolutePath
                    timeStamp = file.lastModified().toString()
                    mimeType = MyAppUtils.getMimeType(file.absolutePath)
                    fileSize = file.length()
                    favourite = 0
                }.also {
                    hImageList.add(it)
                }

            }
            hLastFetchedAudioLocation = i
            i++
        }
        Timber.d("Added files size %s", hImageList.size)
        var hTempAudioList = hAudioListMLD.value

        if (hTempAudioList != null) {
            hTempAudioList.addAll(hImageList)
        } else {
            hTempAudioList = hImageList
        }

        hTempAudioList.sortWith(Comparator { o1: EntityFiles, o2: EntityFiles ->
            o2.timeStamp?.let { o1.timeStamp?.compareTo(it) }!!
        })
        hAudioListMLD.postValue(hTempAudioList)
    }

    fun hLoadMoreItems(pageNumber: Int) {
        Timber.d("Loading more items %s page no.  and total items %s", hPageNo, hTotalPages)
        if (hTotalPages == 0) {
            if (hPageNo <= 1) {
                hPageNo = pageNumber
                hGetData()
            } else if (!hIsAudioFragment) {
                hPageNo = pageNumber
                hGetData()
            }
        } else if (hPageNo <= hTotalPages) {
            hPageNo = pageNumber
            hGetData()
        } else {
            Timber.d("Last page")
        }
    }

    companion object {
        private const val hFilesToLoad = 50
    }

    init {
        hInit()
    }
}
