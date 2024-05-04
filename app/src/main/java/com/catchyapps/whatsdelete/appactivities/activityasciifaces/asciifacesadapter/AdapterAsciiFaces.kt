package com.catchyapps.whatsdelete.appactivities.activityasciifaces.asciifacesadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.catchyapps.whatsdelete.databinding.AsciiRvItemLayoutBinding

class AdapterAsciiFaces(
    val context: Context,
    private val asciiList: List<String>,
    private val onItemClick: (item: String, type: String) -> Unit
) : RecyclerView.Adapter<AdapterAsciiFaces.AsciiVH>() {

    inner class AsciiVH(val binding: AsciiRvItemLayoutBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsciiVH {
        return AsciiVH(
            AsciiRvItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return asciiList.size
    }

    override fun onBindViewHolder(holder: AsciiVH, position: Int) {
        val asciiFileList = asciiList[position]

        with(holder) {
            binding.apply {
                tvAscii.text = asciiFileList

                btnWhatsapp.setOnClickListener {
                    onItemClick(asciiFileList,"whatsapp")
                }

                btnShare.setOnClickListener {
                    onItemClick(asciiFileList,"share")
                }
            }
        }

    }
}