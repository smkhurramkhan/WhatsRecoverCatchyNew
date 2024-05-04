package com.catchyapps.whatsdelete.appactivities.activitystickers.stickeradapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.DescSticker
import com.catchyapps.whatsdelete.databinding.ItemSingleCategoryListBinding

class AdapterSingleStickerCategory(val context: Context,
                                   val onAddStickerClick:(item: DescSticker)->Unit,
                         val stickerList: List<DescSticker>): RecyclerView.Adapter<AdapterSingleStickerCategory.StickerItemVH>() {
    inner class StickerItemVH(val binding: ItemSingleCategoryListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerItemVH {
        val view = ItemSingleCategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickerItemVH(view)
    }

    override fun getItemCount(): Int {
        return stickerList.size
    }

    override fun onBindViewHolder(holder: StickerItemVH, position: Int) {
        val stickerItem = stickerList[position]

        Glide.with(context).load("file:///android_asset/${stickerItem.imagePath}")
            .error(R.drawable.icon_sticker)
            .into(holder.binding.ivSticker)
        holder.itemView.setOnClickListener {
            onAddStickerClick(stickerItem)
        }

    }
}