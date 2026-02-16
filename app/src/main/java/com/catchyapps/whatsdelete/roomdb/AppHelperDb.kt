package com.catchyapps.whatsdelete.roomdb

import android.content.Context
import androidx.paging.PagingSource
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appdao.DaoMessages
import com.catchyapps.whatsdelete.roomdb.appdatabase.DatabaseRecover
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats
import com.catchyapps.whatsdelete.roomdb.appentities.EntityMessages
import com.catchyapps.whatsdelete.roomdb.appentities.EntityScreenShots
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object AppHelperDb {
    private lateinit var recoverDatabase: DatabaseRecover

    fun initializeDb(context: Context) {
        recoverDatabase = DatabaseRecover.getDbInstance(context)!!
    }

    suspend fun getChatByTitle(finalTitle: String): EntityChats? {
        return withContext(Dispatchers.IO) {
            return@withContext recoverDatabase.chatsDao.hGetChatByTitle(finalTitle)
        }
    }

    suspend fun updateChatRow(
        count: Int,
        time: String,
        finalText: String,
        dpPath: ByteArray?,
        id: Long
    ) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hUpdateChatRow(
                count,
                time,
                finalText,
                dpPath,
                id
            )
        }
    }

    suspend fun insertChatRow(chatsEntity: EntityChats): Long {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hInsertChatRow(chatsEntity)
        }
    }

    suspend fun insertChildNotification(messagesEntity: EntityMessages): Long {
        return withContext(Dispatchers.IO) {
            recoverDatabase.messagesDao.insertChildNotification(messagesEntity)
        }
    }

    suspend fun saveScreenShot(screenShotsEntity: EntityScreenShots) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.screenshotsDao.hSaveScreenShot(screenShotsEntity)
        }
    }

    suspend fun clearAllChatNotifications(hMessageId: Long) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.messagesDao.clearAllSingleChatNotification(hMessageId)
        }
    }

    suspend fun resetUnseenCount(chatId: Long) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hUpdateChatCount(0, chatId)
        }
    }

    suspend fun getSingleChat(hMessageId: Long): EntityChats? {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hGetSingleChat(hMessageId)
        }
    }

    suspend fun removeSingleMessage(id: Long) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.messagesDao.removeSingleMessage(id)
        }
    }

    fun getMessagesDao(): DaoMessages {

        return recoverDatabase.messagesDao

    }

    suspend fun getAllScreenShots(): List<EntityScreenShots>? {
        return withContext(Dispatchers.IO) {
            recoverDatabase.screenshotsDao.hGetAllScreenShots()
        }
    }

    suspend fun getAllFolders(): List<EntityFolders>? {
        Timber.d("Getting data")
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hGetAllFolders()
        }
    }

    suspend fun getFolderById(id: String): List<EntityStatuses>? {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesDao.hGetFolderById(id)
        }
    }

    suspend fun removeFolder(id: Int) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hRemoveFolder(id)
        }
    }

    suspend fun deleteStatus(id: Int) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesDao.hDeleteStatus((id))
        }
    }

    suspend fun updateFolderById(playListLogo: String?, noOfItems: Int, id: Int) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hUpdateFolderById(
                playListLogo,
                noOfItems,
                id
            )
        }
    }

    suspend fun getFolderByName(playlistName: String): List<EntityFolders>? {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hGetFoldersByName(playlistName)
        }
    }

    suspend fun insertFolder(foldersEntity: EntityFolders): Long {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hInsertFolder(foldersEntity)
        }
    }

    suspend fun deleteScreenShotById(id: Int): Int {
        return withContext(Dispatchers.IO) {
            recoverDatabase.screenshotsDao.hDeleteScreenShotById(id)
        }
    }

        suspend fun clearAlMessages() {
            return withContext(Dispatchers.IO) {
                recoverDatabase.messagesDao.clearAllMessages()
            }
        }

    suspend fun clearAllChat() {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hClearAllChat()
        }

    }

    suspend fun insertStatus(hStatusesEntity: EntityStatuses) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesDao.hInsertStatus(hStatusesEntity)
        }
    }

    suspend fun updateFolderByName(playlistName: String, id: Int) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.statusesFolderDao.hUpdateFolderName(playlistName, id)
        }
    }

    suspend fun deleteSingleChat(title: String) {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hDeleteSingleChat(title)
        }
    }

    suspend fun getSearchItems(query: String):List<EntityChats>? {
        return withContext(Dispatchers.IO) {
            recoverDatabase.chatsDao.hGetSearchItems(query)
        }
    }

    fun getAllPagedChatHeads(): PagingSource<Int, EntityChats>? {
        return recoverDatabase.chatsDao.hGetAllPagedChatHeads()
    }


}
