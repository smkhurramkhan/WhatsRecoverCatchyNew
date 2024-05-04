package com.catchyapps.whatsdelete.basicapputils

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.treeUri
import com.catchyapps.whatsdelete.databinding.FolderDialogPermissionLayoutBinding
import timber.log.Timber

class MyAppPermissionUtils {
    companion object {

        fun hHasReadWritePermissions(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
                    &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }

        fun hRationaileCheck(context: Context): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                context as AppCompatActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        fun hShowUriPermissionDialog(
            context: Context,
            description: String,
            hCallBack: () -> Unit
        ) {
            val builder = AlertDialog.Builder(context)
            val hFolderPermissionDialogBinding = FolderDialogPermissionLayoutBinding.inflate(
                (context as AppCompatActivity).layoutInflater,
                null,
                false
            )
            builder.setView(hFolderPermissionDialogBinding.root)

            val dialog = builder.create()
            if (dialog.window != null) dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            hFolderPermissionDialogBinding.tvAlertDesc.text =
                HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
            hFolderPermissionDialogBinding.btnPositive.setOnClickListener {
                hCallBack()
                dialog.dismiss()
            }
            hFolderPermissionDialogBinding.btnNegative.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }


        fun hasPermissionPost10(context: Context): Boolean {

            val mContentResolver: ContentResolver = context.contentResolver
            mContentResolver.persistedUriPermissions.find {
                it.uri.equals(treeUri) && it.isReadPermission
            }?.run {
                Timber.d("Permission Granted")
                return true
            } ?: run {
                return false
            }
        }

    }

}