package com.catchyapps.whatsdelete.appclasseshelpers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class MyAppUtils {
    companion object {
        @JvmField
        val WA = "5ha0sA33".replace("5", "W").replace('3', 'p').replace('0', 't')

        @JvmField
        var ROOT_FOLDER = "WAppRecover"

        @JvmField
        var WA_RECOVER_IMAGES = "WARecover Images"

        @JvmField
        var WA_RECOVER_VIDEOS = "WARecover Videos"
        const val WA_STATUS = "WAStatus Saver/"
        const val PRIVATE_CHAT = "Private Chat"
        const val GROUP_CHAT = "Group Chat"
        const val TEXT = 5
        const val AUDIO = 6
        const val IMAGE = 7
        const val VIDEO = 8
        const val LOCATION = 9
        const val DOCUMENT = 10
        const val CONTACT = 11
        const val VOICE = 12

        @JvmStatic
        fun showToast(context: Context?, s: String?) {
            if (context != null && s != null) Toast.makeText(context, s, Toast.LENGTH_LONG).show()
        }

        fun getLastSeenDateTime(timeStamp: Long): String {
            val dateTime: String
            val date = Date(timeStamp)
            val todayDate = Date()
            dateTime = if (date.date == todayDate.date) {
                @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat("hh:mm a")
                "Today " + formatter.format(date)
            } else {
                @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat("MM/dd hh:mm a")
                formatter.format(date)
            }
            return dateTime
        }

        @JvmStatic
        fun getFileDateTime(timeStamp: Long): String {
            val dateTime: String
            val date = Date(timeStamp)
            @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat("MMM dd, yyyy hh:mm a")
            dateTime = formatter.format(date)
            return dateTime
        }

        fun getTimeFromTimeStamp(timeStamp: Long): String {
            val date = Date(timeStamp)
            @SuppressLint("SimpleDateFormat") val formatter = SimpleDateFormat("hh:mm a")
            return formatter.format(date)
        }

        fun getMimeType(url: String?): String? {
            var type: String? = null
            val extension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
            return type
        }

        @JvmStatic
        fun drawableToBitmap(drawable: Drawable): Bitmap {
            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable
                if (bitmapDrawable.bitmap != null) {
                    return bitmapDrawable.bitmap
                }
            }
            val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun noInternetConnectionDialog(con: Context?) {
            val builder = AlertDialog.Builder(con)
            builder.setMessage("No Internet connection. Make sure that Wi-Fi or mobile data is turned on then try again.")
                .setPositiveButton("Ok") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            builder.create().show()
        }

        fun showSnakeBar(mContext: Context) {
            val snackbar = Snackbar.make((mContext as Activity).findViewById(R.id.content), "Added to folder", Snackbar.LENGTH_LONG)
                .setAction("See List") { v: View? -> }
            snackbar.show()
        }

        @JvmStatic
        fun snackBar(context: Context, msg: String?) {
            Snackbar.make((context as Activity).findViewById(R.id.content), msg!!, Snackbar.LENGTH_SHORT).show()
        }

        fun hideKeyBoard(v: View?, context: Context) {
            if (v != null) {
                val inm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        val whatsappPath: String?
            get() {
                return when {
                    MyAppConstants.hWhatsAppOldFilePath.exists() -> {
                        Timber.d("Inside Case 1 ")
                        MyAppConstants.hWhatsAppOldFilePath.absolutePath
                    }
                    MyAppConstants.hWhatsAppNewFilePath.exists() -> {
                        Timber.d("Inside Case 2 ")
                        MyAppConstants.hWhatsAppNewFilePath.absolutePath
                    }
                    else -> {
                        Timber.d("Inside Case 3 ")

                        null
                    }
                }
            }
    }

}