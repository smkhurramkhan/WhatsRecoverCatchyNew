package com.catchyapps.whatsdelete.roomdb.appentities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EntityScreenShots {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var path: String? = null
    var dateTime: String? = null
    var name: String? = null
}