package com.catchyapps.whatsdelete.roomdb.appentities

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity
@Parcelize
data class EntityStatuses(
    @PrimaryKey(autoGenerate = true)
    var id: Int =0,
    var name: String? = null,
    var path: String? = null,
    var filename: String? = null,
    var fileSize: Long? = null,
    var folderId: String? = null,
    var savedPath: String? = null,
    var sharedPath: String? = null,
    var uri: Uri? = null,
    var type: String? = null,
) : Parcelable {
}