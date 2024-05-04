package com.catchyapps.whatsdelete.appactivities.activitystatussaver.adapterstatusaver

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFolders
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R

class AdaptersaverPlayList(private val playListEntities: List<EntityFolders>?, private val context: Context) :
    RecyclerView.Adapter<AdaptersaverPlayList.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvPlayList: TextView = view.findViewById(R.id.tv_playlist)
        var cbPlaylist: CheckBox = view.findViewById(R.id.cb_playlist)
        var layoutPlayList: RelativeLayout = view.findViewById(R.id.layout_playlist)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist__layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvPlayList.text = playListEntities!![position].playlistName
        holder.cbPlaylist.isChecked = playListEntities[position].isCheck
        holder.layoutPlayList.setOnClickListener { v: View? ->
            if (playListEntities[position].isCheck) {
                holder.cbPlaylist.isChecked = false
                playListEntities[position].isCheck = false
            } else {
                holder.cbPlaylist.isChecked = true
                playListEntities[position].isCheck = true
            }
        }
    }

    override fun getItemCount(): Int {
        return playListEntities?.size ?: 0
    }
}