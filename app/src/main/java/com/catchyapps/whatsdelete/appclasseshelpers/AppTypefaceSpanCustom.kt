package com.catchyapps.whatsdelete.appclasseshelpers

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.StyleSpan

class AppTypefaceSpanCustom(family: String, private val newType: Typeface) :
    StyleSpan(Typeface.NORMAL) {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
        paint.typeface = tf
    }
}