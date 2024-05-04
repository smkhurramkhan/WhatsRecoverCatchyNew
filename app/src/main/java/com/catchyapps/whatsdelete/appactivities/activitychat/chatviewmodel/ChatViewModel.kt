package com.catchyapps.whatsdelete.appactivities.activitychat.chatviewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.catchyapps.whatsdelete.roomdb.AppHelperDb


class ChatViewModel(application: Application) : AndroidViewModel(application) {
    var messageId: Long = 0

    private val messagesDao = AppHelperDb.hGetMessagesDao()

    val hItems = Pager(
        PagingConfig(
            pageSize = 50,
            prefetchDistance = 50,
            enablePlaceholders = true
        )
    ) {
        messagesDao.getPagedMessagesList(messageId)!!
    }.flow

}
