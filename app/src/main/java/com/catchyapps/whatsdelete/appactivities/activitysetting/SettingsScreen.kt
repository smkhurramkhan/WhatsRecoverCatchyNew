package com.catchyapps.whatsdelete.appactivities.activitysetting

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.ItemsDecorateClass
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils.Companion.showToast
import com.catchyapps.whatsdelete.appclasseshelpers.PopUpDialogHowToUseRecoverFeature
import com.catchyapps.whatsdelete.basicapputils.MyAppShareModel
import com.catchyapps.whatsdelete.databinding.ScreenSettingsBinding
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener
import kotlinx.coroutines.launch

class SettingsScreen : BaseActivity(),
    CompoundButton.OnCheckedChangeListener {

    private lateinit var activitySettingBinding: ScreenSettingsBinding

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private var hAppSharedPrefs: MyAppSharedPrefs? = null
    private var hDragDropList: MutableList<MyAppShareModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySettingBinding = ScreenSettingsBinding.inflate(layoutInflater)
        setContentView(activitySettingBinding.root)

        initToolbar()
        initAds()
        initVar()
        setupAdapter()
        setupListeners()


    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(activitySettingBinding.topAdLayout, this)
        ShowInterstitial.hideNativeAndBanner(activitySettingBinding.nativeAdContainer, this)

        BaseApplication.showNativeBannerAdmobOnly(
            activitySettingBinding.nativeContainer,
            activitySettingBinding.shimmerViewContainer
        )

    }


    private fun initVar() {
        hAppSharedPrefs = MyAppSharedPrefs(this)
        hDragDropList = hAppSharedPrefs?.hGetPagerList(this)?.toMutableList()
    }

    private fun initToolbar() {
        activitySettingBinding.toolbar.apply {
            title = getString(R.string.action_settings)
            setSupportActionBar(this)
        }
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupListeners() {
        activitySettingBinding.apply {
            switchImage.apply {
                isChecked = hAppSharedPrefs?.isSaveImage == true
                setOnCheckedChangeListener(this@SettingsScreen)
            }
            switchVideo.apply {
                isChecked = hAppSharedPrefs?.isSaveVideo == true
                setOnCheckedChangeListener(this@SettingsScreen)
            }
            switchVoice.apply {
                isChecked = hAppSharedPrefs?.isSaveVoiceNotes == true
                setOnCheckedChangeListener(this@SettingsScreen)
            }
            switchAudio.apply {
                isChecked = hAppSharedPrefs?.isSaveAudio == true
                setOnCheckedChangeListener(this@SettingsScreen)
            }
            switchDocument.apply {
                isChecked = hAppSharedPrefs?.isSaveDocument == true
                setOnCheckedChangeListener(this@SettingsScreen)
            }
            btnErase.setOnClickListener {
                alertDeleteAllChat()
            }
            btnApply.setOnClickListener {
                hDragDropList?.let {
                    hAppSharedPrefs?.hSavePagerList(it)
                }
                showToast(this@SettingsScreen, getString(R.string.setting_applied))
            }
        }
    }

    private fun setupAdapter() {
        activitySettingBinding.hDragDropRv.apply {
            layoutManager = LinearLayoutManager(this@SettingsScreen)
            addItemDecoration(
                ItemsDecorateClass(
                    this@SettingsScreen
                )
            )
            dragListener = object : OnItemDragListener<MyAppShareModel> {
                override fun onItemDragged(
                    previousPosition: Int,
                    newPosition: Int,
                    item: MyAppShareModel
                ) {
                }

                override fun onItemDropped(
                    initialPosition: Int,
                    finalPosition: Int,
                    item: MyAppShareModel
                ) {
                    hUpdateList(initialPosition, finalPosition)
                }

            }
            adapter =
                hAppSharedPrefs?.hGetPagerList(this@SettingsScreen)?.let { SettingAdapter(it) }
        }
    }

    private fun hUpdateList(initialPostion: Int, finalPosition: Int) {
        hDragDropList?.let {
            val hItemMoved = it[initialPostion]
            val hItemToReplaceWith = it[finalPosition]
            it[initialPostion] = hItemToReplaceWith
            it[finalPosition] = hItemMoved
        }
    }

    private fun alertDeleteAllChat() {
        val builder = AlertDialog.Builder(this@SettingsScreen)
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_status_all_chat_history))
        builder.setPositiveButton(getString(R.string.yes)) { _: DialogInterface?, _: Int ->
            lifecycleScope.launch {
                AppHelperDb.hClearAllChat()
                AppHelperDb.hClearAlMessages()
            }
            showToast(this@SettingsScreen, getString(R.string.all_chat_cleared))
        }
            .setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_info) {
            PopUpDialogHowToUseRecoverFeature(
                this@SettingsScreen
            )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when {
            buttonView == activitySettingBinding.switchImage -> {
                hAppSharedPrefs?.isSaveImage = isChecked
            }

            buttonView == activitySettingBinding.switchVideo -> {
                hAppSharedPrefs?.isSaveVideo = isChecked
            }

            buttonView == activitySettingBinding.switchVoice -> {
                hAppSharedPrefs?.isSaveVoiceNotes = isChecked
            }

            buttonView == activitySettingBinding.switchAudio -> {
                hAppSharedPrefs?.isSaveAudio = isChecked
            }

            buttonView == activitySettingBinding.switchDocument -> {
                hAppSharedPrefs?.isSaveDocument = isChecked
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }
}