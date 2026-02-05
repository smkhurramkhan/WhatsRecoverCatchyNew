package com.catchyapps.whatsdelete.appactivities.activitycollection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.appactivities.activityshotscreen.ShotsAdapter
import com.catchyapps.whatsdelete.databinding.FragmentScreenshotsSavedLayoutBinding
import kotlinx.coroutines.launch
import timber.log.Timber

class ScreenshotsSavedFragment : Fragment() {
    private var screenShotsEntityList: MutableList<Any>? = null
    private lateinit var fragmentScreenshotsBinding: FragmentScreenshotsSavedLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentScreenshotsBinding = FragmentScreenshotsSavedLayoutBinding.inflate(
            inflater,
            container,
            false
        )

        return fragmentScreenshotsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init() {
        screenShotsEntityList = ArrayList()
    }

    private fun setUpRecyclerView() {
        val recyclerViewAdapter = screenShotsEntityList?.let {
            ShotsAdapter(
                it,
                requireContext(),
                requireActivity()
            )
        }
        fragmentScreenshotsBinding.rvScreenshots.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = recyclerViewAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            AppHelperDb.getAllScreenShots()?.toMutableList()?.let {
                screenShotsEntityList?.clear()
                screenShotsEntityList?.addAll(it)
                Timber.d("${it.size }")
            }

            if (screenShotsEntityList!!.size > 0) {
                fragmentScreenshotsBinding.tvNotFound.visibility = View.GONE
                setUpRecyclerView()
            } else {
                fragmentScreenshotsBinding.tvNotFound.visibility = View.VISIBLE
                fragmentScreenshotsBinding.rvScreenshots.visibility = View.GONE
            }
        }

    }
}