package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata

import android.os.Parcelable
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas
import kotlinx.parcelize.Parcelize


@Parcelize
data class Details(
    val hTitle: String,
    var hFileSizeString: String? = null,
    var hFileSizeLong: Long = 0,
    var hPath: String? = null,
    var hImage: Int,
    var hColor: Int,
    var hFileCount: Int = 0,
    var hAllFilesUris: MutableList<String> = mutableListOf(),
    var hSchemaType: SchemaHolder? = null
) : Parcelable

@Parcelize
data class SchemaHolder(
    val hMyAppSchemas1: MyAppSchemas? = null,
    var hMyAppSchemas2: MyAppSchemas? = null,
) : Parcelable
