package com.catchyapps.whatsdelete.appnotifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Environment
import android.os.FileObserver
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityhome.MainActivity
import com.catchyapps.whatsdelete.appactivities.activityrecover.MainRecoverActivity
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils.Companion.drawableToBitmap
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants
import com.catchyapps.whatsdelete.roomdb.AppHelperDb
import com.catchyapps.whatsdelete.roomdb.appentities.EntityChats
import com.catchyapps.whatsdelete.roomdb.appentities.EntityMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.Objects

class AppDeletedMessagesNotificationService : NotificationListenerService() {

    var context: Context? = null
    private var wakeLock: PowerManager.WakeLock? = null
   private var appSharedPrefs: MyAppSharedPrefs? = null
    private var WA_PATH = ""

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        appSharedPrefs = MyAppSharedPrefs(context)
        if (MyAppConstants.hWhatsAppOldFilePath.exists()) {
            WA_PATH = MyAppConstants.hWhatsAppOldFilePath.absolutePath
        } else if (MyAppConstants.hWhatsAppNewFilePath.exists()) {
            WA_PATH = MyAppConstants.hWhatsAppNewFilePath.absolutePath
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "whatsdelete:notification_wakelock")
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes max, re-acquired in onStartCommand

        createNotificationChannel()
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("fromNotification", true)
        val pendIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("Managing your deleted messages")
            .setContentIntent(pendIntent)
            .build()

