package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanertabactivity.filefragment.FragmentFiles

class CleanerAdapterTabs(
    fm: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fm, lifecycle) {


    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return FragmentFiles.newInstance(position)
    }

}