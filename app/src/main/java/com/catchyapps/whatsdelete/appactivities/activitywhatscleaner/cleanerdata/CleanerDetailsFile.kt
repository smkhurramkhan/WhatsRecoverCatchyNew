package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata


data class CleanerDetailsFile(
    var name: String? = null,
    var path: String? = null,
    var color: Int = 0,
    var image: Int = 0,
    var hFormatedSize: String? = null,
    var hSize: Long? = null,
    var mod: Long? = null,
    var ext: String? = null,
    var isSelected: Boolean = false,
    var hType: String? = null,
    var hIsDeleted: Boolean? = null
)