        startForeground(1001, notification)
        Timber.d("Service onCreate - foreground started")
    }

    override fun onDestroy() {
        Timber.d("Service onDestroy called")
        imageFileObserver?.stopWatching()
        videoFileObserver?.stopWatching()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.d("Service onTaskRemoved - app swiped away")
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Channel name"
            val description = "Description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("NOTIFICATION_CHANNEL", name, importance)
            channel.description = description
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pack = sbn.packageName
        var ticker = ""
        val title: String
        var text = ""
        val extras = sbn.notification.extras
        title = extras.getString("android.title").toString()
        if (extras.getCharSequence("android.text") != null) {
            text = Objects.requireNonNull(extras.getCharSequence("android.text")).toString()
        }
        if (sbn.notification.tickerText != null) {
            ticker = sbn.notification.tickerText.toString()
        }
        Timber.d("title > $title, ticker > $ticker, text > $text")
        if (pack == "com.whatsapp")
            if (
                (
                        ticker != "WhatsApp" &&
                                ticker != "WhatsApp Web" &&
                                !ticker.contains("@") &&
                                ticker != "Backup in progress" &&
                                ticker != "Finished backup"
                        )
                &&
                !title.contains("WhatsApp") &&
                !title.contains("new messages") &&
                !ticker.contains("missed voice call") &&
                !ticker.contains("missed video call") &&
                !text.contains("You have new messages") &&
                !text.contains("new messages") &&
                !text.contains("messages from") &&
                !title.contains("Backup paused") &&
                !title.contains("Missed") &&
                !title.contains("missed calls") &&
                !title.contains("Deleting") &&
                !title.contains("Backup in progress") &&
                !title.contains("Finished backup") &&
                !title.contains("Couldn't complete")
            ) {
                var bitmap: Bitmap? = null
                var byteArray = ByteArray(0)
                try {
                    if (extras[Notification.EXTRA_LARGE_ICON] is Icon) {
                        val icon = extras[Notification.EXTRA_LARGE_ICON] as Icon?
                        if (icon != null) {
                            val drawable = icon.loadDrawable(context)
                            bitmap = drawable?.let { drawableToBitmap(it) }
                        }
                    } else {
                        bitmap = extras[Notification.EXTRA_LARGE_ICON] as Bitmap?
                    }
                    if (bitmap != null) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        byteArray = stream.toByteArray()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    doInBackground(text, title, ticker, pack, byteArray)
                }

                val msgrcv = Intent("Msg")
                msgrcv.putExtra("package", pack)
                msgrcv.putExtra("ticker", ticker)
                msgrcv.putExtra("title", title)
                msgrcv.putExtra("text", text)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    msgrcv.putExtra("icon", byteArray)
                }
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(msgrcv)
            }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Timber.i("Notification Removed")
    }


    suspend fun doInBackground(
        finalText: String,
        finalTitle: String,
        finalTicker: String,
        pack: String,
        dpPath: ByteArray?
    ) {
        var hFinalTitle = finalTitle
        var hFinalText = finalText
        var hUserName: String? = null
        if (hFinalTitle.contains(":")) {
            val token = hFinalTitle.split(":").toTypedArray()
            hUserName = token[1]
            hFinalTitle = token[0].split("\\(").toTypedArray()[0].trim { it <= ' ' }
        }


        var chatEntity = AppHelperDb.getChatByTitle(hFinalTitle)
        val message = hFinalText
        val chatType: String
        if (hUserName != null) {
            chatType = MyAppUtils.GROUP_CHAT
            hFinalText = "$hUserName: $hFinalText"
        } else {
            chatType = MyAppUtils.PRIVATE_CHAT
        }
        var isNewMessage = false
        var messageType = MyAppUtils.TEXT
        when {
            message.contains("\uD83D\uDCF7") -> { // Image
                messageType = MyAppUtils.IMAGE
            }

            message.contains("\uD83C\uDFA5") -> { // Video
                messageType = MyAppUtils.VIDEO
            }

            message.contains("\uD83C\uDFA4") -> {  // Voice
                messageType = MyAppUtils.VOICE
            }

            message.contains("\uD83D\uDCCC") -> {  // Location
                messageType = MyAppUtils.LOCATION
            }

            message.contains("\uD83D\uDCC4") -> {   // Document
                messageType = MyAppUtils.DOCUMENT
            }

            message.contains("\uD83C\uDFB5") -> {   // Audio
                messageType = MyAppUtils.AUDIO
            }

            message.contains("\uD83D\uDC64") -> {    // Contact
                messageType = MyAppUtils.CONTACT
            }
        }
        if (chatEntity != null) {
            if (chatEntity.lastMessage != hFinalText) {
                isNewMessage = true
                AppHelperDb.updateChatRow(
                    chatEntity.unSeenCount + 1,
                    System.currentTimeMillis().toString(),
                    hFinalText,
                    dpPath,
                    chatEntity.id
                )
            }
        } else {
            isNewMessage = true
            chatEntity =
                EntityChats()
            chatEntity.title = hFinalTitle
            chatEntity.chatType = chatType
            chatEntity.unSeenCount = 1
            chatEntity.lastMessageTime = System.currentTimeMillis().toString()
            chatEntity.lastMessage = hFinalText
            chatEntity.lastMessageType = messageType
            chatEntity.profilePic = dpPath

            chatEntity.id = AppHelperDb.insertChatRow(chatEntity)
        }
        if (isNewMessage) {
            val messagesEntity = EntityMessages()
            messagesEntity.chatHeaderId = chatEntity.id
            messagesEntity.timeStamp = System.currentTimeMillis().toString()
            messagesEntity.body = message
            messagesEntity.chatHeaderName = hFinalTitle
            messagesEntity.messageType = messageType
            messagesEntity.title = hUserName
            messagesEntity.ticker = finalTicker
            val notificationId = AppHelperDb.insertChildNotification(messagesEntity)

            messagesEntity.id = notificationId
            if (message == "This message was deleted") {
                createDeletedMessageNotification(
                    "$hFinalTitle deleted message",
                    "Click here to see deleted message"
                )
            }
        }
    }


    fun createDeletedMessageNotification(title: String?, message: String?) {
        val hNotificationChannelId = "10001"
        val resultIntent = Intent(this, MainRecoverActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent = PendingIntent.getActivity(
            this,
            0 /* Request code */, resultIntent,
            PendingIntent.FLAG_CANCEL_CURRENT xor PendingIntent.FLAG_IMMUTABLE
        )
        val mBuilder = NotificationCompat.Builder(
            this,
            hNotificationChannelId
        )
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
        mBuilder.setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(false)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(resultPendingIntent)
        val mNotificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel =
                NotificationChannel(hNotificationChannelId, "NOTIFICATION_CHANNEL_NAME", importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder.setChannelId(hNotificationChannelId)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build())
    }

    private var imageFileObserver: FileObserver? = null
    private var videoFileObserver: FileObserver? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service onStartCommand - flags=%d startId=%d", flags, startId)

        // Re-acquire wakelock on each start command (service may have been restarted)
        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(10 * 60 * 1000L)
        }

        // Stop old observers before creating new ones (prevents leaking watchers)
        imageFileObserver?.stopWatching()
        videoFileObserver?.stopWatching()

        val imagePath = WA_PATH + "/Media/" + MyAppUtils.WA + " Images/"
        val srcDir = File(imagePath)
        if (srcDir.exists()) {
            imageFileObserver = object : FileObserver(srcDir.absolutePath) {
                override fun onEvent(event: Int, path: String?) {
                    if (event == CREATE || event == CLOSE_WRITE || event == MODIFY || event == MOVED_TO) {
                        Timber.d("Image file event: path=%s", path)
                        val destinationPath =
                            MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_RECOVER_IMAGES
                        copyAttachmentFile(imagePath + path, destinationPath)
                    }
                }
            }
            imageFileObserver?.startWatching()
        }

        val videoPath = WA_PATH + "/Media/" + MyAppUtils.WA + " Video/"
        val videoDir = File(videoPath)
        if (videoDir.exists()) {
            videoFileObserver = object : FileObserver(videoDir.absolutePath) {
                override fun onEvent(event: Int, path: String?) {
                    if (event == CREATE || event == CLOSE_WRITE || event == MODIFY || event == MOVED_TO) {
                        Timber.d("Video file event: path=%s", path)
                        val destinationPath =
                            MyAppUtils.ROOT_FOLDER + "/" + MyAppUtils.WA_RECOVER_VIDEOS
                        copyAttachmentFile(videoPath + path, destinationPath)
                    }
                }
            }
            videoFileObserver?.startWatching()
        }

        // START_STICKY: system will restart this service if it gets killed
        return START_STICKY
    }

    private fun copyAttachmentFile(srcPath: String, destPath: String) {
        val desDir = File(Environment.getExternalStorageDirectory(), destPath)
        val source = File(Environment.getExternalStorageDirectory(), srcPath)
        try {
            FileUtils.copyFileToDirectory(source, desDir)
        } catch (e: IOException) {
            if (e.message != null) Timber.i(e.message!!)
        }
    }


}