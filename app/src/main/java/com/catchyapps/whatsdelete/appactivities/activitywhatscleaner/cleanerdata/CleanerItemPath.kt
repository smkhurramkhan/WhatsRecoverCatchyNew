package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata

import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas

data class CleanerItemPath(
    val hType: String? = null,
    val hPath: String? = null,
    val hMyAppSchemas: MyAppSchemas? = null
) {
    override fun toString(): String {
        return "Path Item with type $hType and Path $hPath"
    }
}
