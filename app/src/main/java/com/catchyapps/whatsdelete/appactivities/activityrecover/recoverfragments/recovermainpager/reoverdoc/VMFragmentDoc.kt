package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hGetDocPath
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appnotifications.MediaBackupHelper
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
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
        // Only show recovered deleted documents from app-private storage
        val recoveredDocs = MediaBackupHelper.getRevealedFilesAsEntities(
            getApplication(), MediaBackupHelper.TYPE_DOCUMENTS
        )
        hDocsListMLD?.postValue(recoveredDocs)
    }


    companion object {
        private const val hFilesToLoad = 50
    }

    init {
        hInit()
    }
}
