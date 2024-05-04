package com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata

import com.catchyapps.whatsdelete.basicapputils.empty

data class ListModelStickers(
    val category: String=String.empty,
   val stickersList: List<DescSticker>
)
