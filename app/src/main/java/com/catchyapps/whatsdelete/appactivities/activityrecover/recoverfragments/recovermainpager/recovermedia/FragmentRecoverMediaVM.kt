package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.basicapputils.MyAppFetcherFile
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetImagesPath
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetVideoPath
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*


class FragmentRecoverMediaVM(application: Application) : AndroidViewModel(application) {

    private var hVideoFileListMld = MutableLiveData<MutableList<EntityFiles>?>()
    val hVideoFileListLd: LiveData<MutableList<EntityFiles>?>
        get() = hVideoFileListMld

    private var hImagesListMld = MutableLiveData<MutableList<EntityFiles>?>()
    val hImagesListld: LiveData<MutableList<EntityFiles>?>
        get() = hImagesListMld


    private var hIsImageFragment = false
    private var hLastFetchedImageLocation = 0
    private var hLastFetchedvideoLocation = 0
    private var hPageNo = 1
    private var hFetchedImagesList: List<File>? = null
    private var hTotalPages = 0
    private var hFetchedVideoList: List<File>? = null
    private fun hInit() {
        hFetchedImagesList = ArrayList()
        hFetchedVideoList = ArrayList()
    }

    fun hSetFragmentType(arguments: Bundle?) {
        if (arguments != null) {
            hIsImageFragment = arguments.getBoolean(MyAppConstants.H_IS_IMAGE, true)
        }
        hGetData()
    }

    private fun hGetData() {
        viewModelScope.launch(Dispatchers.IO) {
            if (hIsImageFragment) {
                hGetImageList()
            } else {
                hGetVideoList()
            }
        }
    }

    private fun hGetVideoList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            fetchVideosForPost10()

        } else {

            fetchVideosForPre10()

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchVideosForPost10() {

        val fileUri = "primary:Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"

        val files = MyAppFetcherFile.fetchFilesForPost10(fileUri, getApplication())

        hVideoFileListMld.postValue(files)

    }

    private fun fetchVideosForPre10() {


        Timber.d("hGetVideoList")
        if (hFetchedVideoList?.isEmpty() == true) {
            val targetDirector = File(hGetVideoPath)
            val files = targetDirector.listFiles()
            files?.let {
                it.sortedArrayWith(kotlin.Comparator { file1, file2 ->
                    return@Comparator file1.lastModified().compareTo(file2.lastModified())
                })
                it.reverse()

                hFetchedVideoList = listOf(*it)
                hTotalPages = hFetchedVideoList?.size?.div(hFilesToLoad) ?: 0
            }
        }
        val hVideoList: MutableList<EntityFiles> = ArrayList()
        var i = hLastFetchedvideoLocation
        while (i < hFilesToLoad * hPageNo && i < hFetchedVideoList!!.size - 1) {
            val file = hFetchedVideoList!![i]
            if (file.name.contains(".")) {
                val filesEntity = EntityFiles()
                filesEntity.title = file.name
                filesEntity.filePath = file.absolutePath
                filesEntity.timeStamp = file.lastModified().toString()
                filesEntity.mimeType = MyAppUtils.getMimeType(file.absolutePath)
                filesEntity.fileSize = file.length()
                hVideoList.add(filesEntity)
            }
            hLastFetchedvideoLocation = i
            i++
        }


        Timber.d("Added Video size %s", hVideoList.size)

        var hTempVideoFileList = hVideoFileListMld.value
        if (hTempVideoFileList != null) {
            hTempVideoFileList.addAll(hVideoList)
        } else {
            hTempVideoFileList = hVideoList
        }

        hVideoFileListMld.postValue(hTempVideoFileList)

    }


    private fun hGetImageList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            fetchImagesForPost10()

        } else {

            fetchImagesForPre10()

        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchImagesForPost10() {

        val fileUri = "primary:Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images"

        val files = MyAppFetcherFile.fetchFilesForPost10(fileUri, getApplication())

        hImagesListMld.postValue(files)
    }

    private fun fetchImagesForPre10() {
        Timber.d("Image Path ${Environment.getExternalStorageDirectory().absolutePath}")
        if (hFetchedImagesList!!.isEmpty()) {
            val targetDirector = File(hGetImagesPath)
            val files = targetDirector.listFiles()
            files?.let {
                it.sortedArrayWith { t, t2 ->
                    t.lastModified().compareTo(t2.lastModified())
                }
                it.reverse()

                hFetchedImagesList = listOf(*it)
                hTotalPages = hFetchedImagesList?.size?.div(hFilesToLoad) ?: 0

            }
        }
        val hImageList: MutableList<EntityFiles> = ArrayList()
        var i = hLastFetchedImageLocation
        while (i < hFilesToLoad * hPageNo && i < hFetchedImagesList!!.size - 1) {
            val file = hFetchedImagesList!![i]
            if (file.name.contains(".")) {
                val filesEntity = EntityFiles()
                filesEntity.title = file.name
                filesEntity.filePath = file.absolutePath
                filesEntity.timeStamp = file.lastModified().toString()
                filesEntity.mimeType = MyAppUtils.getMimeType(file.absolutePath)
                filesEntity.fileSize = file.length()
                hImageList.add(filesEntity)
            }
            hLastFetchedImageLocation = i
            i++
        }

        Timber.d("Added images size %s", hImageList.size)
        var hTempImageList = hImagesListMld.value
        if (hTempImageList != null) {
            hTempImageList.addAll(hImageList)

        } else {
            hTempImageList = hImageList
        }
        hImagesListMld.postValue(hTempImageList)
    }


    fun hLoadMoreItems(pageNumber: Int) {
        Timber.d("Loading more items")
        when {
            hTotalPages == 0 -> {
                if (hPageNo <= 1) {
                    hPageNo = pageNumber
                    hGetData()
                }
            }
            hPageNo <= hTotalPages -> {
                hPageNo = pageNumber
                hGetData()
            }
            else -> {
                Timber.d("Last page")
            }
        }
    }

    companion object {
        private const val hFilesToLoad = 50
    }

    init {
        hInit()
    }
}
