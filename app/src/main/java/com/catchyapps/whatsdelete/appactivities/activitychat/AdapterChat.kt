package com.catchyapps.whatsdelete.appactivities.activitychat

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppDateUtils
import com.catchyapps.whatsdelete.roomdb.appentities.EntityMessages
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.databinding.ItemScreenChatLayoutBinding

class AdapterChat(
    private val context: Context,
) : PagingDataAdapter<EntityMessages, AdapterChat.MessageVh>(DIFF_CALLBACK) {

    private var selectedIds = SparseArray<String>()

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EntityMessages>() {
            override fun areItemsTheSame(
                oldItem: EntityMessages,
                newItem: EntityMessages
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: EntityMessages,
                newItem: EntityMessages
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageVh {
        return MessageVh(
            ItemScreenChatLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageVh, position: Int) {
        val messageEntity = getItem(position)
        var isNewDay = false
        if (
            messageEntity?.body?.startsWith("http") == true ||
            messageEntity?.body?.startsWith("https") == true ||
            messageEntity?.body?.startsWith("wwww") == true ||
            messageEntity?.body?.endsWith(".com") == true ||
            messageEntity?.body?.endsWith(".net") == true ||
            messageEntity?.body?.endsWith(".pk") == true
        ) {
            holder.binding.textChatMessage.setTextColor(context.resources.getColor(R.color.blue_link))
            val mystring = messageEntity.body
            val content = SpannableString(mystring)
            content.setSpan(UnderlineSpan(), 0, mystring!!.length, 0)
            holder.binding.textChatMessage.text = content
        } else {
            if (messageEntity != null) {
                holder.binding.textChatMessage.text = messageEntity.body
            }
        }
        // If there is at least one item preceding the current one, check the previous message.
        if (position < itemCount - 1) {
            val prevMessage = snapshot().items[position + 1]
            if (messageEntity != null) {
                if (
                    !MyAppDateUtils.hasSameDate(
                        messageEntity.timeStamp!!.toLong(),
                        prevMessage.timeStamp!!.toLong()
                    )
                ) {
                    isNewDay = true
                }
            }
        } else if (position == itemCount - 1) {
            isNewDay = true
        }
        if (isNewDay) {
            holder.binding.cvChatDate.visibility = View.VISIBLE
            if (messageEntity != null) {
                if (MyAppDateUtils.isToday(messageEntity.timeStamp!!.toLong()))
                    holder.binding.textGroupChatDate.setText(R.string.today)
                else
                    holder.binding.textGroupChatDate.text =
                        messageEntity.timeStamp?.toLong()?.let {
                           MyAppDateUtils.formatDate(
                                it
                            )
                        }
            }
        } else {
            holder.binding.cvChatDate.visibility = View.GONE
        }

        messageEntity?.title?.let {
            holder.binding.tvMessageUsername.text = it
        }

        holder.binding.textChatMessage.setOnClickListener { v: View? ->
            if (
                messageEntity?.body?.startsWith("http") == true ||
                messageEntity?.body?.startsWith("https") == true ||
                messageEntity?.body?.startsWith("wwww") == true ||
                messageEntity?.body?.endsWith(".com") == true ||
                messageEntity?.body?.endsWith(".net") == true ||
                messageEntity?.body?.endsWith(".pk") == true
            ) {
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(messageEntity.body)
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context,
                        context.getString(R.string.no_app_found_to_open_this_link), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
        if (messageEntity != null) {
            holder.binding.textChatTime.text =
                MyAppUtils.getTimeFromTimeStamp(messageEntity.timeStamp!!.toLong())
        }
        if (selectedIds.indexOfKey(position) > -1) {
            holder.itemView.foreground =
                ColorDrawable(ContextCompat.getColor(context, R.color.colorControlActivated))
        } else {
            holder.itemView.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context, R.color.colorTransparent
                    )
                )
        }
    }

    fun hGetItem(position: Int): EntityMessages? {
        return getItem(position)
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return snapshot().items.size
    }

    inner class MessageVh(val binding: ItemScreenChatLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)
}
