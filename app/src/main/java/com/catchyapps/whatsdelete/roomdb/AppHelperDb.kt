package com.catchyapps.whatsdelete.roomdb

import android.content.Context
import androidx.paging.PagingSource
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import com.catchyapps.whatsdelete.roomdb.appdao.DaoChats
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
    private lateinit var hRecoverDatabase: DatabaseRecover

    fun initializeDb(context: Context) {
        hRecoverDatabase = DatabaseRecover.getDbInstance(context)!!
    }

    suspend fun hGetChatByTitle(finalTitle: String): EntityChats? {
        return withContext(Dispatchers.IO) {
            return@withContext hRecoverDatabase.chatsDao.hGetChatByTitle(finalTitle)
        }
    }

    suspend fun hUpdateChatRow(
        count: Int,
        time: String,
        finalText: String,
        dpPath: ByteArray?,
        id: Long
    ) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hUpdateChatRow(
                count,
                time,
                finalText,
                dpPath,
                id
            )
        }
    }

    suspend fun hInsertChatRow(chatsEntity: EntityChats): Long {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hInsertChatRow(chatsEntity)
        }
    }

    suspend fun hInsertChildNotification(messagesEntity: EntityMessages): Long {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.messagesDao.insertChildNotification(messagesEntity)
        }
    }

    suspend fun hSaveScreenShot(screenShotsEntity: EntityScreenShots) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.screenshotsDao.hSaveScreenShot(screenShotsEntity)
        }
    }

    suspend fun hClearAllChatNotifications(hMessageId: Long) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.messagesDao.clearAllSingleChatNotification(hMessageId)
        }
    }

    suspend fun hGetSingleChat(hMessageId: Long): EntityChats? {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hGetSingleChat(hMessageId)
        }
    }

    suspend fun hRemoveSingleMessage(id: Long) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.messagesDao.removeSingleMessage(id)
        }
    }

    fun hGetMessagesDao(): DaoMessages {

        return hRecoverDatabase.messagesDao

    }

    suspend fun hGetAllScreenShots(): List<EntityScreenShots>? {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.screenshotsDao.hGetAllScreenShots()
        }
    }

    suspend fun hGetAllFolders(): List<EntityFolders>? {
        Timber.d("Getting data")
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hGetAllFolders()
        }
    }

    suspend fun hGetfolderById(id: String): List<EntityStatuses>? {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesDao.hGetFolderById(id)
        }
    }

    suspend fun hRemoveFolder(id: Int) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hRemoveFolder(id)
        }
    }

    suspend fun hDeleteStatus(id: Int) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesDao.hDeleteStatus((id))
        }
    }

    suspend fun hUpdateFolderById(playListLogo: String?, noOfItems: Int, id: Int) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hUpdateFolderById(
                playListLogo,
                noOfItems,
                id
            )
        }
    }

    suspend fun hGetfolderByName(playlistName: String): List<EntityFolders>? {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hGetFoldersByName(playlistName)
        }
    }

    suspend fun hInsertFolder(foldersEntity: EntityFolders): Long {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hInsertFolder(foldersEntity)
        }
    }

    suspend fun hDeleteScreenShotById(id: Int): Int {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.screenshotsDao.hDeleteScreenShotById(id)
        }
    }

        suspend fun hClearAlMessages() {
            return withContext(Dispatchers.IO) {
                hRecoverDatabase.messagesDao.clearAllMessages()
            }
        }

    suspend fun hClearAllChat() {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hClearAllChat()
        }

    }

    fun hGetChatDao(): DaoChats {
        return hRecoverDatabase.chatsDao
    }

    suspend fun hInsertSattus(hStatusesEntity: EntityStatuses) {
        Timber.d("hInsertSattus")
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesDao.hInsertStatus(hStatusesEntity)
        }
    }

    suspend fun hUpdateFolderByName(playlistName: String, id: Int) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.statusesFolderDao.hUpdateFolderName(playlistName, id)
        }
    }

    suspend fun hDeleteSingleChat(title: String) {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hDeleteSingleChat(title)
        }
    }

    suspend fun hGetSearchItems(query: String):List<EntityChats>? {
        return withContext(Dispatchers.IO) {
            hRecoverDatabase.chatsDao.hGetSearchItems(query)
        }
    }

    fun hGetAllPagedChatHeads(): PagingSource<Int, EntityChats>? {
        return hRecoverDatabase.chatsDao.hGetAllPagedChatHeads()
    }


}
