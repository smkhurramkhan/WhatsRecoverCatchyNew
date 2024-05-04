package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata

sealed class CleanerVs {

    data class OnShowPermissionDialog(
       val permission: Boolean? = false
    ) : CleanerVs()

    data class OnLaunchIntent(
        val detailItem: String
    ) : CleanerVs()

    class OnShowProgress(
        var hIsShowProgress: Boolean = false,
        var hMessage: String? = null
    ) : CleanerVs()
}
