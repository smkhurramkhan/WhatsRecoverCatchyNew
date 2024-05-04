package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils.Companion.getFileDateTime
import com.catchyapps.whatsdelete.databinding.LayoutCustomDocumentBinding
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import java.util.*

class RecoverDocsAdapter(
    private val context: Context?,
    private var filterList: MutableList<EntityFiles>,
) : RecyclerView.Adapter<RecoverDocVhCustom>(), Filterable {

    private var mainChatList: MutableList<EntityFiles>
    private var selectedIds = SparseArray<String>()
    private var hDocsAdapterCallbacks: DocsAdapterCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecoverDocVhCustom {
        return RecoverDocVhCustom(
            LayoutCustomDocumentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecoverDocVhCustom, position: Int) {
        val viewHolder = holder
        val files = filterList[position]
        holder.hCustomDocumentLayoutBinding.ivDocumentType
            .setImageResource(getRecourseImage(files.filePath!!))
        holder.hCustomDocumentLayoutBinding.tvDocumentName.text = files.title
        holder.hCustomDocumentLayoutBinding.tvSizeTime.text = files.timeStamp?.toLong()
            ?.let { getFileDateTime(it) }
       /* viewHolder.hCustomDocumentLayoutBinding.ivMore.setOnClickListener {
            hDocsAdapterCallbacks?.hMoreClick(it, position, files)
        }*/
        viewHolder.hCustomDocumentLayoutBinding.ivDocumentType.setOnClickListener {
            hDocsAdapterCallbacks?.hMoreClick(it, position, files)
        }
        viewHolder.hCustomDocumentLayoutBinding.parent.setOnLongClickListener {
            hDocsAdapterCallbacks?.hLongClick(it, position, files)
            true
        }
        viewHolder.hCustomDocumentLayoutBinding.parent.setOnClickListener {
            hDocsAdapterCallbacks?.hSingleClick(
                it,
                position,
                files
            )
        }
        if (selectedIds.indexOfKey(position) > -1) {
            viewHolder.hCustomDocumentLayoutBinding.parent.foreground = ColorDrawable(
                ContextCompat.getColor(
                    context!!,
                    R.color.colorControlActivated
                )
            )
        } else {
            viewHolder.hCustomDocumentLayoutBinding.parent.foreground =
                ColorDrawable(
                    ContextCompat.getColor(
                        context!!,
                        android.R.color.transparent
                    )
                )
        }
    }

    private fun getRecourseImage(url: String): Int {
        return if (url.contains(".doc") || url.contains(".docx")) {
            R.drawable.ic_word_img
        } else if (url.contains(".pdf")) {
            // PDF file
            R.drawable.ic_pdf_svg
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            R.drawable.ic_ppt_svg
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            R.drawable.ic_excel_svg
        } else if (url.contains(".zip")) {
            // WAV audio file
            R.drawable.ic_zip_svg
        } else if (url.contains(".rar")) {
            // WAV audio file
            R.drawable.ic_rar_svg
        } else if (url.contains(".txt")) {
            // Text file
            R.drawable.ic_txt_svg
        } else {
            R.drawable.ic_doc_svg
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterList = if (charString.isEmpty()) {
                    mainChatList
                } else {
                    val filteredList: MutableList<EntityFiles> = ArrayList()
                    for (row in mainChatList) {
                        if (row.title?.lowercase(Locale.getDefault())
                                ?.contains(charString.lowercase(Locale.getDefault())) == true
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterList = filterResults.values as MutableList<EntityFiles>
                notifyDataSetChanged()
            }
        }
    }

    fun hSetItemCallbacks(docsAdapterCallbacks: DocsAdapterCallbacks?) {
        hDocsAdapterCallbacks = docsAdapterCallbacks
    }

    fun removeItem(position: Int) {
        mainChatList.removeAt(position)
        filterList.removeAt(position)
        notifyDataSetChanged()
    }

    fun hAddItems(docsList: MutableList<EntityFiles>) {
        mainChatList = docsList.toMutableList()
        filterList = docsList
        notifyDataSetChanged()
    }

    fun hGetLists(): List<EntityFiles> {
        return filterList
    }

    interface DocsAdapterCallbacks {
        fun hSingleClick(v: View?, position: Int, files: EntityFiles?)
        fun hLongClick(v: View?, position: Int, files: EntityFiles?)
        fun hMoreClick(v: View?, position: Int, files: EntityFiles?)
    }


    fun getItem(position: Int): EntityFiles {
        return mainChatList[position]
    }

    fun setSelectedIds(selectedIds: SparseArray<String>) {
        this.selectedIds = selectedIds
        notifyDataSetChanged()
    }

    init {
        mainChatList = filterList.toMutableList()
    }
}