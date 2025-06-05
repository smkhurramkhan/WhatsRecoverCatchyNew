package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager

import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.activityrecover.TypesIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.SharedVM
import com.catchyapps.whatsdelete.appactivities.activityrecover.ViewStateShared.OnLaunchIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.ViewStateShared.OnUpdatePager
import com.catchyapps.whatsdelete.appactivities.activitysetting.SettingsScreen
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.databinding.MainFragmentRecoveryBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class FragmentReoverMainPager : Fragment() {
    private lateinit var hFragmentRecoveryMainBinding: MainFragmentRecoveryBinding
    private var hTabsPagerAdapter: RecoverTabsPagerAdapter? = null
    private var notificationListenerDialog: AlertDialog? = null
    private val hSharedVM by activityViewModels<SharedVM>()

    private val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    private val ANDROID_DOCID = "primary:Android/media/com.whatsapp/WhatsApp/Media"
    var uri: Uri? = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        ANDROID_DOCID
    )
    var treeUri: Uri? = DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        ANDROID_DOCID
    )

    private var appSharedPrefs: MyAppSharedPrefs? = null

    private var position:Int = 0


    private val hSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            hSharedVM.hUpdateSettings(TypesIntent.H_SETTINGS)
        }
    }

    private val hStoragePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            Timber.d(" Result : ${result.data?.data}")

            val mContentResolver: ContentResolver = requireActivity().contentResolver


            val directoryUri = result.data?.data ?: return@registerForActivityResult
            mContentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

//            hSharedVM.hUpdateSettings(TypesIntent.H_URI, result)
        }
    }

    private val hNotificationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (isNotificationListenerEnable) {
                if (notificationListenerDialog != null) {
                    notificationListenerDialog!!.dismiss()
                    notificationListenerDialog!!.cancel()
                    hSharedVM.hSetFirstTime(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appSharedPrefs = MyAppSharedPrefs(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hFragmentRecoveryMainBinding = MainFragmentRecoveryBinding.inflate(
            inflater,
            container,
            false
        )

        val bundle = this.arguments
        if (bundle != null) {
            position = bundle.getInt("position", 0)
        }

        return hFragmentRecoveryMainBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isNotificationListenerEnable) {
            hSharedVM.hSetFirstTime(false)
            showNotificationListenerDialog()
        }

        hSharedVM.hGetPagerList()

        hSubscribeObservers()

        loadAds()

    }


    private fun hSubscribeObservers() {
        hSharedVM.hSharedViewStateLD.observe(viewLifecycleOwner) {

            when (it) {
                is OnUpdatePager -> {
                    hSetPagerFragments(it)
                }
                is OnLaunchIntent -> {
                    hLaunchIntent(it)
                }
                else -> Unit
            }
        }
    }


    private fun hLaunchIntent(onLaunchIntent: OnLaunchIntent) {
        onLaunchIntent.apply {
            hIsLaunchSettingsIntent?.let { isLaunchSettingsIntent ->
                when (isLaunchSettingsIntent) {
                    true -> hSettingsLauncher.launch(
                        Intent(
                            requireContext(),
                            SettingsScreen::class.java,
                        )
                    )
                    false -> Unit
                }
            }
            hIsLaunchUriIntent?.let { isLaunchUriIntent ->

                when (isLaunchUriIntent) {

                    true -> {

                        if (MyAppPermissionUtils.hasPermissionPost10(requireContext())) {

                            Timber.d("Permission Granted")

                        } else {

                            hStoragePermissionLauncher.launch(
                                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    putExtra(
                                        DocumentsContract.EXTRA_INITIAL_URI,
                                        uri
                                    )
                                }
                            )

                        }

                    }

                    false -> Unit
                }
            }
        }
    }

    private fun hSetPagerFragments(onUpdatePager: OnUpdatePager) {

        onUpdatePager.hFragmentNamesList?.let { fragmentNamesList ->
            onUpdatePager.hPagerFragmentsList?.let { fragmentPagerList ->

                hTabsPagerAdapter = RecoverTabsPagerAdapter(
                    childFragmentManager,
                    lifecycle
                ).also {
                    it.hSetFragmentList(fragmentPagerList)
                }


                hFragmentRecoveryMainBinding.apply {
                    viewpager.adapter = hTabsPagerAdapter

                    val tabLayout: TabLayout = tabs
                    TabLayoutMediator(
                        tabLayout,
                        viewpager
                    ) { tab: TabLayout.Tab, position: Int ->
                        tab.text = fragmentNamesList[position]
                    }.attach()
                    hFragmentRecoveryMainBinding.viewpager.currentItem = position

                }
            }
        }
    }

    private fun loadAds() {
        ShowInterstitial.hideNativeAndBanner(
            hFragmentRecoveryMainBinding.topAdLayout,
            requireContext() as AppCompatActivity
        )

        BaseApplication.showNativeBanner(
            hFragmentRecoveryMainBinding.nativeContainer,
            hFragmentRecoveryMainBinding.shimmerViewContainer
        )


    }

    private val isNotificationListenerEnable: Boolean
        get() {
            val contentResolver = requireActivity().contentResolver
            val enabledNotificationListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            val packageName = requireActivity().packageName

            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(
                packageName
            ))
        }

    private fun showNotificationListenerDialog() {
        val hDialogBuilder = AlertDialog.Builder(requireContext())
        hDialogBuilder.setTitle(getString(R.string.notification_listener_service))
            .setMessage(R.string.we_need_your_permission_desc)
            .setPositiveButton(getString(R.string.goto_settings)) { dialog: DialogInterface, _: Int ->
                dialog.cancel()
                dialog.dismiss()

                hNotificationSettingsLauncher.launch(
                    Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                )
            }
            .setNegativeButton(getString(R.string.not_now)) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
        notificationListenerDialog = hDialogBuilder.create()
        notificationListenerDialog?.setCanceledOnTouchOutside(false)
        notificationListenerDialog?.show()
    }

}