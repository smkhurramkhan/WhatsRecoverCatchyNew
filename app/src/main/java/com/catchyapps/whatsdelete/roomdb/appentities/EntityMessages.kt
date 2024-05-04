package com.catchyapps.whatsdelete.roomdb.appentities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class EntityMessages {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var chatHeaderId: Long = 0
    var messageType = 0
    var chatHeaderName: String? = null
    var title: String? = null
    var body: String? = null
    var imagePath: String? = null
    var timeStamp: String? = null
    var ticker: String? = null
    var other: String? = null
    override fun toString(): String {
        return "ChildNotification{" +
                "notificationHeaderId=" + chatHeaderId +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", ticker='" + ticker + '\'' +
                '}'
    }
}