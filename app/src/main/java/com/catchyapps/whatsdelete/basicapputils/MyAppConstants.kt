package com.catchyapps.whatsdelete.basicapputils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.catchyapps.whatsdelete.R
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object MyAppConstants {

    @JvmField
    val hWhatsAppOldFilePath = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "WhatsApp"
    )

    @JvmField
    val hWhatsAppNewFilePath = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "Android/media/com.whatsapp/WhatsApp"
    )
    var hTag = "khurramTag %s"
    const val H_AUDIO_FRAGMENT = R.string.audio
    const val H_VOICE_FRAGMENT = R.string.voice
    const val H_VIDEO_FRAGMENT =R.string.video
    const val H_IMAGES_FRAGMENT =R.string.image
    const val H_DOCUMENT_FRAGMENT = R.string.document
    const val H_CHATS_FRAGMENT = R.string.chat


    const val H_TITLE_ARG = "title"
    const val H_IS_AUDIO = "is_audio"
    const val H_IS_IMAGE = "is_image"
    val H_TITLE_ARRAY = arrayOf(R.string.image, R.string.video)




/*  //copied code
    val H_URI_SCHEMA= "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia"
    val STATUS_DIRECTORY = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "WhatsApp/Media/.Statuses"
    )
    val STATUS_DIRECTORY_NEW = File(
        Environment.getExternalStorageDirectory().toString() +
                File.separator + "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
    )*/


    const val WA_FAV_VIDEOS = "WARecover Favourite_Videos"
    const val WA_FAV_DOC = "WARecover Favourite_Doc"
    const val WA_FAV_AUDIO = "WARecover Favourite_Audio"
    const val WA_FAV_VOICE = "WARecover Favourite_Voice"
    const val WA_FAV_IMAGES = "WARecover Favourite_Images"

    @JvmField
    var ROOT_FOLDER = "WARecover"


    @JvmStatic
    fun showToast(context: Context?, s: String?) {
        if (context != null && s != null) Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }

    @JvmStatic
    fun logCat(s: String?) {
        if (s != null) Timber.d(s) else Timber.d("logcat null")
    }


    @JvmStatic
    fun getFileDateTime(timeStamp: Long): String {
        val dateTime: String
        val date = Date(timeStamp)
        @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a")
        dateTime = formatter.format(date)
        return dateTime
    }

    @JvmStatic
    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

}
