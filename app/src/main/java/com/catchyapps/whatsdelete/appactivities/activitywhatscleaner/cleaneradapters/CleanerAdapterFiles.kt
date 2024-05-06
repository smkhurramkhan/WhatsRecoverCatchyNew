package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitypreview.VideoMediaPlayerActivity
import com.catchyapps.whatsdelete.appactivities.activitypreview.PreviewScreen
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_AUDIO
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_GIFS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_IMAGE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_STATUS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VIDEOS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_VOICE
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.H_WALLPAPERS
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.cleanerviewholder.ViewHolderDoc
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.cleanerviewholder.ViewHolderImage
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleaneradapters.cleanerviewholder.ViewHolderVideos
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerDetailsFile
import com.catchyapps.whatsdelete.databinding.ItemContentDocumentLayoutBinding
import com.catchyapps.whatsdelete.databinding.ItemContentImagesLayoutBinding
import com.catchyapps.whatsdelete.databinding.ItemContentVideosLayoutBinding
import timber.log.Timber
import java.io.File

class CleanerAdapterFiles(
    private val context: Context,
    private val hCheckBoxCallBack: (CleanerDetailsFile) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var hFileDetailsList = listOf<CleanerDetailsFile>()


    companion object {
        const val H_IMAGE_VH = 0
        const val H_VIDEO_VH = 1
        const val H_DOC_VH = 2

    }

    override fun getItemViewType(position: Int): Int {
        return when (hFileDetailsList[position].hType) {
            H_IMAGE,
            H_WALLPAPERS,
            H_GIFS,
            H_STATUS,
            -> H_IMAGE_VH
            H_VIDEOS -> H_VIDEO_VH
            else -> H_DOC_VH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            H_IMAGE_VH -> {
                return ViewHolderImage(
                    ItemContentImagesLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            H_VIDEO_VH -> return ViewHolderVideos(
                ItemContentVideosLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> {
                return ViewHolderDoc(
                    ItemContentDocumentLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, positions: Int) {
        when (viewHolder) {
            is ViewHolderDoc -> hBindDocViewHolder(
                docViewHolder = viewHolder,
                positions = positions
            )
            is ViewHolderImage -> hBindImageViewHolder(
                imageViewHolder = viewHolder,
                positions = positions
            )
            is ViewHolderVideos -> hBindVideoViewHolder(
                videosViewHolder = viewHolder,
                positions = positions
            )
        }
    }

    private fun hBindVideoViewHolder(
        videosViewHolder: ViewHolderVideos,
        positions: Int,
    ) {
        val hFileDetailsItem = hFileDetailsList[positions]
        videosViewHolder.hItemVideosContentBinding.apply {
            play.circleBackgroundColor = ContextCompat.getColor(
                play.context,
                hFileDetailsItem.color
            )
            play.borderColor = ContextCompat.getColor(
                play.context,
                hFileDetailsItem.color
            )
            play.setImageResource(hFileDetailsItem.image)
            Glide.with(context)
                .load(hFileDetailsItem.path)
                .transition(DrawableTransitionOptions.withCrossFade())
                .thumbnail(Glide.with(context).load(R.drawable.loading_img))
                .into(image)
            image.setOnClickListener {
                File(
                    Uri.parse(
                        hFileDetailsItem.path
                    ).toString())
                val intent = Intent(
                    context,
                    VideoMediaPlayerActivity::class.java
                )
                intent.putExtra("path", hFileDetailsItem.path)
                (context as AppCompatActivity).startActivityForResult(intent, 101)
            }
            checkbox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                hFileDetailsItem.isSelected = isChecked
                if (checkbox.isPressed) {
                    hCheckBoxCallBack(hFileDetailsItem)
                }
            }
            checkbox.isChecked = hFileDetailsItem.isSelected
        }

    }

    private fun hBindImageViewHolder(imageViewHolder: ViewHolderImage, positions: Int) {
        val hFileDetailsItem = hFileDetailsList[positions]
        imageViewHolder.hItemImagesContentBinding.apply {
            Glide.with(context)
                .load(hFileDetailsItem.path)
                .transition(DrawableTransitionOptions.withCrossFade())
                .thumbnail(Glide.with(context).load(R.drawable.loading_img))
                .into(image)
            image.setOnClickListener { v: View? ->
                val intent = Intent(
                    context,
                    PreviewScreen::class.java
                )
                intent.putExtra(
                    "file_path",
                    hFileDetailsItem.path
                )
                (context as AppCompatActivity).startActivityForResult(
                    intent,
                    101
                )
            }
            checkbox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                hFileDetailsItem.isSelected = isChecked
                if (checkbox.isPressed) {
                    hCheckBoxCallBack(hFileDetailsItem)
                }
            }
            checkbox.isChecked = hFileDetailsItem.isSelected
        }

    }

    private fun hBindDocViewHolder(docViewHolder: ViewHolderDoc, positions: Int) {
        val hFileDetailsItem = hFileDetailsList[positions]
        when (hFileDetailsItem.hType) {
            H_AUDIO -> hSetDocViewForAudioFile(
                docViewHolder.hItemDocumentContentBinding,
                hFileDetailsItem
            )
            H_VOICE -> hSetDocViewForVoiceFile(
                docViewHolder.hItemDocumentContentBinding,
                hFileDetailsItem
            )
            else -> hSetDocViewForFile(
                docViewHolder.hItemDocumentContentBinding,
                hFileDetailsItem
            )
        }
    }

    private fun hSetDocViewForFile(
        hItemDocumentContentBinding: ItemContentDocumentLayoutBinding,
        hFileDetailsItem: CleanerDetailsFile,
    ) {
        hItemDocumentContentBinding.apply {
            title.text = hFileDetailsItem.name
            data.text = hFileDetailsItem.hFormatedSize.toString()
            extension.text = hFileDetailsItem.ext
            image.setImageResource(hFileDetailsItem.image)
            checkbox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                hFileDetailsItem.isSelected = isChecked
                if (checkbox.isPressed) {
                    hCheckBoxCallBack(hFileDetailsItem)
                }
            }
            checkbox.isChecked = hFileDetailsItem.isSelected
            hCardView.setOnClickListener { v: View? ->
                val a = File(Uri.parse(hFileDetailsItem.path).toString())
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider", a
                )
                val intent = Intent(Intent.ACTION_VIEW)
                var mime = "*/*"
                val mimeTypeMap = MimeTypeMap.getSingleton()
                if (mimeTypeMap.hasExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    )
                ) {
                    mime = mimeTypeMap.getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    ).toString()
                }
                try {
                    Timber.e(mime)
                    intent.setDataAndType(uri, mime)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context,
                        context.getString(R.string.couldn_t_find_app_that_open_this_file), Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun hSetDocViewForVoiceFile(
        hItemDocumentContentBinding: ItemContentDocumentLayoutBinding,
        hFileDetailsItem: CleanerDetailsFile,
    ) {
        hItemDocumentContentBinding.apply {

            title.text = hFileDetailsItem.name
            data.text = hFileDetailsItem.hFormatedSize.toString()
            extension.text = hFileDetailsItem.ext
            image.setImageResource(hFileDetailsItem.image)
            checkbox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                hFileDetailsItem.isSelected = isChecked
                if (checkbox.isPressed) {
                    hCheckBoxCallBack(hFileDetailsItem)
                }
            }
            checkbox.isChecked = hFileDetailsItem.isSelected
            hCardView.setOnClickListener { v: View? ->
                val a = File(Uri.parse(hFileDetailsItem.path).toString())
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider", a
                )
                val intent = Intent(Intent.ACTION_VIEW)
                var mime = "*/*"
                val mimeTypeMap = MimeTypeMap.getSingleton()
                if (mimeTypeMap.hasExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    )
                ) {
                    mime = mimeTypeMap.getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    ).toString()
                }
                try {
                    Timber.e(mime)
                    intent.setDataAndType(uri, mime)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, context.getString(R.string.couldn_t_find_app_that_open_this_file), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hSetDocViewForAudioFile(
        hItemDocumentContentBinding: ItemContentDocumentLayoutBinding,
        hFileDetailsItem: CleanerDetailsFile,
    ) {
        hItemDocumentContentBinding.apply {
            title.text = hFileDetailsItem.name
            data.text = hFileDetailsItem.hFormatedSize.toString()
            extension.text = hFileDetailsItem.ext
            image.setImageResource(hFileDetailsItem.image)
            checkbox.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
                hFileDetailsItem.isSelected = isChecked
                if (checkbox.isPressed) {
                    hCheckBoxCallBack(hFileDetailsItem)
                }
            }
            checkbox.isChecked = hFileDetailsItem.isSelected
            hCardView.setOnClickListener {
                val a = File(Uri.parse(hFileDetailsItem.path).toString())

                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider", a
                )
                val intent = Intent(Intent.ACTION_VIEW)
                var mime = "*/*"
                val mimeTypeMap = MimeTypeMap.getSingleton()
                if (mimeTypeMap.hasExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    )
                ) {
                    mime = mimeTypeMap.getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                    ).toString()
                }
                try {
                    Timber.e(mime)
                    intent.setDataAndType(uri, mime)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, context.getString(R.string.couldn_t_find_app_that_open_this_file), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return hFileDetailsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun hSetData(it: List<CleanerDetailsFile>?) {
        Timber.d("hello")
        if (it != null) {
            hFileDetailsList = it
            notifyDataSetChanged()
        }
    }

}
