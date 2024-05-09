package com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppPermissionUtils
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.appactivities.activitypremium.ActivityPremium
import com.catchyapps.whatsdelete.appactivities.activityrecover.SharedVM
import com.catchyapps.whatsdelete.appactivities.activityrecover.TypesIntent
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice.AudioMediaAdapter.CallBack
import com.catchyapps.whatsdelete.appactivities.activitysetting.SettingsScreen
import com.catchyapps.whatsdelete.databinding.AudioMediaFragmentBinding
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit


class VoiceMediaFragment : Fragment(), CallBack, ActionMode.Callback {

    private var recyclerViewAdapter: AudioMediaAdapter? = null
    private var objectList = mutableListOf<EntityFiles>()
    private var tempList = mutableListOf<EntityFiles>()
    private val isAudio = true
    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying = false
    private var audioLengthInSec = 0
    private var isPlaying = false
    private var handler: Handler? = null
    private var actionMode: ActionMode? = null
    private var selectedIds = SparseArray<String>()

    private lateinit var fragmentAudioBinding: AudioMediaFragmentBinding
    private var fragmentVoiceViewModel: VoiceMediaVMFragment? = null


    private var totalItemCount = 0
    private var lastVisibleItem = 0
    private var loading = true
    private var pageNumber = 1
    private var layoutManager: LinearLayoutManager? = null
    private val sharedVM by activityViewModels<SharedVM>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        hInitVariables()
    }

    private fun hInitVariables() {
        mediaPlayer = MediaPlayer()
        objectList = ArrayList()
        handler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        fragmentAudioBinding = AudioMediaFragmentBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return fragmentAudioBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentVoiceViewModel = ViewModelProvider(this)[VoiceMediaVMFragment::class.java]
        setUpRecyclerView()
        fragmentAudioBinding.playButton.setOnClickListener { playPauseAudio() }
        hSubscribeObservers()
        hSetUpLoadMoreListener()
        if (checkPermission()) fragmentVoiceViewModel!!.hSetFragmentType(arguments)
    }

    private fun hSetUpLoadMoreListener() {
        fragmentAudioBinding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int, dy: Int,
            ) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = layoutManager?.itemCount!!
                lastVisibleItem = layoutManager!!
                    .findLastCompletelyVisibleItemPosition()
                if (!loading && lastVisibleItem == totalItemCount - 1) {
                    pageNumber++
                    loading = true
                    fragmentVoiceViewModel!!.hLoadMoreItems(pageNumber)
                }
            }
        })
    }

    private fun hSubscribeObservers() {
        fragmentVoiceViewModel?.hAudioListLD?.observe(viewLifecycleOwner) { audioList ->
            Timber.d("Audio Data Retrieved and Thread is ${Thread.currentThread().name}")
            audioList?.toList()?.let { hSetViewsData(it) }
        }
        fragmentVoiceViewModel?.hVoiceListLD?.observe(viewLifecycleOwner) { voiceList ->
            Timber.d("Voice Data Retrieved and Thread is ${Thread.currentThread().name}")
            voiceList?.toList()?.let { hSetViewsData(it) }
        }
    }

    private fun hSetViewsData(list: List<EntityFiles>) {
        loading = false
        if (list.isNotEmpty()) {
            tempList = list.toMutableList()
            fragmentAudioBinding.hProgressbar.visibility = View.GONE
            fragmentAudioBinding.recyclerView.visibility = View.VISIBLE
            fragmentAudioBinding.tvNoAudioVoice.visibility = View.GONE
            recyclerViewAdapter!!.hAddItems(list.reversed())
            val fileEntities = recyclerViewAdapter!!.hGetLists()
            Timber.d("List size  %s", fileEntities.size)
            objectList = fileEntities as MutableList<EntityFiles>
        } else {
            if (isAudio) {
                fragmentAudioBinding.hProgressbar.visibility = View.GONE
                fragmentAudioBinding.tvNoAudioVoice.text =
                    requireActivity().getString(R.string.no_audio_detected_yet)
            } else {
                fragmentAudioBinding.tvNoAudioVoice.text =
                    requireActivity().getString(R.string.no_voice_detected_yet)
            }
            fragmentAudioBinding.tvNoAudioVoice.visibility = View.VISIBLE
            fragmentAudioBinding.hProgressbar.visibility = View.GONE
            fragmentAudioBinding.tvNoAudioVoice
                .setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    R.drawable.nofiles_img,
                    0,
                    0
                )
            fragmentAudioBinding.recyclerView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermission()) {
            if (tempList.isNotEmpty()) {
                fragmentAudioBinding.hProgressbar.visibility = View.GONE
            }
            fragmentAudioBinding.recyclerView.visibility = View.VISIBLE
            fragmentAudioBinding.permissionDialoge.visibility = View.GONE
        }
    }

    private fun checkPermission(): Boolean {
        val currentAPIVersion = Build.VERSION.SDK_INT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (MyAppPermissionUtils.hasPermissionPost10(requireContext())) {

                true

            } else {

                Timber.d("Permission not Granted")
                fragmentAudioBinding.hProgressbar.visibility = View.GONE
                fragmentAudioBinding.recyclerView.visibility = View.GONE
                fragmentAudioBinding.permissionDialoge.visibility = View.VISIBLE
                fragmentAudioBinding.btnPositive.setOnClickListener {
                    sharedVM.hLaunchIntent(TypesIntent.H_URI)
                }

                false

            }

        } else if (currentAPIVersion >= Build.VERSION_CODES.M && currentAPIVersion <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    Objects.requireNonNull(requireContext()),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        Objects.requireNonNull(requireActivity()),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    val alertBuilder = AlertDialog.Builder(requireActivity())
                    alertBuilder.setCancelable(true)
                    alertBuilder.setTitle("Permission necessary")
                    alertBuilder.setMessage("Storage permission is necessary to Download Images and Videos!!!")
                    alertBuilder.setPositiveButton(
                        R.string.ok
                    ) { _: DialogInterface?, _: Int ->
                        ActivityCompat.requestPermissions(
                            Objects.requireNonNull(requireActivity()),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                        )
                    }
                    val alert = alertBuilder.create()
                    alert.show()
                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
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

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragmentVoiceViewModel!!.hSetFragmentType(arguments)
            } else {
                checkAgain()
            }
        }
    }

    private fun checkAgain() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                Objects.requireNonNull(requireActivity()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            val alertBuilder = AlertDialog.Builder(requireActivity())
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle("Permission necessary")
            alertBuilder.setMessage("Write Storage permission is necessary to Download Images and Videos!!!")
            alertBuilder.setPositiveButton(
                R.string.yes
            ) { _: DialogInterface?, _: Int ->
                ActivityCompat.requestPermissions(
                    Objects.requireNonNull(requireActivity()),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_STORAGE
                )
            }
            val alert = alertBuilder.create()
            alert.show()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_STORAGE
            )
        }
    }

    private fun setUpRecyclerView() {
        fragmentAudioBinding.recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(requireActivity())
        fragmentAudioBinding.recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = AudioMediaAdapter(requireActivity(), objectList, this, requireActivity())
        fragmentAudioBinding.recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun playAudio(itemPos: Int) {
        fragmentAudioBinding.playercontainer.visibility = View.VISIBLE
        val data = objectList[itemPos]
        fragmentAudioBinding.songName.text = data.title
        fragmentAudioBinding.animationView.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.round_circle
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            fragmentAudioBinding.totalTime.text =
                " /  ${data.fileUri?.let { getDurationAbove10(it) }}"

        } else {

            fragmentAudioBinding.totalTime.text = " /  ${data.filePath?.let { getDuration(it) }}"

        }

        try {
            if (mediaPlayer!!.isPlaying) {
                fragmentAudioBinding.animationView.playAnimation()
                pauseAudio()
                isAudioPlaying = false
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            mediaPlayer = MediaPlayer()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                audioLengthInSec = data.fileUri?.let { getLengthPost10(it) }!!
                mediaPlayer!!.reset()
                context?.contentResolver?.openFileDescriptor(data.fileUri!!.toUri(), "r")?.use {
                    mediaPlayer!!.setDataSource(it.fileDescriptor)
                }

            } else {
                data.filePath?.let { getLength(it) }!!
                val tempFile = File(data.filePath)
                val fis = FileInputStream(tempFile)
                mediaPlayer!!.reset()
                mediaPlayer!!.setDataSource(fis.fd)

            }
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnPreparedListener { mediaPlayers: MediaPlayer ->
                mediaPlayer = mediaPlayers
                mediaPlayer!!.seekTo(0)
                fragmentAudioBinding.realseekBar.max = audioLengthInSec
                fragmentAudioBinding.realseekBar.progress = 0
                isAudioPlaying = true
                playPauseAudio()
            }
            mediaPlayer!!.setOnCompletionListener { mediaPlayers ->
                mediaPlayer!!.seekTo(0)
                fragmentAudioBinding.realseekBar.progress = 0
                isPlaying = false
                fragmentAudioBinding.runTime.text = "00:00"
                fragmentAudioBinding.playButton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.icon_primary_play
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireActivity(), "Error in play audio" + e.message, Toast.LENGTH_SHORT)
                .show()
        }
        updateCurrentDuration()
        fragmentAudioBinding.runTime.text = "00:00"
        fragmentAudioBinding.realseekBar.progress = 0
        fragmentAudioBinding.realseekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress * 1000)
                    fragmentAudioBinding.runTime.text =
                        getCurrentDuration((progress * 1000).toLong())
                }
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun playPauseAudio() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer!!.pause()
                fragmentAudioBinding.animationView.pauseAnimation()
                fragmentAudioBinding.playButton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.icon_primary_play
                )
                isPlaying = false
            } else {
                mediaPlayer!!.start()
                fragmentAudioBinding.animationView.playAnimation()
                fragmentAudioBinding.playButton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.primary_pause_icon
                )
                isPlaying = true
                updateCurrentDuration()
            }
        }
    }

    private fun getLengthPost10(path: String): Int? {

        path.let {
            context?.contentResolver?.openFileDescriptor(it.toUri(), "r")?.use {
                MediaMetadataRetriever().apply {
                    setDataSource(it.fileDescriptor)
                    val hDurationLong =
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    return hDurationLong?.let { it1 ->
                        TimeUnit.MILLISECONDS.toSeconds(it1).toInt()
                    }
                }
            }
        }
        return null
    }

    private fun getLength(path: String) {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillisec = time!!.toLong()

        audioLengthInSec = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec).toInt()
    }

    private fun getDuration(audioFilePath: String): String? {
        var hTimeinMillsec: Long = 1
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(audioFilePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (time != null) {
                hTimeinMillsec = time.toLong()
            }
            getCurrentDuration(hTimeinMillsec)
        } catch (e: Exception) {
            return ""
        }
    }

    fun getDurationAbove10(audioFilePath: String): String? {

        audioFilePath.let {
            context?.contentResolver?.openFileDescriptor(it.toUri(), "r")?.use {
                MediaMetadataRetriever().apply {
                    setDataSource(it.fileDescriptor)
                    val hDurationLong =
                        extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
                    if (hDurationLong != null) {
                        return@let getCurrentDuration(hDurationLong)
                    }
                }
            }
        }

        return null
    }

    private fun updateCurrentDuration() {
        val updateVideoDuration: Runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    if (isAudioPlaying) {
                        if (mediaPlayer!!.isPlaying) {
                            fragmentAudioBinding.runTime.text =
                                getCurrentDuration(mediaPlayer!!.currentPosition.toLong())
                            handler!!.postDelayed(this, 1000)
                            fragmentAudioBinding.realseekBar.progress =
                                mediaPlayer!!.currentPosition / 1000
                        }
                    }
                }
            }
        }
        handler!!.postDelayed(updateVideoDuration, 0)
    }

    fun getCurrentDuration(timeInMillisecond: Long): String {
        val hours: String
        val minutes: String
        val seconds: String
        val duration = timeInMillisecond / 1000
        val hrs = duration / 3600
        val min = (duration - hrs * 3600) / 60
        val sec = duration - (hrs * 3600 + min * 60)
        hours = when {
            hrs < 1 -> {
                ""
            }

            hrs > 9 -> {
                "$hrs:"
            }

            else -> {
                "0$hrs:"
            }
        }
        minutes = when {
            min < 1 -> {
                "00:"
            }

            min > 9 -> {
                "$min:"
            }

            else -> {
                "0$min:"
            }
        }
        seconds = when {
            sec < 1 -> {
                "00"
            }

            sec > 9 -> {
                sec.toString()
            }

            else -> {
                "0$sec"
            }
        }
        return hours + "" + minutes + "" + seconds
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun pauseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.pause()
            isPlaying = false
            fragmentAudioBinding.animationView.pauseAnimation()
            fragmentAudioBinding.playButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.icon_primary_play
            )
        }
    }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = recyclerViewAdapter!!.getItem(position)
            if (data != null) {
                if (actionMode != null) {
                    if (selectedIds.indexOfKey(position) > -1) selectedIds.remove(position) else selectedIds.put(
                        position,
                        data.title
                    )
                    if (selectedIds.size() > 0) actionMode!!.title =
                        selectedIds.size()
                            .toString() + getString(R.string.items_selected) //show selected item count on action mode.
                    else {
                        actionMode?.title = "" //remove item count from action mode.
                        actionMode?.finish() //hide action mode.
                    }
                    recyclerViewAdapter?.setSelectedIds(selectedIds)
                }
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.file_menu_select_fragment, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            alertDeleteConfirmation()
            return true
        } else if (item.itemId == R.id.action_share) {
            shareMultipleFiles()
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        val isMultiSelect = false
        selectedIds = SparseArray()
        recyclerViewAdapter!!.setSelectedIds(selectedIds)
    }

    private fun alertDeleteConfirmation() {
        val builder = android.app.AlertDialog.Builder(requireActivity())
        val imageVideo = if (isAudio) "Audio" else "Voice"
        if (selectedIds.size() > 1) builder.setMessage(getString(R.string.delete) + selectedIds.size() +  getString(R.string.items_selected) + imageVideo + "?")
        else builder.setMessage(
            "${getString(R.string.delete)} $imageVideo?"
        )
        builder.setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface, which: Int ->
            try {
                for (i in 0 until selectedIds.size()) {
                    val fileEntity = objectList[selectedIds.keyAt(i)]
                    val fDelete = File(fileEntity.filePath!!)
                    if (fDelete.exists()) {
                        val delete = fDelete.delete()
                        if (delete) {

                            objectList.removeAt(selectedIds.keyAt(i))

                        }
                    }
                }
                if (objectList.size > 0) {
                    recyclerViewAdapter?.notifyDataSetChanged()
                } else {
                    fragmentAudioBinding.recyclerView.visibility = View.GONE
                    fragmentAudioBinding.tvNoAudioVoice.visibility = View.VISIBLE
                    fragmentAudioBinding.hProgressbar.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.cancel()
            actionMode?.title = "" //remove item count from action mode.
            actionMode?.finish() //
        }.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun shareMultipleFiles() {
        try {
            val selectedUri = ArrayList<Uri>()
            for (i in 0 until selectedIds.size()) {
                val uri = objectList[selectedIds.keyAt(i)].filePath
                val u = FileProvider.getUriForFile(
                    requireContext(),
                    requireActivity().packageName + ".provider",
                    File(uri)
                )
                selectedUri.add(u)
            }
            actionMode?.title = "" //remove item count from action mode.
            actionMode?.finish()
            shareMultiple(selectedUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultiple(files: ArrayList<Uri>?) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, getString(R.string.share)))
    }

    override fun onPause() {
        super.onPause()
        Timber.d("fragment onPause")
        hReleaseAudio()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    fun hReleaseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.pause()
            isPlaying = false
            fragmentAudioBinding.animationView.pauseAnimation()
            fragmentAudioBinding.playButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.icon_primary_play
            )
        }
    }

    fun hExecuteSearch(newText: String?) {
        recyclerViewAdapter!!.filter.filter(newText)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main, menu)
        val mSearch = menu.findItem(R.id.actionSearch)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = "Search"
        mSearchView.isSubmitButtonEnabled = false

        @SuppressLint("CutPasteId")
        val txtSearch = mSearchView.findViewById<EditText>(R.id.search_src_text)

        txtSearch.setTextColor(Color.WHITE)

        @SuppressLint("CutPasteId")
        val searchTextView = mSearchView.findViewById<AutoCompleteTextView>(R.id.search_src_text)
        try {
            TextView::class.java.declaredFields.find {
                it.name.equals("mCursorDrawableRes")
            }?.let {
                it.isAccessible = true
                it[searchTextView] = R.drawable.icon_cursor_png
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                hExecuteSearch(newText)
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

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(requireContext(), SettingsScreen::class.java))
                return false
            }

            else -> {
            }
        }
        return false
    }

    private fun showPremiumDialog() {
        val intent = Intent(requireContext(), ActivityPremium::class.java)
        startActivity(intent)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 123
    }
}

