package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recoverchat

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.SparseArray
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.RVClickListeners
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activitychat.ActivityChat
import com.catchyapps.whatsdelete.appactivities.activityrecover.TypesIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.appactivities.activityrecover.SharedVM
import com.catchyapps.whatsdelete.appactivities.activitysetting.SettingsScreen
import com.catchyapps.whatsdelete.databinding.ChatFragmentScreenBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*


class FragmentChatRecover : Fragment(), ActionMode.Callback {
    private val hChatViewModel: ModelRecoverChatView by viewModels()
    private lateinit var hChatAdapter: RecoverChatPagingAdapter
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    private lateinit var hFragmentChatBinding: ChatFragmentScreenBinding
    private val hSharedVM by activityViewModels<SharedVM>()

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        hFragmentChatBinding = ChatFragmentScreenBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return hFragmentChatBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hFragmentChatBinding.animationView.playAnimation()
        hInitAdapter()
        hSetupListeners()
        if (isNotificationListenerEnable) {
            stopAnimation()
            hFragmentChatBinding.layoutNoPermission.visibility = View.GONE
        }
        hSubscribeObservers()

    }

    private fun hSubscribeObservers() {
        lifecycleScope.launch {
            hChatViewModel.hItems.collectLatest {
                hChatAdapter.submitData(it)
            }
        }

        hChatViewModel.hSearchedChatList.observe(viewLifecycleOwner) {
            hChatViewModel.hCheckForRealList(hChatAdapter.snapshot().items)
            lifecycleScope.launch {
                hChatAdapter.submitData(PagingData.from(it))
            }
        }
        hChatViewModel.hIsRestoreListLD.observe(viewLifecycleOwner) {
            if (it) {
                lifecycleScope.launch {
                    hChatViewModel.hGetOrignalList()?.let { it1 -> hChatAdapter.submitData(it1) }
                }
            }
        }
    }


    private fun hSetupViews() {
        if (hChatAdapter.snapshot().items.isNotEmpty()) {
            hFragmentChatBinding.tvNoChat.visibility = View.GONE
            hFragmentChatBinding.rvNotification.visibility = View.VISIBLE
        } else {
            hFragmentChatBinding.rvNotification.visibility = View.GONE
            if (hFragmentChatBinding.layoutNoPermission.visibility != View.VISIBLE) {
                hFragmentChatBinding.tvNoChat.visibility = View.VISIBLE
            }
        }
    }


    private fun hSetupListeners() {
        hFragmentChatBinding.tvEnableNotification.setOnClickListener { view: View? ->
            startActivityForResult(
                Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                ), 10001
            )
        }


        hFragmentChatBinding.rvNotification.addOnItemTouchListener(
            RVClickListeners(
                requireContext(),
                hFragmentChatBinding.rvNotification,
                object :
                    RVClickListeners.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        if (isMultiSelect) {
                            multiSelect(position)
                        } else {
                            Intent(
                                requireContext(),
                                ActivityChat::class.java
                            ).also { intent ->
                                hChatAdapter.snapshot().items[position].apply {
                                    intent.putExtra("notification_id", this.id)
                                    intent.putExtra("title", this.title)
                                    intent.putExtra("profile_pic", this.profilePic)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                }
                            }.apply {
                                requireContext().startActivity(this)
                            }
                        }
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        if (!isMultiSelect) {
                            selectedIds = SparseArray()
                            isMultiSelect = true
                            if (actionMode == null) {
                                try {
                                    //show ActionMode.
                                    actionMode =
                                        (Objects.requireNonNull(requireActivity()) as MainRecoverActivity)
                                            .startSupportActionMode(this@FragmentChatRecover) //show ActionMode.
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        multiSelect(position)
                    }
                }
            )
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (isNotificationListenerEnable) {
            stopAnimation()
            hInitAdapter()
            hFragmentChatBinding.layoutNoPermission.visibility = View.GONE
        }
    }

    private fun stopAnimation() {
        hFragmentChatBinding.tvEnableNotification.visibility = View.GONE
        hFragmentChatBinding.layoutNoPermission.visibility = View.GONE
        hFragmentChatBinding.animationView.visibility = View.GONE
        hFragmentChatBinding.animationView.pauseAnimation()
        hFragmentChatBinding.animationView.clearAnimation()
    }


    private fun hInitAdapter() {
        hChatAdapter = RecoverChatPagingAdapter(requireContext())
        hChatAdapter.addLoadStateListener {
            hSetupViews()
        }

        hFragmentChatBinding.rvNotification.apply {
            visibility = View.VISIBLE
            layoutManager = LinearLayoutManager(context)
            adapter = hChatAdapter
        }
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = hChatAdapter.hGetItem(position)
            if (data != null) {
                if (actionMode != null) {
                    if (selectedIds.indexOfKey(position) > -1)
                        selectedIds.remove(position)
                    else selectedIds.put(position, data.title)
                    if (selectedIds.size() > 0) actionMode!!.title =
                        selectedIds.size()
                            .toString() + getString(R.string.items_selected) //show selected item count on action mode.
                    else {
                        actionMode?.title = "" //remove item count from action mode.
                        actionMode?.finish() //hide action mode.
                    }
                    hChatAdapter.setSelectedIds(selectedIds)
                }
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mActionMode = mode
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.chat_menu_select, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    @SuppressLint("NonConstantResourceId")
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                alertDeleteChat()
                return true
            }

        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        mActionMode = null
        isMultiSelect = false
        selectedIds = SparseArray()
        hChatAdapter.setSelectedIds(selectedIds)
    }

    private fun alertDeleteChat() {
        val builder = AlertDialog.Builder(Objects.requireNonNull(requireActivity()))
        if (selectedIds.size() > 1) {
            builder.setMessage(getString(R.string.delete)  + selectedIds.size() + getString(R.string.selected_chats))
        } else {
            builder.setMessage(getString(R.string.delete_selected_chat))
        }
        builder.setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface, which: Int ->
            lifecycleScope.launch {
                for (i in 0 until selectedIds.size()) {
                    hChatViewModel.deleteSingleChat(selectedIds.valueAt(i))
                }
                actionMode?.title = ""
                actionMode?.finish()
                hChatAdapter.notifyDataSetChanged()
                dialog.cancel()
            }
        }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }


    suspend fun hExecuteSearch(newText: String?) {
        hChatViewModel.hExecuteSearch(newText)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)

        val mSearch = menu.findItem(R.id.actionSearch)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = getString(R.string.search)
        mSearchView.isSubmitButtonEnabled = false
        val txtSearch = mSearchView.findViewById<EditText>(R.id.search_src_text)
        txtSearch.setTextColor(Color.WHITE)
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                lifecycleScope.launch {
                    hExecuteSearch(newText)
                }
                return true
            }
        })

        val viewGift = menu.findItem(R.id.action_prem)
        val view = viewGift.actionView
        view?.setOnClickListener {
            showPremiumDialog()
        }

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun showPremiumDialog() {
        val intent = Intent(requireContext(), ActivityPremium::class.java)
        startActivity(intent)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
               startActivity(Intent(requireContext(),SettingsScreen::class.java))
                return false
            }

        }
        return false
    }

    companion object {
        var mActionMode: ActionMode? = null
    }
}
