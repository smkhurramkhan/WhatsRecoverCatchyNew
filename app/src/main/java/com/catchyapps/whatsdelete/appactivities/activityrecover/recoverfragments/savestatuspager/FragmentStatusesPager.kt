package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.VPMainAdapter
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.savedstatus.StatusSaveFragment
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.savestatuspager.appstatus.FragmentSaveStatuses
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.HowToUseStatusFragmentScreen
import com.catchyapps.whatsdelete.appactivities.activitystatussaver.VMStatusShared
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_TITLE_ARG
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_TITLE_ARRAY
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.databinding.FragmentMainScreenBinding
import timber.log.Timber
import java.util.*


class FragmentStatusesPager : Fragment() {
    private var hPrefs: MyAppSharedPrefs? = null
    private lateinit var hMainFragmentBinding: FragmentMainScreenBinding
    var uri: Uri? = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        hWhatAppMainUri
    )
    var treeUri: Uri? = DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        hWhatAppMainUri
    )
    private val viewModel: VMStatusShared by activityViewModels()

    private val hCheckStorageAccessForResults = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            Timber.d("Result Ok")

            result.data?.data?.let { hDirectoryUri ->
                requireActivity().contentResolver.takePersistableUriPermission(
                    hDirectoryUri,
                    FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
                )
                hInitViewPager()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("Oncreate")
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        hPrefs = MyAppSharedPrefs(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        hMainFragmentBinding = FragmentMainScreenBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hMainFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (CleanerConstans.hCheck11thUpSdk()) {

            if (MyAppPermissionUtils.hasPermissionPost10(requireContext())) {

                Timber.d("Permission Granted")

            } else {

                hCheckStorageAccessForResults.launch(
                    Intent(ACTION_OPEN_DOCUMENT_TREE).apply {
                        flags = FLAG_GRANT_READ_URI_PERMISSION or
                                FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                FLAG_GRANT_WRITE_URI_PERMISSION
                        putExtra(
                            DocumentsContract.EXTRA_INITIAL_URI,
                            uri
                        )
                    }
                )

            }

        }

        hInitViewPager()
    }


    private fun hInitViewPager() {
        val mainViewPagerAdapter =
            VPMainAdapter(
                childFragmentManager
            )

        mainViewPagerAdapter.swipeLocked = true

        for (aTitleArray in H_TITLE_ARRAY) {
            val fragment = FragmentSaveStatuses()
            val bundle = Bundle()
            bundle.putString(H_TITLE_ARG, getString(aTitleArray).lowercase(Locale.getDefault()))
            fragment.arguments = bundle
            mainViewPagerAdapter.addFragment(fragment, getString(aTitleArray))
        }
        val statusSavedFragment = StatusSaveFragment()
        mainViewPagerAdapter.addFragment(statusSavedFragment, getString(R.string.saved))
        hMainFragmentBinding.viewpager.adapter = mainViewPagerAdapter
        hMainFragmentBinding.tabs.setupWithViewPager(hMainFragmentBinding.viewpager)
        hMainFragmentBinding.tabs.setOnClickListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.only_tab_clicked),
                Toast.LENGTH_LONG
            ).show()
        }
        if (hPrefs?.isFirstTimeStatusDialog == true) {
            dialogStatus()
            hPrefs?.hSetFirstTime(false)

        }
        viewModel.position.observe(requireActivity()) {
            hMainFragmentBinding.viewpager.currentItem = it
        }
    }

    private fun dialogStatus() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.status))
        builder.setMessage(getString(R.string.if_you_don_t_save_statuses_they_will_disappear_after_24_hours))
        builder.setPositiveButton(getString(R.string.ok)) { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            hPrefs!!.isFirstTimeStatusDialog = false
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        requireActivity().menuInflater.inflate(R.menu.menu_status_main, menu)
        super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_prem -> {
                val intent = Intent(requireContext(), ActivityPremium::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_help -> {
                showInfoDialog()
            }
            R.id.action_whatsapp -> {
                var intent =
                    requireActivity().packageManager.getLaunchIntentForPackage("com.whatsapp")
                if (intent != null) {
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                } else {
                    intent = Intent(ACTION_VIEW)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    intent.data = Uri.parse("market://details?id=" + "com.whatsapp")
                }
                startActivity(intent)
            }
        }
        return false
    }

    private fun showInfoDialog() {
        val manager = childFragmentManager
        val fragment = HowToUseStatusFragmentScreen.newInstance()
        fragment.show(manager, "")
    }
}
