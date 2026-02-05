package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recoverchat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats
import kotlinx.coroutines.launch

class ModelRecoverChatView(application: Application) : AndroidViewModel(application) {


    private val hSearchedChatListMLD = MutableLiveData<List<EntityChats>>()
    private var hOrignalList: List<EntityChats>? = null

    private val hIsRestoreListMLD = MutableLiveData(false)
    val hIsRestoreListLD: LiveData<Boolean>
        get() = hIsRestoreListMLD

    private var hIsRestoreList = false

    val hSearchedChatList: LiveData<List<EntityChats>>
        get() = hSearchedChatListMLD

    private var hLastSearchQuery: String? = null

    suspend fun deleteSingleChat(title: String?) {
        if (title != null) {
            val chatEntity = AppHelperDb.getChatByTitle(title)
            if (chatEntity != null) AppHelperDb.clearAllChatNotifications(chatEntity.id)
            AppHelperDb.deleteSingleChat(title)
        }
    }


    val hItems = Pager(
        PagingConfig(
            pageSize = 50,
            prefetchDistance = 50,
            enablePlaceholders = true
        )
    ) {
        AppHelperDb.getAllPagedChatHeads()!!
    }.flow


    suspend fun deleteAllChat() {
        AppHelperDb.clearAllChat()
        AppHelperDb.clearAlMessages()
    }


    suspend fun hExecuteSearch(searchQuery: String?) {
        if (!searchQuery.isNullOrEmpty()) {
            if (searchQuery != hLastSearchQuery) {
                hIsRestoreList = false
                hLastSearchQuery = searchQuery
                viewModelScope.launch {
                    val hGetSearchItems = AppHelperDb.getSearchItems(hLastSearchQuery!!)
                    hSearchedChatListMLD.postValue(hGetSearchItems!!)
                }
            }
        } else {
            if (!hIsRestoreList) {
                hIsRestoreList = true
                hIsRestoreListMLD.value = hIsRestoreList
                hLastSearchQuery = null
            }
        }

    }

    fun hGetOrignalList(): PagingData<EntityChats>? {
        return if (hOrignalList != null) {
            return PagingData.from(hOrignalList!!)
        } else
            null
    }

    fun hCheckForRealList(items: List<EntityChats>) {
        if (hOrignalList == null) {
            hOrignalList = items
        }

    }

}
