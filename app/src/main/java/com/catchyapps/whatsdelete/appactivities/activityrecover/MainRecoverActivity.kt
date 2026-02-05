package com.catchyapps.whatsdelete.appactivities.activityrecover

import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.activityViewModels
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityrecover.ViewStateShared.OnLaunchIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.ViewStateShared.OnUpdatePager
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recoverchat.FragmentChatRecover
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia.RecoverMediaFragment
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc.FragmentRecoverDoc
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice.VoiceMediaFragment
import com.catchyapps.whatsdelete.appactivities.activitysetting.SettingsScreen
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.PopUpDialogHowToUseRecoverFeature
import com.catchyapps.whatsdelete.appnotifications.AppDeletedMessagesNotificationService
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.databinding.ScreenMainRecoveryBinding
import timber.log.Timber
import kotlin.getValue
import kotlin.text.contains

class MainRecoverActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {

    private var appSharedPrefs: MyAppSharedPrefs? = null
    private var hFragmentManager: FragmentManager? = null
    private lateinit var hActivityMainBinding: ScreenMainRecoveryBinding
    private var position = 0
    private var name: String? = null
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private var notificationListenerDialog: AlertDialog? = null

    private val hSharedVM by viewModels<SharedVM>()

    private val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    private val ANDROID_DOCID = "primary:Android/media/com.whatsapp/WhatsApp/Media"
    var uri: Uri? = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        ANDROID_DOCID
    )

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
        if (result.resultCode == RESULT_OK) {

            Timber.d(" Result : ${result.data?.data}")

            val mContentResolver: ContentResolver = contentResolver


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
        if (result.resultCode == RESULT_OK) {
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

        hActivityMainBinding = ScreenMainRecoveryBinding.inflate(layoutInflater)
        setContentView(hActivityMainBinding.root)

        hFragmentManager = supportFragmentManager
        appSharedPrefs = MyAppSharedPrefs(this)
        setSupportActionBar(hActivityMainBinding.toolbar)
        hActivityMainBinding.toolbar.title = resources.getString(R.string.app_name)
        setSupportActionBar(hActivityMainBinding.toolbar)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }
        name = intent.extras?.getString("tab", null)

        when(name){
            "Chat"->{
                replaceFragment(FragmentChatRecover())
            }

            "documents"->{
                replaceFragment(FragmentRecoverDoc())
            }

            "voice"->{
                replaceFragment(VoiceMediaFragment())
            }

            "audio"->{
                loadFragment(VoiceMediaFragment(), "Audio")
            }

            "images"->{
                loadFragment(RecoverMediaFragment(), "Image")
            }

            "video"->{
                replaceFragment(RecoverMediaFragment())
            }
            else -> {
                replaceFragment(FragmentChatRecover())
            }
        }



        position = hSharedVM.getPositionOfList(name)


     //   loadFragment(FragmentReoverMainPager())
        if (!appSharedPrefs!!.isShowHowWorkDialog) PopUpDialogHowToUseRecoverFeature(
            this@MainRecoverActivity
        )

        val notificationService =
            Intent(this@MainRecoverActivity, AppDeletedMessagesNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, notificationService)
        } else {
            startService(notificationService)
        }

        hSubscribeObservers()

    }

    private fun hSubscribeObservers() {
        hSharedVM.hSharedViewStateLD.observe(this) {

            when (it) {
                is OnUpdatePager -> {
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
            hIsLaunchUriIntent?.let { isLaunchUriIntent ->

                when (isLaunchUriIntent) {

                    true -> {

                        if (MyAppPermissionUtils.hasPermissionPost10(this@MainRecoverActivity)) {

                            Timber.d("Permission Granted")

                        } else {

                            Timber.d("permission not granted")
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


    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        hFragmentManager = null
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.contentFrame, fragment)
        fragmentTransaction.addToBackStack("fragmentRecoverMain")
        fragmentTransaction.commit()
    }

    private fun loadFragment(newFragment: Fragment?, bundleKey: String ) {
        supportActionBar?.title = resources.getString(R.string.app_name)
       /* val bundle = Bundle()
        bundle.putInt("position", position)
        newFragment?.arguments = bundle*/

        val bundle = Bundle()
       // bundle.putString("name", "Images")
        bundle.putBoolean(bundleKey, true)
        newFragment?.arguments = bundle

        hFragmentManager
            ?.beginTransaction()
            ?.add(
                R.id.contentFrame,
                newFragment!!,
                "fragmentRecoverMain"
            )
            ?.addToBackStack("fragmentRecoverMain")
            ?.commit()
    }

    private val isNotificationListenerEnable: Boolean
        get() {
            val contentResolver = contentResolver
            val enabledNotificationListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            val packageName = packageName

            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(
                packageName
            ))
        }

    private fun showNotificationListenerDialog() {
        val hDialogBuilder = AlertDialog.Builder(this)
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