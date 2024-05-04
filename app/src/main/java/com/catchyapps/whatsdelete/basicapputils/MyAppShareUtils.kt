package com.catchyapps.whatsdelete.basicapputils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.catchyapps.whatsdelete.R

object MyAppShareUtils {
     fun copyText(text: String,activity: Activity) {
        val clipboardManager = activity.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("TextRepeater", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(activity, "Copied", Toast.LENGTH_SHORT).show()
    }

     fun shareText(text: String,title:String,activity: Activity) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        activity.startActivity(shareIntent)
    }

     fun shareToWhatsApp(message: String,activity: Activity) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, message)
        sendIntent.type = "text/plain"
        sendIntent.setPackage("com.whatsapp")

        try {
            activity.startActivity(sendIntent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(activity, activity.getString(R.string.whatsapp_not_found), Toast.LENGTH_LONG).show()
        }
    }
}