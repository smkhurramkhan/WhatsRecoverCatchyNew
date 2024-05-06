package com.catchyapps.whatsdelete.appactivities.activityonboarding

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.databinding.LayoutHintStartBinding
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appactivities.activityhome.MainActivity
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.uri
import com.catchyapps.whatsdelete.databinding.ScreensOnboardingBinding
import com.rd.animation.type.AnimationType

class OnboardingScreens : BaseActivity() {
    private var viewPagerAdapter: ViewPagerAdapter? = null
    private var prefs: MyAppSharedPrefs? = null
    private var selectedPosition = 0
    private var notificationListenerDialog: AlertDialog? = null
    private lateinit var binding: ScreensOnboardingBinding
    private var viewPager: ViewPager? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {


            val mContentResolver: ContentResolver = this.contentResolver


            val directoryUri = result.data?.data ?: return@registerForActivityResult
            mContentResolver.takePersistableUriPermission(
                directoryUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            viewPager?.currentItem = 2

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScreensOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        prefs = MyAppSharedPrefs(this)
        viewPagerAdapter = ViewPagerAdapter()

        viewPager = findViewById(R.id.viewPager_startup)
        viewPager?.adapter = viewPagerAdapter


        binding.pageIndicatorView.count = 4 // specify total count of indicators
        binding.pageIndicatorView.setAnimationType(AnimationType.THIN_WORM)
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.pageIndicatorView.selection = position
                selectedPosition = position
                if (position == 2) binding.btnNext.setText(R.string.accept) else binding.btnNext.setText(
                    R.string.next
                )
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.btnNext.setOnClickListener {
            if (selectedPosition == 0) {
                viewPager?.currentItem = 1
            } else if (selectedPosition == 1) {

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {

                    if (ContextCompat.checkSelfPermission(
                            this@OnboardingScreens,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        showPermissionDialog()

                    } else viewPager?.currentItem = 2

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    if (!MyAppPermissionUtils.hasPermissionPost10(this)) {

                        MyAppPermissionUtils.hShowUriPermissionDialog(
                            context = this,
                            description = CleanerConstans.hGetDialogText(
                                MyAppSchemas.H_DOCUMENT_TYPE,
                                this
                            ),
                        ) {
                            storagePermissionLauncher.launch(
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

                    }else viewPager?.currentItem = 2

                } else viewPager?.currentItem = 2

            } else {
                if (!isNotificationListenerEnable) {
                    showNotificationListenerDialog()
                } else {
                    prefs?.hSetFirstTime(false)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }

        binding.btnPrevious.setOnClickListener {
            if (viewPager != null && viewPager?.currentItem!! > 0) {
                viewPager?.currentItem = viewPager!!.currentItem - 1
            } else {
                Toast.makeText(this, "You are at the first page", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showNotificationListenerDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.notification_listener_service))
            .setMessage(R.string.we_need_your_permission_desc)
        builder.setPositiveButton(getString(R.string.goto_settings)) { _: DialogInterface?,
                                                                       _: Int ->
            startActivityForResult(
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                NOTIFICATION_LISTENER_PERMISSION_CODE
            )
        }
        builder.setNegativeButton(getString(R.string.not_now)) { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
            prefs?.hSetFirstTime(false)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("is_new_user", true)
            startActivity(intent)
            finish()
        }
        notificationListenerDialog = builder.create()
        notificationListenerDialog?.setCanceledOnTouchOutside(false)
        notificationListenerDialog?.show()
    }

    private val isNotificationListenerEnable: Boolean
        get() {
            val contentResolver = this.contentResolver
            val enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val packageName = this.packageName

            return !(enabledNotificationListeners == null || !enabledNotificationListeners.contains(
                packageName
            ))
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isNotificationListenerEnable) {
            if (notificationListenerDialog != null) {
                notificationListenerDialog!!.dismiss()
                notificationListenerDialog!!.cancel()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    prefs?.hSetFirstTime(false)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("is_new_user", true)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showPermissionDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@OnboardingScreens,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val alertBuilder = AlertDialog.Builder(this@OnboardingScreens)
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle(getString(R.string.permission_necessary))
            alertBuilder.setMessage(getString(R.string.storage_permission_is_necessary_to_status_media_files))
            alertBuilder.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface?, which: Int ->
                ActivityCompat.requestPermissions(
                    this@OnboardingScreens, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                )
            }
            val alert = alertBuilder.create()
            alert.show()
        } else {
            ActivityCompat.requestPermissions(
                this@OnboardingScreens,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewPager?.currentItem = 2
            }
        }
    }

    inner class ViewPagerAdapter : PagerAdapter() {
        private var itemBinding: LayoutHintStartBinding? = null
        var resources = intArrayOf(
            R.drawable.icon_by_using_this_app_svg,
            R.drawable.icon_we_care_about_privacy_svg,
            R.drawable.icon_by_click_accipting_svg
        )
        private var titles = getResources().getStringArray(R.array.startup_titles)
        private var descriptions = getResources().getStringArray(R.array.startup_descriptions)
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            itemBinding = LayoutHintStartBinding.inflate(layoutInflater, null, false)
            container.addView(itemBinding?.root)
            itemBinding?.tvTitle?.text = titles[position]
            itemBinding?.ivStartup?.setImageResource(resources[position])
            return itemBinding!!.root
        }

        override fun getCount(): Int {
            return resources.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 10001
        private const val NOTIFICATION_LISTENER_PERMISSION_CODE = 10002
    }
}