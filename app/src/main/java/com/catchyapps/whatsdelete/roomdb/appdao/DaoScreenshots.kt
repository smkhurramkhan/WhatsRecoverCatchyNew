package com.catchyapps.whatsdelete.roomdb.appdao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.catchyapps.whatsdelete.roomdb.appentities.EntityScreenShots

@Dao
interface DaoScreenshots {
    @Insert
     fun hSaveScreenShot(screenShotsEntity: EntityScreenShots)

    @Query("select * from entityscreenshots")
     fun hGetAllScreenShots(): List<EntityScreenShots>?

    @Query("select * from entityscreenshots where id=:id")
     fun hGetScreenShotById(id: Int): EntityScreenShots?

    @Query("delete from entityscreenshots where id=:id")
     fun hDeleteScreenShotById(id: Int): Int
}