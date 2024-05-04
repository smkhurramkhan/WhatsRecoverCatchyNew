package com.catchyapps.whatsdelete.appactivities.activityrecover

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.PopUpDialogHowToUseRecoverFeature
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.FragmentReoverMainPager
import com.catchyapps.whatsdelete.appnotifications.AppDeletedMessagesNotificationService
import com.catchyapps.whatsdelete.databinding.ScreenMainBinding

class MainRecoverActivity : com.catchyapps.whatsdelete.appactivities.BaseActivity() {

    private var appSharedPrefs: MyAppSharedPrefs? = null
    private var hFragmentManager: FragmentManager? = null
    private lateinit var hActivityMainBinding: ScreenMainBinding
    private var position = 0
    private var name: String? = null
    private val viewModel by viewModels<SharedVM>()
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hActivityMainBinding = ScreenMainBinding.inflate(layoutInflater)
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

        position = viewModel.getPositionOfList(name)


        loadFragment(FragmentReoverMainPager())
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

    }


    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        hFragmentManager = null
    }


    private fun loadFragment(newFragment: Fragment?) {
        supportActionBar?.title = resources.getString(R.string.app_name)
        val bundle = Bundle()
        bundle.putInt("position", position)
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

}