package com.catchyapps.whatsdelete.appactivities.activityrecover

import androidx.fragment.app.Fragment

sealed class ViewStateShared(

) {
    data class OnUpdatePager(
        val hPagerFragmentsList: List<Fragment>? = null,
        val hFragmentNamesList: List<String>? = null,
    ) : ViewStateShared()

    data class OnLaunchIntent(
        var hIsLaunchSettingsIntent: Boolean? = null,
        var hIsLaunchUriIntent: Boolean? = null,
    ) : ViewStateShared()

    object OnUpdateDocs : ViewStateShared()
}

