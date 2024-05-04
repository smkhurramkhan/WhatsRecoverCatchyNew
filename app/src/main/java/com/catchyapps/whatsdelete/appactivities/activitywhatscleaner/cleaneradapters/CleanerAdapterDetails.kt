package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.cleanerviewholder.ViewHolderDetails
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.Details
import com.catchyapps.whatsdelete.databinding.ItemCleanerContentLayoutBinding


class CleanerAdapterDetails(
    val clickCallBack: (detailItem: Details, postion: Int) -> Unit
) : RecyclerView.Adapter<ViewHolderDetails>() {
    private var hDetailList = listOf<Details>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderDetails {

        return ViewHolderDetails(
            ItemCleanerContentLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(detailsViewHolder: ViewHolderDetails, position: Int) {
        val hDetailItem = hDetailList[position]
        detailsViewHolder.hItemCleanerMainContentBinding.apply {
            val hData = "${hDetailItem.hFileSizeString} in ${hDetailItem.hFileCount} Files"
            title.text = hDetailItem.hTitle
            data.text = hData
           /* image.circleBackgroundColor = ContextCompat.getColor(
                image.context,
                hDetailItem.hColor
            )*/

            constraintLayout.setBackgroundColor(ContextCompat.getColor(constraintLayout.context, hDetailItem.hColor))

           /* image.borderColor = ContextCompat.getColor(
                image.context,
                hDetailItem.hColor
            )*/
            image.setImageResource(hDetailItem.hImage)
            hCardView.setOnClickListener {
                clickCallBack(
                    hDetailItem,
                    position
                )
            }
        }

    }

    override fun getItemCount(): Int {
        return hDetailList.size
    }

    fun hSetData(list: List<Details>) {
        hDetailList = list
        notifyDataSetChanged()
    }
}
