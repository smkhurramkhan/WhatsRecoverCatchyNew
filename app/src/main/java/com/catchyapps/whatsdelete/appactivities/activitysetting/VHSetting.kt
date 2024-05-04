package com.catchyapps.whatsdelete.appactivities.activitysetting

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.catchyapps.whatsdelete.R


class VHSetting(
    val itemView: View,
) : DragDropSwipeAdapter.ViewHolder(itemView) {
    val hTitle: TextView = itemView.findViewById(R.id.title)
    val hDragIcon: ImageView = itemView.findViewById(R.id.dragicon)
    val hMainIcon: ImageView = itemView.findViewById(R.id.icon)
}
