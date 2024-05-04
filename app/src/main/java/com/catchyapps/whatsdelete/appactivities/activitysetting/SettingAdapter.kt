package com.catchyapps.whatsdelete.appactivities.activitysetting

import android.view.View
import com.catchyapps.whatsdelete.basicapputils.MyAppShareModel
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter

class SettingAdapter(
    dataSet: List<MyAppShareModel> = emptyList(),
) : DragDropSwipeAdapter<MyAppShareModel, VHSetting>(dataSet) {


    override fun getViewHolder(itemView: View) = VHSetting(itemView)

    override fun onBindViewHolder(item: MyAppShareModel, viewHolder: VHSetting, position: Int) {
        viewHolder.hTitle.text = item.title
        viewHolder.hDragIcon.setImageResource(item.dragicon)
        viewHolder.hMainIcon.setImageResource(item.icon)

    }

    override fun getViewToTouchToStartDraggingItem(
        item: MyAppShareModel,
        viewHolder: VHSetting,
        position: Int
    ): View {
        return viewHolder.hDragIcon

    }

}