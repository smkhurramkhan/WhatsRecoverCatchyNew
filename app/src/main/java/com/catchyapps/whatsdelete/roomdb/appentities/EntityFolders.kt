package com.catchyapps.whatsdelete.roomdb.appentities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class EntityFolders : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var noOfItems = 0
    var playlistName: String? = null
    var playListLogo: String? = null
    var createdDate: String? = null
    var isCheck = false
}