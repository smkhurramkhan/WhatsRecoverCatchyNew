package com.catchyapps.whatsdelete.appactivities.activitydirectchat.countrycodeadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.countrymodel.CountryModel
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.countryviewholder.CountriesViewHolder
import com.catchyapps.whatsdelete.databinding.CountiresItemLayoutBinding
import java.util.*

class CountryCodeAdapterClass(
    var dataList: List<CountryModel>,
    context: Context,
    val callback: (countryModel: CountryModel?, position: Int) -> Unit
) : RecyclerView.Adapter<CountriesViewHolder>(),
    Filterable {
    var filterList: List<CountryModel>
    var context: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountriesViewHolder {
        return CountriesViewHolder(
            CountiresItemLayoutBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: CountriesViewHolder, position: Int) {
        val country = filterList[position]
        holder.binding.countryname.text = country.countryname
        holder.binding.countrycode.text = country.countrycode
        holder.itemView.setOnClickListener {
            callback(country, position)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterList = if (charString.isEmpty()) {
                    dataList
                } else {
                    val filteredList: MutableList<CountryModel> = ArrayList()
                    for (row in dataList) {

                        if (row.countryname.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        } else if (row.countrycode.contains(charString.lowercase(Locale.getDefault()))) {
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
                filterList = filterResults.values as List<CountryModel>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }


    override fun getItemViewType(position: Int): Int {
        return position
    }

    init {
        filterList = dataList
        this.context = context
    }
}