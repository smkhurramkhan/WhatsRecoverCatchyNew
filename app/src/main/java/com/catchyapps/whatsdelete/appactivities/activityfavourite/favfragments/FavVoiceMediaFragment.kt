package com.catchyapps.whatsdelete.appactivities.activityfavourite.favfragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.getMimeType
import com.catchyapps.whatsdelete.appactivities.activityfavourite.ActivityFavorite
import com.catchyapps.whatsdelete.appactivities.activityfavourite.favadapters.FavoriteVoiceAdapter
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.databinding.VoicesFavouriteBinding
import org.apache.commons.io.comparator.LastModifiedFileComparator
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit


@SuppressLint("NotifyDataSetChanged")
class FavVoiceMediaFragment : Fragment(), ActionMode.Callback {
    private var adapterFavMedia: FavoriteVoiceAdapter? = null
    private lateinit var objectList: MutableList<EntityFiles>
    private var actionMode: ActionMode? = null
    private var isMultiSelect = false
    private var selectedIds = SparseArray<String>()
    var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying = false
    private var audioLengthInSec = 0
    private var isPlaying = false
    private var handler: Handler? = null
    var binding: VoicesFavouriteBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = VoicesFavouriteBinding.inflate(inflater, container, false)
        objectList = ArrayList()
        mediaPlayer = MediaPlayer()
        handler = Handler(Looper.getMainLooper())
        binding!!.playButton.setOnClickListener { playPauseAudio() }
        data
        setUpRecyclerView()


