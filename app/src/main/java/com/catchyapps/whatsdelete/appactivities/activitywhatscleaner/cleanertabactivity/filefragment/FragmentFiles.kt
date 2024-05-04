package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanertabactivity.filefragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_AUDIO
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_DOCUMENTS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VOICE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.CleanerAdapterFiles
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.Details
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerTypeFilter.*
import com.catchyapps.whatsdelete.databinding.DocsFragmentCleanerLayoutBinding
import com.catchyapps.whatsdelete.databinding.FragmentImagesCleanerLayoutBinding
import com.catchyapps.whatsdeletestatusaver.screen.screenswhatscleaner.GridSpacingItemDecoration
import timber.log.Timber


class FragmentFiles : Fragment() {
    private var hFilesAdapter: CleanerAdapterFiles? = null
    private var hPostion = 0
    private var hDetailsItem: Details? =null
    private lateinit var hViewBinding: ViewBinding
    private val hFilesViewModel: ViewModelFile by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        when (hDetailsItem?.hTitle) {
            H_DOCUMENTS, H_AUDIO, H_VOICE -> {
                hViewBinding = FragmentImagesCleanerLayoutBinding.inflate(
                    layoutInflater,
                    container,
                    false
                )
            }
            else -> {
                hViewBinding = FragmentImagesCleanerLayoutBinding.inflate(
                    layoutInflater,
                    container,
                    false
                )
            }
        }

        return hViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        hDetailsItem?.let {
            Timber.d("Viewmodel initialize")
            hFilesViewModel.hFetchData(
                detailsItem = it,
                hPostion = hPostion
            )
        }
        hSetClickListeners()
        hInitRecyclerView()
        hSubscribeObservers()


    }

    private fun hSubscribeObservers() {
        hFilesViewModel.hFileDetialsListLD.observe(viewLifecycleOwner) {
            try {
                (hViewBinding as FragmentImagesCleanerLayoutBinding).hProgressbar.visibility = View.GONE
            } catch (e: Exception) {
                (hViewBinding as DocsFragmentCleanerLayoutBinding).hProgressbar.visibility = View.GONE
            }
            hFilesAdapter?.hSetData(it)
        }

        hFilesViewModel.hSelectedFilesSizeLD.observe(viewLifecycleOwner) {
            var hString = getString(R.string.delete_items_blank)
            hString = hString.replace(
                oldValue = getString(R.string._0b),
                newValue = it
            )
            try {
                (hViewBinding as FragmentImagesCleanerLayoutBinding).delete.text = hString
            } catch (e: Exception) {
                (hViewBinding as DocsFragmentCleanerLayoutBinding).delete.text = it
            }
        }

    }

    private fun hSetClickListeners() {
        val hDelB: Button
        val hDateB: Chip
        val hNameB: Chip
        val hSizeB: Chip
        val hNoFilesTv: TextView
        val hSelectAllCb: CheckBox
        val hChipGroup: ChipGroup

        when (hDetailsItem?.hTitle) {
            H_DOCUMENTS, H_AUDIO, H_VOICE -> {
                hDelB = (hViewBinding as FragmentImagesCleanerLayoutBinding).delete
                hDateB = (hViewBinding as FragmentImagesCleanerLayoutBinding).date
                hNameB = (hViewBinding as FragmentImagesCleanerLayoutBinding).name
                hSizeB = (hViewBinding as FragmentImagesCleanerLayoutBinding).size
                hNoFilesTv = (hViewBinding as FragmentImagesCleanerLayoutBinding).nofiles
                hSelectAllCb = (hViewBinding as FragmentImagesCleanerLayoutBinding).selectall
                hChipGroup = (hViewBinding as FragmentImagesCleanerLayoutBinding).hChipGroup
            }
            else -> {
                hDelB = (hViewBinding as FragmentImagesCleanerLayoutBinding).delete
                hDateB = (hViewBinding as FragmentImagesCleanerLayoutBinding).date
                hNameB = (hViewBinding as FragmentImagesCleanerLayoutBinding).name
                hSizeB = (hViewBinding as FragmentImagesCleanerLayoutBinding).size
                hNoFilesTv = (hViewBinding as FragmentImagesCleanerLayoutBinding).nofiles
                hSelectAllCb = (hViewBinding as FragmentImagesCleanerLayoutBinding).selectall
                hChipGroup = (hViewBinding as FragmentImagesCleanerLayoutBinding).hChipGroup
            }
        }

        hChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                hDateB.id -> hFilesViewModel.hApplyFiler(H_DATE)
                hNameB.id -> hFilesViewModel.hApplyFiler(H_NAME)
                hSizeB.id -> hFilesViewModel.hApplyFiler(H_SIZE)
            }
        }



        hSelectAllCb.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            hFilesViewModel.hSelectUnSelectAll(isChecked)
        }
        hDelB.setOnClickListener { v: View? ->
            if (hFilesViewModel.hHasFilesToDelete()) {
                AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to status selected files?")
                    .setCancelable(true)
                    .setPositiveButton(
                        "YES"
                    ) { dialog: DialogInterface?, which: Int ->
                        hFilesViewModel.hInitiateDeletion()
                    }
                    .setNegativeButton(
                        "NO"
                    ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                    .create().show()
            }
        }


    }

    private fun hInitRecyclerView() {
        hFilesAdapter = CleanerAdapterFiles(
            context = requireContext(),
            hCheckBoxCallBack = { fileDetailsItem ->
                hFilesViewModel.hAddRemoveSelectedItem(fileDetailsItem)
            }
        )
        val hLayoutManager: RecyclerView.LayoutManager
        val hRecyclerView: RecyclerView =
            when (hDetailsItem?.hTitle) {
                H_DOCUMENTS, H_AUDIO, H_VOICE -> {
                    hLayoutManager = LinearLayoutManager(context)
                    (hViewBinding as FragmentImagesCleanerLayoutBinding).recyclerView
                }
                else -> {
                    hLayoutManager = GridLayoutManager(context, 3)
                    (hViewBinding as FragmentImagesCleanerLayoutBinding).recyclerView
                }
            }
        hRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = hLayoutManager
            if (hLayoutManager is GridLayoutManager) {
                addItemDecoration(
                    GridSpacingItemDecoration(
                        3,
                        12,
                        12
                    )
                )
            }
            adapter = hFilesAdapter
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = MyAppSharedPrefs(requireContext())

        val detailItem = settings.getDetailItem()


        hDetailsItem = detailItem
        hPostion = arguments?.getInt(CleanerConstans.H_POSITION)!!
    }


    companion object {
        fun newInstance(position: Int): FragmentFiles {
            return FragmentFiles().also { fragment ->
                Bundle().apply {
                    putInt(CleanerConstans.H_POSITION, position)
                }.also { bundle ->
                    fragment.arguments = bundle
                }
            }

        }
    }
}