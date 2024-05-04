package com.catchyapps.whatsdelete.appactivities.activitystickers.stickeradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.ListModelStickers
import com.catchyapps.whatsdelete.databinding.RvStickersItemLayoutBinding

class AdapterSticker(val context: Context,
                     val stickerList: List<ListModelStickers>,
                     val onAddStickerClick:(item:ListModelStickers)->Unit
) :RecyclerView.Adapter<AdapterSticker.StickersViewHolder>() {
    inner class StickersViewHolder(val binding: RvStickersItemLayoutBinding): ViewHolder(binding.root)

    private var adapterStickerItem:AdapterStickerItem?= null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):StickersViewHolder {
      val view = RvStickersItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickersViewHolder(view)
    }

    override fun getItemCount(): Int {
       return stickerList.size
    }

    override fun onBindViewHolder(holder: StickersViewHolder, position: Int) {
        val stickerItem = stickerList[position]
        holder.binding.apply {
            categoryName.text = stickerItem.category
            adapterStickerItem= AdapterStickerItem(context,stickerItem.stickersList)
            rvStickers.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
            rvStickers.adapter =adapterStickerItem
            btnAddToWhatsapp.setOnClickListener {
                onAddStickerClick(stickerItem)
            }

        }

    }
}