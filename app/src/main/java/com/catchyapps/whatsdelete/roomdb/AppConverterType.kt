package com.catchyapps.whatsdelete.roomdb

import android.net.Uri
import androidx.room.TypeConverter

class AppConverterType {
    @TypeConverter
    fun fromUriToString(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun stringToUri(string: String?): Uri? {
        string?.let {
            return Uri.parse(it)
        }
        return null
    }
}
