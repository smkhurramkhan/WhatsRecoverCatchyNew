package com.catchyapps.whatsdelete.roomdb.appdao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catchyapps.whatsdelete.roomdb.appentities.EntityMessages

@Dao
interface DaoMessages {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChildNotification(messagesEntity: EntityMessages): Long


    @Query("select * from EntityMessages where ChatHeaderId=:notificationHeaderId order by timeStamp desc")
    fun getPagedMessagesList(notificationHeaderId: Long): PagingSource<Int, EntityMessages>?

    @Query("delete from EntityMessages where id=:id")
    fun removeSingleMessage(id: Long)

    @Query("delete from EntityMessages where ChatHeaderId=:chatId")
    fun clearAllSingleChatNotification(chatId: Long)

    @Query("delete from EntityMessages")
    fun clearAllMessages()
}