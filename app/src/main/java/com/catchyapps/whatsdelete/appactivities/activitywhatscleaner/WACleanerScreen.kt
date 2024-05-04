package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_CLEANER_IC
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.CleanerAdapterDetails
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerVs.*
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanertabactivity.ActivityTabLayout
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.databinding.ScreenCleanerWhatsBinding


class WACleanerScreen : com.catchyapps.whatsdelete.appactivities.BaseActivity() {

    private lateinit var activityWhatsCleanerBinding: ScreenCleanerWhatsBinding
    private var detailsAdapter: CleanerAdapterDetails? = null
    private val whatsCleanerViewModel: VMWhatsCleaner by viewModels()
    private var appSharedPrefs: MyAppSharedPrefs? = null

    private val mContentResolver: ContentResolver by lazy { this.contentResolver }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val directoryUri = result.data?.data ?: return@registerForActivityResult

            mContentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            activityWhatsCleanerBinding.hProgressbar.visibility = View.VISIBLE
            whatsCleanerViewModel.hGetCleaningData()

        }

    }

    val uri: Uri? = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        hWhatAppMainUri
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityWhatsCleanerBinding = ScreenCleanerWhatsBinding.inflate(
            layoutInflater
        )

        setContentView(activityWhatsCleanerBinding.root)

        hCheckForPermissions()

        appSharedPrefs = MyAppSharedPrefs(this)

        hSetupToolbar()

        whatsCleanerViewModel.hGetCleaningData()

        hLoadAds()

        hInitRecyclerView()

        hSetupClickListerns()

        hSubscribeObservers()

    }

    private fun hLaunchIntent(title: String) {
        Intent(
            this@WACleanerScreen,
            ActivityTabLayout::class.java
        ).also { intent ->
            intent.putExtra(H_CLEANER_IC, title)
            hTabLayoutActivityLauncher.launch(
                intent
            )
        }

    }

    private val hTabLayoutActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            if (result.data != null)
                if (result?.data!!.getBooleanExtra("home", false)) {
                    finish()
                }
        }
    }

    private fun hSubscribeObservers() {
        whatsCleanerViewModel.hDetailsListLd.observe(this) {
            activityWhatsCleanerBinding.hProgressbar.visibility = View.GONE
            detailsAdapter?.hSetData(it)
        }
        whatsCleanerViewModel.hTotalSizeLd.observe(this) {
            activityWhatsCleanerBinding.hProgressbar.visibility = View.GONE
            activityWhatsCleanerBinding.data.text = it
        }

        whatsCleanerViewModel.hCleanerVSLD.observe(this) {
            when (it) {
                is OnLaunchIntent -> {
                    hLaunchIntent(it.detailItem)
                }
                is OnShowProgress -> hShowHideProgressDialog(it)
                is OnShowPermissionDialog -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showPermissionDialog()
                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPermissionDialog() {
        storagePermissionLauncher.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    uri
                )
            })
    }

    private fun hShowHideProgressDialog(hOnShowProgress: OnShowProgress) {
        when (hOnShowProgress.hIsShowProgress) {
            true -> activityWhatsCleanerBinding.hProgressbar.visibility = View.VISIBLE
            false -> activityWhatsCleanerBinding.hProgressbar.visibility = View.GONE
        }
        hOnShowProgress.hMessage?.let {
            MyAppUtils.showToast(
                context = this,
                s = hOnShowProgress.hMessage
            )
        }
    }

    private fun hSetupClickListerns() {
        activityWhatsCleanerBinding.clearall.setOnClickListener {
            val hAlertDialog = AlertDialog.Builder(this@WACleanerScreen).create()
            hAlertDialog.setTitle(getString(R.string.clear_all))
            hAlertDialog.setMessage(getString(R.string.are_you_sure_you_want_to_clear_all_the_data))
            hAlertDialog.setIcon(R.drawable.ic_app)
            hAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.clear_all)) { _, _ ->
                whatsCleanerViewModel.hExecuteDeletion(null)
            }
            hAlertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
            hAlertDialog.show()
        }


    }

    private fun hInitRecyclerView() {
        detailsAdapter = CleanerAdapterDetails(
            clickCallBack = { detailItem, _ ->
                whatsCleanerViewModel.hExecuteDeletion(
                    detailItem
                )
            },
        )
        activityWhatsCleanerBinding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = detailsAdapter
            layoutManager = GridLayoutManager(this@WACleanerScreen, 3)
        }

    }

    private fun hLoadAds() {
        ShowInterstitial.hideNativeAndBanner(activityWhatsCleanerBinding.topAdLayout, this)
        BaseApplication.showNativeBannerAdmobOnly(
            activityWhatsCleanerBinding.nativeAd,
            activityWhatsCleanerBinding.shimmerViewContainer
        )
    }

    private fun hSetupToolbar() {
        var title: String? = getString(R.string.whatsapp_cleaner)
        if (intent != null && intent.getStringExtra("title") != null)
            title = intent.getStringExtra("title")

        activityWhatsCleanerBinding.toolbar.apply {
            toolbarTitle.text = title
            btnback.setOnClickListener { onBackPressed() }
            btnPremium.setOnClickListener { startActivity(Intent(this@WACleanerScreen, ActivityPremium::class.java))
                finish()
            }
        }


    }


    private val hLauchSettingsContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (MyAppPermissionUtils.hHasReadWritePermissions(this))
            whatsCleanerViewModel.hGetCleaningData()
    }

    private val hRequestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it.size == 2) {
            whatsCleanerViewModel.hGetCleaningData()
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) || !ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_needed))
                    .setMessage(getString(R.string.storage_permission_is_needed_to_read_whatsapp_media_press_ok_to_enable_in_settings))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                        hLauchSettingsContract.launch(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                                it.data = Uri.fromParts(
                                    "package",
                                    packageName,
                                    null
                                )
                            }
                        )
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        finish()
                    }.create().show()
            }
        }
    }


    private fun hCheckForPermissions() {
        if (!MyAppPermissionUtils.hHasReadWritePermissions(this)) {
            if (MyAppPermissionUtils.hRationaileCheck(this)) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.permission_needed))
                    .setMessage(getString(R.string.storage_permission_is_needed_to_read_whatsapp_media))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                        hRequestPermissions.launch(
                            arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        )
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                        finish()
                    }.create().show()
            } else {

                hRequestPermissions.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }


}