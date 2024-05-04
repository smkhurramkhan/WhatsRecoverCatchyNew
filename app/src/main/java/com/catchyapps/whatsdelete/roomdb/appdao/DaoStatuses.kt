package com.catchyapps.whatsdelete.roomdb.appdao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses

@Dao
interface DaoStatuses {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun hInsertStatus(model: EntityStatuses): Long

    @Query("SELECT * FROM EntityStatuses WHERE folderId = :folderId")
     fun hGetFolderById(folderId: String?): List<EntityStatuses>?

    @Query("SELECT * FROM EntityStatuses")
     fun hGetAllStatus(): List<EntityStatuses>?

    @Query("DELETE FROM EntityStatuses WHERE id = :id")
     fun hDeleteStatus(id: Int)

    @Query("UPDATE EntityStatuses SET folderId = :folderId  WHERE id=:id")
     fun hUpdatstatusfolder(folderId: Int, id: Int)
}