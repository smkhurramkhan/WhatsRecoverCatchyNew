package com.catchyapps.whatsdelete.appactivities.myapplanguage.languageadapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.appactivities.myapplanguage.countrymodel.ModelCountry
import com.catchyapps.whatsdelete.appactivities.myapplanguage.viewholdercountry.VHCountry
import com.catchyapps.whatsdelete.databinding.ItemCountryListLayoutBinding


class AdapterCountry(
    val context: Context,
    private val countryList: MutableList<ModelCountry>,
    val onClick: (item: ModelCountry) -> Unit
) : RecyclerView.Adapter<VHCountry>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHCountry {
        return VHCountry(
            ItemCountryListLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    override fun onBindViewHolder(holder: VHCountry, position: Int) {
        val country = countryList[position]
        val width = 120
        val height = 90

        val newDrawable: Drawable = context.resources.getDrawable(country.countryFlag)
        newDrawable.setBounds(0, 0, width, height)

        holder.binding.apply {
            countryName.text = country.countryName
           // countryName.setCompoundDrawablesWithIntrinsicBounds(newDrawable, null, null, null)
            countryName.setCompoundDrawablesRelative(null, null, newDrawable, null)
           // countryFlag.setImageResource(country.countryFlag)
        }

        holder.binding.countryName.setOnClickListener {
            onClick(country)
        }
    }
}