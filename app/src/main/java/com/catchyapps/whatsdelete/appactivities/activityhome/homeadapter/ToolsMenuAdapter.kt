package com.catchyapps.whatsdelete.appactivities.activityhome.homeadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.appactivities.activityhome.menumodels.ModelMenu
import com.catchyapps.whatsdelete.appactivities.activityhome.menuviewholder.ToolsViewHolder
import com.catchyapps.whatsdelete.databinding.ItemMoreToolsBinding

class ToolsMenuAdapter(
    private var dataList: List<ModelMenu>,
    private val onClick: (item: ModelMenu) -> Unit

) :
    RecyclerView.Adapter<ToolsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolsViewHolder {
        return ToolsViewHolder(
            ItemMoreToolsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false

            )
        )
    }

    override fun onBindViewHolder(holder: ToolsViewHolder, position: Int) {
        val model = dataList[position]

        holder.binding.ivicon.setImageResource(model.icon)
        holder.binding.ivtitle.text = model.title

        holder.binding.mainCard.setOnClickListener {
            onClick(model)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}