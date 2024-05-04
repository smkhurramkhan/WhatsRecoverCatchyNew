package com.catchyapps.whatsdelete.basicapputils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MyAppShareModel(
    var icon: Int,
    var title: String,
    var dragicon: Int,
) : Parcelable