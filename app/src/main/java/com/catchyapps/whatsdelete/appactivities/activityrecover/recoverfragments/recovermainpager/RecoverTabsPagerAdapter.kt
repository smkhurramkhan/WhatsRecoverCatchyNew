package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class RecoverTabsPagerAdapter(
    manager: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(manager, lifecycle) {

    private var mFragmentList: List<Fragment> = ArrayList()

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }


    fun hSetFragmentList(fragmentPagerList: List<Fragment>) {
        mFragmentList = fragmentPagerList
    }


}