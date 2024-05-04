package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recoverchat

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.paging.PagingData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.databinding.LayoutItemNotificationHeaderBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class RecoverChatPagingAdapter(
    val hContext: Context
) : PagingDataAdapter<EntityChats, RecoverChatPagingAdapter.ChatViewHolder>(DIFF_CALLBACK), Filterable {


    private var selectedIds = SparseArray<String>()

    inner class ChatViewHolder(val binding: LayoutItemNotificationHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EntityChats>() {
            override fun areItemsTheSame(
                oldItem: EntityChats,
                newItem: EntityChats
            ): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: EntityChats,
                newItem: EntityChats
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutItemNotificationHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val hChatEntity = getItem(position)

        if (hChatEntity?.profilePic != null) {
            Glide.with(hContext)
                .load(hChatEntity.profilePic)
                .error(R.drawable.icon_user_default)
                .into(holder.binding.ivChatProfileImage)
        } else
            holder.binding.ivChatProfileImage.setImageResource(R.drawable.icon_user_default)

        holder.binding.lastMessage.text = hChatEntity?.lastMessage
        val userName: String? = hChatEntity?.title
        holder.binding.name.text = userName

        if (hChatEntity?.lastMessageTime == "0") holder.binding.messageTime.visibility =
            View.GONE else {
            holder.binding.messageTime.visibility = View.VISIBLE
            try {
                if (hChatEntity != null) {
                    val timeAgo = hChatEntity.lastMessageTime?.toLong()
                        ?.let { MyAppUtils.getLastSeenDateTime(it) }
                    holder.binding.messageTime.text = timeAgo
                }
            } catch (e: NumberFormatException) {
                holder.binding.messageTime.text = ""
            }
        }


        if (hChatEntity?.unSeenCount!! > 0) {
            holder.binding.unseenNotificationCount.visibility = View.VISIBLE
            if (hChatEntity.unSeenCount < 1000)
                holder.binding.unseenNotificationCount.text = hChatEntity.unSeenCount.toString()
            else
                holder.binding.unseenNotificationCount.text = "999"
        } else holder.binding.unseenNotificationCount.visibility = View.GONE

        if (selectedIds.indexOfKey(position) > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.binding.layoutRoot.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        hContext,
                        R.color.colorControlActivated
                    )
                )
            } else holder.binding.layoutRoot.background = ColorDrawable(
                ContextCompat.getColor(
                    hContext,
                    R.color.colorControlActivated
                )
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                holder.binding.layoutRoot.foreground = ColorDrawable(
                    ContextCompat.getColor(
                        hContext,
                        R.color.colorTransparent
                    )
                ) else
                holder.binding.layoutRoot.background = ColorDrawable(
                    ContextCompat.getColor(
                        hContext,
                        R.color.colorTransparent
                    )
                )
        }
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    fun hGetItem(position: Int): EntityChats? {
        return getItem(position)
    }

    override fun getFilter(): Filter {
        return hChatFilter
    }

    private val hChatFilter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val hQuery = constraint.toString()
            var hFilteredList: List<EntityChats>?
            if (hQuery.isEmpty()) {
                hFilteredList = snapshot().items
            } else {
                snapshot().items.filter {
                    it.title!!.lowercase(Locale.getDefault())
                        .contains(hQuery.lowercase(Locale.getDefault()))
                }.apply {
                    hFilteredList = this
                }
            }
            val hFilterResults = FilterResults()
            hFilterResults.values = hFilteredList
            return hFilterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            CoroutineScope(Dispatchers.Main).launch {
                val hResultsList: List<EntityChats> = results?.values as List<EntityChats>

                val hList: PagingData<EntityChats> = PagingData.from(hResultsList)
                submitData(hList)
            }
        }
    }


}
