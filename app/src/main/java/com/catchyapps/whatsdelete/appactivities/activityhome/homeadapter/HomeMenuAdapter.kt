package com.catchyapps.whatsdelete.appactivities.activityhome.homeadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.appactivities.activityhome.menumodels.ModelMenu
import com.catchyapps.whatsdelete.appactivities.activityhome.menuviewholder.VHMenuStatusScreen
import com.catchyapps.whatsdelete.databinding.ItemCardviewLayoutBinding

class HomeMenuAdapter(
    private var dataList: List<ModelMenu>,
    private val onClick: (item: ModelMenu) -> Unit

) :
    RecyclerView.Adapter<VHMenuStatusScreen>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHMenuStatusScreen {
        return VHMenuStatusScreen(
            ItemCardviewLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false

            )
        )
    }

    override fun onBindViewHolder(holder: VHMenuStatusScreen, position: Int) {
        val model = dataList[position]

        holder.binding.ivicon.setImageResource(model.icon)
        holder.binding.ivtitle.text = model.title

        holder.binding.parent.setOnClickListener {
            onClick(model)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}