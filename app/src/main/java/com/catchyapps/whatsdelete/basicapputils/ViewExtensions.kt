package com.catchyapps.whatsdelete.basicapputils

import android.view.View

/**
 *  Extension functions for view visibility
 * **/
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}