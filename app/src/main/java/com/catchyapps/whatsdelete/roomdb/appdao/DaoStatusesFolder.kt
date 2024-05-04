package com.catchyapps.whatsdelete.roomdb.appdao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders

@Dao
interface DaoStatusesFolder {
    @Insert
     fun hInsertFolder(model: EntityFolders): Long

    @Query("SELECT * FROM EntityFolders")
     fun hGetAllFolders(): List<EntityFolders>?

    @Query("SELECT * FROM EntityFolders WHERE id = :id")
     fun hGetSingleFolder(id: Int): EntityFolders?

    @Query("DELETE FROM EntityFolders WHERE id = :id")
     fun hRemoveFolder(id: Int)

    @Update
     fun hUpdateFolder(foldersEntity: EntityFolders)

    @Query("UPDATE EntityFolders SET noOfItems = :itemCount , playListLogo = :logo WHERE id IN (:id) ")
     fun hUpdateFolderById(logo: String?, itemCount: Int, id: Int)

    @Query("UPDATE EntityFolders SET playlistName = :name WHERE id = :id")
     fun hUpdateFolderName(name: String?, id: Int)

    @Query("SELECT * FROM EntityFolders where playlistName=:name")
     fun hGetFoldersByName(name: String?): List<EntityFolders>?
}