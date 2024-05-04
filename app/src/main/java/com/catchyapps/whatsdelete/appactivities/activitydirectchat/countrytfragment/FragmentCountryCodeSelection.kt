package com.catchyapps.whatsdelete.appactivities.activitydirectchat.countrytfragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.DirectChatScreenActivity
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.countrycodeadapter.CountryCodeAdapterClass
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.selectionhelper.CountriesDataRepo
import com.catchyapps.whatsdelete.appactivities.activitydirectchat.namesetlistener.SetMyName
import com.catchyapps.whatsdelete.databinding.FragmentDialogCountrySelectorLayoutBinding

class FragmentCountryCodeSelection : DialogFragment() {
    private var callBackActivity: DirectChatScreenActivity? = null
    private var prefs: MyAppSharedPrefs? = null
    var adapter: CountryCodeAdapterClass? = null
    private var setname: com.catchyapps.whatsdelete.appactivities.activitydirectchat.namesetlistener.SetMyName? = null
    var isDataIsSearched = false
    private lateinit var binding: FragmentDialogCountrySelectorLayoutBinding


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setname = context as com.catchyapps.whatsdelete.appactivities.activitydirectchat.namesetlistener.SetMyName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callBackActivity = DirectChatScreenActivity()
        prefs = MyAppSharedPrefs(activity)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDialogCountrySelectorLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val countries = com.catchyapps.whatsdelete.appactivities.activitydirectchat.selectionhelper.CountriesDataRepo.hGetCountriesList()

        adapter = CountryCodeAdapterClass(countries,
            requireContext(),
            callback = { country, _ ->
                dismiss()
                setname?.setMyName(country?.countrycode)
            }
        )

        binding.countrycodeslist.layoutManager = LinearLayoutManager(context)
        val divider =
            DividerItemDecoration(binding.countrycodeslist.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.custom_divider)
            ?.let { divider.setDrawable(it) }


        binding.countrycodeslist.addItemDecoration(divider)
        binding.countrycodeslist.adapter = adapter
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                adapter?.filter?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                isDataIsSearched = true
                //  filterItems(newText);
                adapter?.filter?.filter(newText)
                return false
            }
        })
    }


    companion object {
        @JvmStatic
        fun newInstance(): FragmentCountryCodeSelection {
            return FragmentCountryCodeSelection()
        }
    }
}