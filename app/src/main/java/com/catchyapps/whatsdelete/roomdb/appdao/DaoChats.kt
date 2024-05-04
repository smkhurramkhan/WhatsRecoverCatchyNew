package com.catchyapps.whatsdelete.roomdb.appdao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats

@Dao
interface DaoChats {
    @Query("SELECT * FROM EntityChats WHERE title LIKE '%' || :query || '%' ")
    fun hGetSearchItems(query: String?): List<EntityChats>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun hInsertChatRow(chatsEntity: EntityChats): Long

    @Query("SELECT * from EntityChats order by lastMessageTime desc")
    fun hGetAllPagedChatHeads(): PagingSource<Int, EntityChats>?

    @Query("select * from EntityChats where id=:id")
    fun hGetSingleChat(id: Long): EntityChats?

    @Query("select * from EntityChats where title=:title")
    fun hGetChatByTitle(title: String?): EntityChats?

    @Query("delete from EntityChats where id=:id")
    fun hRemoveSingleChat(id: Long)

    @Query(
        "update EntityChats set unSeenCount=:count, " +
                "lastMessageTime=:timestamp," +
                " lastMessage=:lastMessage, " +
                "profilePic=:profilePic where id=:id"
    )
    fun hUpdateChatRow(
        count: Int,
        timestamp: String?,
        lastMessage: String?,
        profilePic: ByteArray?,
        id: Long
    )

    @Query("update EntityChats set unSeenCount=:count where id=:id")
    fun hUpdateChatCount(count: Int, id: Long): Int

    @Query("delete from EntityChats where title=:title")
    fun hDeleteSingleChat(title: String?)

    @Query("delete from EntityChats")
    fun hClearAllChat()
}