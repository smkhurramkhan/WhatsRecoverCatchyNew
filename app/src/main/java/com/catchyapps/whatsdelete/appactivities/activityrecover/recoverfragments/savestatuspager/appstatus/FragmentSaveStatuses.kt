package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityStatuses
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_TITLE_ARRAY
import com.catchyapps.whatsdelete.databinding.FragmentStatusesLayoutBinding
import java.util.*


class FragmentSaveStatuses : Fragment() {
    private lateinit var hFragmentStatusesBinding: FragmentStatusesLayoutBinding
    private val hStatusViewModel: VMSaveStatus by viewModels()
    private lateinit var hStatusAdapter: StatusSaveAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hFragmentStatusesBinding = FragmentStatusesLayoutBinding.inflate(
            layoutInflater,
            container,
            false,
        )
        return hFragmentStatusesBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission()

        hStatusViewModel.hSetData(arguments)

        hSubscribeObservers()

    }

    private fun hSubscribeObservers() {
        if (hStatusViewModel.hMediaType == getString(H_TITLE_ARRAY[0]).lowercase(Locale.getDefault())) {
            hStatusViewModel.hImagesFileListLD.observe(viewLifecycleOwner) {
                hSetupView(it)
                hInitRecyclerView(it)


            }
        } else {
            hStatusViewModel.hVideoFilesListLD.observe(viewLifecycleOwner) {
                hSetupView(it)
                hInitRecyclerView(it)
            }
        }

    }


    private fun hInitRecyclerView(list: List<EntityStatuses>) {
        hStatusAdapter = StatusSaveAdapter(
            requireActivity(),
            false,
            requireActivity()
        ).also {
            it.hSetData(list)
        }
        hFragmentStatusesBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(
                activity,
                3
            )
            adapter = hStatusAdapter
        }


    }


    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (currentAPIVersion >= Build.VERSION_CODES.M && currentAPIVersion <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.permission_necessary))
                    .setMessage(getString(R.string.storage_permission_is_necessary_to_status_media_files))
                        .setPositiveButton(getString(R.string.allow)) { dialog: DialogInterface, which: Int ->
                            dialog.cancel()
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                            )
                        }
                        .setNegativeButton(
                            getString(R.string.cancel)
                        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                    )
                }
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    private fun hSetupView(list: List<EntityStatuses>) {
        if (list.isNotEmpty()) {
            hFragmentStatusesBinding.recyclerView.visibility = View.VISIBLE
            hFragmentStatusesBinding.layoutNotfound.visibility = View.GONE
        } else {
            hFragmentStatusesBinding.recyclerView.visibility = View.GONE
            if (hStatusViewModel.hMediaType ==getString( H_TITLE_ARRAY[0]).lowercase(Locale.getDefault())) {
                hFragmentStatusesBinding.layoutNotfound.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.nofiles_img,
                    0,
                    0
                )
                hFragmentStatusesBinding.layoutNotfound.setText(R.string.noimagestatus)
            } else {
                hFragmentStatusesBinding.layoutNotfound.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.nofiles_img,
                    0,
                    0
                )
                hFragmentStatusesBinding.layoutNotfound.setText(R.string.novideostatus)
            }
            hFragmentStatusesBinding.layoutNotfound.visibility = View.VISIBLE
        }
    }


    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123
    }

}

