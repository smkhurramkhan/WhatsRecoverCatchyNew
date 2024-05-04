package com.catchyapps.whatsdelete.roomdb.appentities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EntityFiles {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var mimeType: String? = null
    var title: String? = null
    var from: String? = null
    var filePath: String? = null
    var fileUri: String? = null
    var timeStamp: String? = null
    var ticker: String? = null
    var other: String? = null
    var fileSize: Long = 0
    var favourite // 0 for not favourite, 1 for favourite
            = 0
    var isPlaying = false
    var isIsfav = false
        private set

    fun setIsfav(isfav: Boolean) {
        isIsfav = isfav
    }
}