package com.catchyapps.whatsdelete.appactivities.activitychat

import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.text.format.DateFormat
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.BaseActivity
import com.catchyapps.whatsdelete.appadsmanager.ShowInterstitial
import com.catchyapps.whatsdelete.appclasseshelpers.RVClickListeners
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityScreenShots
import com.catchyapps.whatsdelete.appactivities.activitychat.chatviewmodel.ChatViewModel
import com.catchyapps.whatsdelete.appactivities.activitypreview.MediaPreviewScreen
import com.catchyapps.whatsdelete.databinding.ScreenScreenChatBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ActivityChat : BaseActivity(), ActionMode.Callback {
    private var messageId: Long = 0
    private lateinit var chatScreenAdapter: AdapterChat
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    private var isLocalChat = true
    private lateinit var chatBinding: ScreenScreenChatBinding
    private val chatViewModel: ChatViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatBinding = ScreenScreenChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
        initToolbar()
        initAds()
        setupListeners()
        setupRecyclerView()
        subscribeObservers()
    }

    private fun initAds() {
        ShowInterstitial.hideNativeAndBanner(chatBinding.topAdLayout, this)

        BaseApplication.showNativeBanner(
            chatBinding.nativeContainer, chatBinding.shimmerViewContainer
        )
    }

    private fun subscribeObservers() {
        chatViewModel.messageId = messageId
        lifecycleScope.launch {
            AppHelperDb.resetUnseenCount(messageId)
        }
        lifecycleScope.launch {
            chatViewModel.hItems.collectLatest {
                chatScreenAdapter.submitData(it)
            }
        }
    }


    private fun setupRecyclerView() {
        chatScreenAdapter = AdapterChat(this)

        chatBinding.rvChildNotifications.apply {
            layoutManager = LinearLayoutManager(this@ActivityChat).also {
                it.reverseLayout = true
            }
            adapter = chatScreenAdapter
        }
    }

    private fun setupListeners() {
        chatBinding.ivBackArrow.setOnClickListener { onBackPressed() }
        chatBinding.btnscreenshot.setOnClickListener { takeScreenshot() }
        chatBinding.btndeletechat.setOnClickListener { clearChatAlertDialog() }
        chatBinding.rvChildNotifications.addOnItemTouchListener(
            RVClickListeners(
                this,
                chatBinding.rvChildNotifications,
                object :
                    RVClickListeners.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        if (isMultiSelect) {
                            multiSelect(position)
                        }
                    }

                    override fun onItemLongClick(view: View, position: Int) {
                        if (isLocalChat) if (!isMultiSelect) {
                            selectedIds = SparseArray()
                            isMultiSelect = true
                            if (actionMode == null) {
                                actionMode = startSupportActionMode(this@ActivityChat)
                            }
                        }
                        multiSelect(position)
                    }
                })
        )
    }

    private fun initToolbar() {
        setSupportActionBar(chatBinding.toolbar)
        val hExtras = intent.extras
        if (hExtras != null) {
            isLocalChat = true
            messageId = hExtras.getLong("notification_id")
            val title = hExtras.getString("title")
            val byteArray = intent.getByteArrayExtra("profile_pic")
            Glide.with(this).load(byteArray).error(R.drawable.ic_app)
                .into(chatBinding.ivUserprofile)
            chatBinding.toolbarTvName.text = title
        } else {
            finish()
            MyAppUtils.showToast(this, getString(R.string.something_went_wrong))
        }
    }

    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {
            val cw = ContextWrapper(applicationContext)
            val directory = cw.getDir("WaRecover", MODE_PRIVATE)

            // image naming and path  to include sd card  appending name you choose for file
            val file = File(directory.path)
            if (!file.isDirectory) {
                file.mkdirs()
            }

            // create bitmap screen capture
            val v1 = window.findViewById<View>(R.id.viewContainer)
            v1.isDrawingCacheEnabled = true
            val bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false
            val imageFile = File(file, "$now.jpg")
            val outputStream = FileOutputStream(imageFile)
            val quality = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            val screenShotsEntity = EntityScreenShots()
            screenShotsEntity.dateTime = System.currentTimeMillis().toString() + ""
            screenShotsEntity.path = imageFile.absolutePath
            screenShotsEntity.name = "$now.jpg"
            lifecycleScope.launch {
                AppHelperDb.saveScreenShot(screenShotsEntity)
            }
            Toast.makeText(this, getString(R.string.screenshot_saved), Toast.LENGTH_LONG).show()
            val intent = Intent(this, MediaPreviewScreen::class.java)
            intent.putExtra("file_path", imageFile.absolutePath)
            startActivity(intent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = chatScreenAdapter.hGetItem(position)
            if (data != null) {
                if (actionMode != null) {
                    if (selectedIds.indexOfKey(position) > -1) selectedIds.remove(position) else selectedIds.put(
                        position,
                        data.title
                    )
                    if (selectedIds.size() > 0) actionMode!!.title =
                        selectedIds.size()
                            .toString() + "  Selected" //show selected item count on action mode.
                    else {
                        actionMode!!.title = "" //remove item count from action mode.
                        actionMode!!.finish() //hide action mode.
                    }
                    chatScreenAdapter.setSelectedIds(selectedIds)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_clear_chat) {
            clearChatAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearChatAlertDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.are_you_sure_you_want_to_clear_all_messages_in_this_chat))
            .setPositiveButton(getString(R.string.clear_all_messages)) { _: DialogInterface?, _: Int ->
                lifecycleScope.launch(Dispatchers.Main) {
                    AppHelperDb.clearAllChatNotifications(messageId)
                    val chatEntity = AppHelperDb.getSingleChat(messageId)
                    if (chatEntity != null) {
                        AppHelperDb.updateChatRow(
                            0,
                            chatEntity.lastMessageTime!!,
                            "",
                            chatEntity.profilePic,
                            messageId
                        )
                    }
                }

            }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.cancel() }
            .create().show()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.chat_menu_select, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                deleteAlertDialog()
                return true
            }

            R.id.action_copy -> messageCopied()
            R.id.action_forward -> forwardMessages()
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        isMultiSelect = false
        selectedIds = SparseArray()
        chatScreenAdapter.setSelectedIds(SparseArray())
    }

    private fun messageCopied() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        var copiedMessage = StringBuilder()
        if (selectedIds.size() == 1) {
            copiedMessage =
                chatScreenAdapter.hGetItem(selectedIds.keyAt(0))?.body?.let { StringBuilder(it) }!!
            val clip = ClipData.newPlainText("label", copiedMessage.toString())
            clipboard.setPrimaryClip(clip)
            MyAppUtils.showToast(this@ActivityChat, getString(R.string.message_copied))
        } else {
            for (i in 0 until selectedIds.size()) {
                copiedMessage.append(chatScreenAdapter.hGetItem(selectedIds.keyAt(i))?.body)
                    .append("\n")
            }
            val clip = ClipData.newPlainText("label", copiedMessage)
            clipboard.setPrimaryClip(clip)
            MyAppUtils.showToast(
                this@ActivityChat, selectedIds.size().toString() + getString(R.string.message_copied)
            )
        }
        chatScreenAdapter.notifyDataSetChanged()
        actionMode?.title = "" //remove item count from action mode.
        actionMode?.finish()
    }

    private fun deleteAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.delete_message))
        builder.setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface?, which: Int ->
            for (i in 0 until selectedIds.size()) {
                if (chatScreenAdapter.itemCount > selectedIds.keyAt(i)) {
                    chatScreenAdapter.hGetItem(selectedIds.keyAt(i))?.id?.let {
                        lifecycleScope.launch(Dispatchers.Main) {
                            AppHelperDb.removeSingleMessage(it)
                        }
                    }
                }
            }
            chatScreenAdapter.notifyDataSetChanged()
            actionMode?.title = "" //remove item count from action mode.
            actionMode?.finish() //
        }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun forwardMessages() {
        val messages = StringBuilder()
        for (i in 0 until selectedIds.size()) {
            messages.append(chatScreenAdapter.hGetItem(selectedIds.keyAt(i))).append("\n")

        }
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, messages as CharSequence)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }
}
