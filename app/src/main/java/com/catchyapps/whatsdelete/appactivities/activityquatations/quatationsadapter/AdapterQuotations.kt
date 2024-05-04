package com.catchyapps.whatsdelete.appactivities.activityquatations.quatationsadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.catchyapps.whatsdelete.appactivities.activityquatations.data.QuotationsDataClass
import com.catchyapps.whatsdelete.databinding.ItemLayoutQuatationsBinding

class AdapterQuotations(
    private var quotesList: List<QuotationsDataClass>,
    private val onclick: (item: QuotationsDataClass, action: String) -> Unit
) : RecyclerView.Adapter<AdapterQuotations.QuotationViewHolder>() {

    private var originalDataList: List<QuotationsDataClass> = quotesList.toList()
    inner class QuotationViewHolder(val binding: ItemLayoutQuatationsBinding) :
        ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotationViewHolder {
        val view = ItemLayoutQuatationsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuotationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return quotesList.size
    }

    override fun onBindViewHolder(holder: QuotationViewHolder, position: Int) {
        val mQuotesList = quotesList[position]
        holder.binding.apply {
            tvMotivationalQuate.text = mQuotesList.quotes

            btnCopy.setOnClickListener {
                onclick(mQuotesList, "copy")
            }
            btnWhatsapp.setOnClickListener {
                onclick(mQuotesList, "whatsapp")
            }

            btnShare.setOnClickListener {
                onclick(mQuotesList, "share")
            }
        }
    }

    fun filter(query: String) {
        quotesList = if (query.isEmpty()) {
            originalDataList
        } else {
            originalDataList.filter { it.toString().toLowerCase().contains(query.toLowerCase()) }
        }
        notifyDataSetChanged()
    }

}