        return binding!!.root
    }

    private fun setUpRecyclerView() {
        binding!!.recyclerView.setHasFixedSize(true)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(activity)
        adapterFavMedia = FavoriteVoiceAdapter(objectList,
            requireContext(),
            onClick = { position ->
                if (isMultiSelect) {
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position)
                } else {
                    if (position < objectList.size) {
                        try {
                            playAudio(position)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            onLongClick = { position, view ->
                val ivoptions = view.findViewById<ImageView>(R.id.ivMore)
                if (!isMultiSelect) {
                    selectedIds = SparseArray()
                    isMultiSelect = true
                    if (actionMode == null) {
                        try {
                            ivoptions.visibility = View.GONE
                            actionMode =
                                (requireActivity() as ActivityFavorite).startSupportActionMode(
                                    this@FavVoiceMediaFragment
                                ) //show ActionMode. //show ActionMode.
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                multiSelect(position)
            }
        )
        binding!!.recyclerView.adapter = adapterFavMedia
    }

    private val data: Unit
        get() {
            objectList.clear()
            var recoverFilesEntity: EntityFiles
            val targetPath: String
            val cw = ContextWrapper(requireContext())
            val directory = cw.getDir(MyAppConstants.ROOT_FOLDER, Context.MODE_PRIVATE)
            val dir = File(directory, MyAppConstants.WA_FAV_VOICE)
            targetPath = dir.path
            binding!!.tvNoAudioVoice.setCompoundDrawablesWithIntrinsicBounds(
                0,
                R.drawable.nofiles_img,
                0,
                0
            )
            val targetDirector = File(targetPath)
            val files = targetDirector.listFiles()
            try {
                if (files != null) {
                    Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE)
                    for (file in files) {
                        if (file.name.contains(".")) {
                            recoverFilesEntity = EntityFiles()
                            recoverFilesEntity.title = file.name
                            recoverFilesEntity.filePath = file.absolutePath
                            recoverFilesEntity.timeStamp = file.lastModified().toString()
                            recoverFilesEntity.mimeType = getMimeType(file.absolutePath)
                            recoverFilesEntity.fileSize = file.length()
                            objectList.add(recoverFilesEntity)
                        }
                    }
                }
                if (objectList.size > 0) {
                    binding!!.recyclerView.visibility = View.VISIBLE
                    binding!!.tvNoAudioVoice.visibility = View.GONE
                    adapterFavMedia!!.notifyDataSetChanged()
                } else {
                    binding!!.recyclerView.visibility = View.GONE
                    binding!!.tvNoAudioVoice.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun multiSelect(position: Int) {
        if (position > -1) {
            val data = adapterFavMedia!!.getItem(position)
            multi = true
            if (actionMode != null) {
                if (selectedIds.indexOfKey(position) > -1) selectedIds.remove(position) else selectedIds.put(
                    position,
                    data.title
                )
                if (selectedIds.size() > 0) actionMode!!.title = selectedIds.size()
                    .toString() + "  Selected" //show selected item count on action mode.
                else {
                    actionMode!!.title = "" //remove item count from action mode.
                    actionMode!!.finish() //hide action mode.
                }
                adapterFavMedia!!.setSelectedIds(selectedIds)
            } else {
                Timber.d("Action mode is null")
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
        isMultiSelect = false
        multi = false
        selectedIds = SparseArray()
        adapterFavMedia!!.setSelectedIds(selectedIds)
    }

    private fun alertDeleteConfirmation() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Delete " + selectedIds.size() + "selected Images?")
        builder.setPositiveButton("DELETE") { dialog: DialogInterface, _: Int ->
            try {
                for (i in 0 until selectedIds.size()) {
                    val recoverFileEntity = objectList[selectedIds.keyAt(i)]
                    val fdelete = File(recoverFileEntity.filePath!!)
                    if (fdelete.exists()) {
                        fdelete.delete()
                    }
                    objectList.removeAt(selectedIds.keyAt(i))
                }
                if (objectList.size > 0) {
                    adapterFavMedia!!.notifyDataSetChanged()
                } else {
                    binding!!.recyclerView.visibility = View.GONE
                    binding!!.tvNoAudioVoice.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            dialog.cancel()
            actionMode!!.title = "" //remove item count from action mode.
            actionMode!!.finish() //
        }.setNegativeButton("CANCEL") { dialog: DialogInterface, _: Int -> dialog.cancel() }
        builder.create().show()
    }

    private fun shareMultipleFiles() {
        try {
            val selecteduri = ArrayList<Uri>()
            //List<Integer> selectedItemPositions = recyclerViewAdapter.getSelectedItems();
            for (i in 0 until selectedIds.size()) {
                val recoverFileEntity = objectList[selectedIds.keyAt(i)]
                val uri = recoverFileEntity.filePath!!
                val u = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    File(uri)
                )
                selecteduri.add(u)
            }
            actionMode!!.title = ""
            actionMode!!.finish()
            shareMultiple(selecteduri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultiple(files: ArrayList<Uri>?) {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.type = "*/*"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
        startActivity(Intent.createChooser(intent, "Share files"))
    }

    @SuppressLint("SetTextI18n")
    private fun playAudio(itemPos: Int) {
        binding!!.playercontainer.visibility = View.VISIBLE
        val data = objectList[itemPos]
        binding!!.songName.text = data.title
        binding!!.animationView.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.round_circle
        )
        binding!!.totalTime.text = " / " + getDuration(data.filePath)
        try {
            if (mediaPlayer!!.isPlaying) {
                binding!!.animationView.pauseAnimation()
                pauseAudio()
                isAudioPlaying = false
                mediaPlayer!!.stop()
                mediaPlayer!!.release()
                mediaPlayer = null
            }
            mediaPlayer = MediaPlayer()
            getLength(data.filePath)
            val tempFile = File(data.filePath!!)
            val fis = FileInputStream(tempFile)
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(fis.fd)
            mediaPlayer!!.prepare()
            mediaPlayer!!.setOnPreparedListener { mediaPlayers: MediaPlayer ->
                mediaPlayer = mediaPlayers
                mediaPlayer!!.seekTo(0)
                binding!!.realseekBar.max = audioLengthInSec
                binding!!.realseekBar.progress = 0
                isAudioPlaying = true
                playPauseAudio()
            }
            mediaPlayer!!.setOnCompletionListener {
                mediaPlayer!!.seekTo(0)
                binding!!.realseekBar.progress = 0
                isPlaying = false
                binding!!.runTime.text = resources.getString(R.string._00_0000)
                binding!!.playButton.background = ContextCompat.getDrawable(
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
        binding!!.runTime.text = resources.getString(R.string._00_0000)
        binding!!.realseekBar.progress = 0
        binding!!.realseekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress * 1000)
                    binding!!.runTime.text = getCurrentDuration(progress * 1000L)
                }
            }
        })
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun playPauseAudio() {
        if (mediaPlayer != null) {
            if (isPlaying) {

                mediaPlayer!!.pause()
                binding!!.animationView.pauseAnimation()
                binding!!.playButton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.icon_primary_play
                )
                isPlaying = false
            } else {
                mediaPlayer!!.start()
                binding!!.animationView.playAnimation()
                binding!!.playButton.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.primary_pause_icon
                )
                isPlaying = true
                updateCurrentDuration()
            }
        }
    }

    private fun getLength(path: String?) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillisec = time!!.toLong()
        audioLengthInSec = TimeUnit.MILLISECONDS.toSeconds(timeInMillisec).toInt()
    }

    private fun getDuration(audioFilePath: String?): String {
        var timeInMillisec: Long = 1
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(audioFilePath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        if (time != null) {
            timeInMillisec = time.toLong()
        }
        return getCurrentDuration(timeInMillisec)
    }

    private fun updateCurrentDuration() {
        val updateVideoDuration: Runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    if (isAudioPlaying) {
                        if (mediaPlayer!!.isPlaying) {
                            binding!!.runTime.text =
                                getCurrentDuration(mediaPlayer!!.currentPosition.toLong())
                            handler!!.postDelayed(this, 1000)
                            binding!!.realseekBar.progress = mediaPlayer!!.currentPosition / 1000
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
            //stopPosition = mediaPlayer.getCurrentPosition();
            mediaPlayer!!.pause()
            isPlaying = false
            binding!!.animationView.pauseAnimation()
            binding!!.playButton.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.icon_primary_play
            )
        }
    }

    companion object {
        var multi = false
    }
}