package com.catchyapps.whatsdelete.roomdb.appentities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class EntityChats {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var unSeenCount = 0
    var lastMessageType = 0
    var title: String? = null
    var date: String? = null
    var createdBy: String? = null
    var chatType: String? = null
    var lastMessage: String? = null
    var lastMessageTime: String? = null
    var chatUserName: String? = null
    var phoneNumber: String? = null
    var typingRecording: String? = null
    var other: String? = null
    var profilePic: ByteArray? = null

    @Ignore
    var profileLInk: String? = null
}