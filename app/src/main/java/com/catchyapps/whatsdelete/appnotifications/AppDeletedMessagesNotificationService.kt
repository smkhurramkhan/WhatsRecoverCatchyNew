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
import android.os.FileObserver
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Objects

class AppDeletedMessagesNotificationService : NotificationListenerService() {

    var context: Context? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var appSharedPrefs: MyAppSharedPrefs? = null
    private var WA_PATH = ""
    private var mediaPollingJob: Job? = null

    private var imageFileObserver: FileObserver? = null
    private var videoFileObserver: FileObserver? = null
    private var audioFileObserver: FileObserver? = null
    private var documentFileObserver: FileObserver? = null

    private companion object {
        const val MAX_RECENT_KEYS = 50
        const val MEDIA_POLL_INTERVAL_MS = 15_000L // 15 seconds
    }

    // Prevents duplicate processing from concurrent coroutines
    private val dbMutex = Mutex()
    // Tracks recently processed notification keys to skip duplicates
    private val recentNotificationKeys = LinkedHashSet<String>()

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
        showForegroundNotification()
        Timber.d("Service onCreate - foreground started")
    }

    override fun onDestroy() {
        Timber.d("Service onDestroy called")
        mediaPollingJob?.cancel()
        mediaPollingJob = null
        stopAllFileObservers()
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        super.onDestroy()
    }

    private fun stopAllFileObservers() {
        imageFileObserver?.stopWatching()
        videoFileObserver?.stopWatching()
        audioFileObserver?.stopWatching()
        documentFileObserver?.stopWatching()
        imageFileObserver = null
        videoFileObserver = null
        audioFileObserver = null
        documentFileObserver = null
    }

    /**
     * Creates a FileObserver that copies new files to app-private storage with .cache.
     * CLOSE_WRITE ensures the file is fully written before we copy.
     */
    private fun createMediaObserver(watchPath: String, mediaType: String): FileObserver {
        return object : FileObserver(watchPath, CLOSE_WRITE or MOVED_TO) {
            override fun onEvent(event: Int, path: String?) {
                if (path == null || path.startsWith(".")) return
                val sourceFile = File(watchPath, path)
                if (!sourceFile.isFile) return
                Timber.d("FileObserver [%s]: new file %s", mediaType, path)
                MediaBackupHelper.cacheFile(applicationContext, mediaType, sourceFile)
            }
        }
    }

    /**
     * Polling loop — only checks for deleted files (reveals).
     * Pre-Android 11: uses direct File.exists()
     * Android 11+: uses SAF queries
     */
    private fun startMediaPollingLoop() {
        mediaPollingJob?.cancel()
        mediaPollingJob = CoroutineScope(Dispatchers.IO).launch {
            Timber.d("POLL_DEBUG: polling coroutine started, interval=%dms", MEDIA_POLL_INTERVAL_MS)
            while (isActive) {
                delay(MEDIA_POLL_INTERVAL_MS)
                Timber.d("POLL_DEBUG: tick - checking for deleted files (api=%d)", Build.VERSION.SDK_INT)
                try {
                    val revealed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        MediaBackupHelper.checkRevealsSaf(applicationContext)
                    } else if (WA_PATH.isNotEmpty()) {
                        MediaBackupHelper.checkReveals(applicationContext, "$WA_PATH/Media")
                    } else 0

                    if (revealed > 0) {
                        Timber.d("Media polling: %d deleted media file(s) recovered", revealed)
                        createDeletedMessageNotification(
                            "Deleted media recovered",
                            "$revealed deleted media file(s) recovered. Tap to view."
                        )
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Media polling: reveal check failed")
                }
            }
        }
        Timber.d("Media polling loop started (interval=%ds)", MEDIA_POLL_INTERVAL_MS / 1000)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.d("Service onTaskRemoved - app swiped away")
        super.onTaskRemoved(rootIntent)
    }

    private fun showForegroundNotification() {
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

    /**
     * Returns the specific media type for the notification text, or null if not media.
     */
    private fun detectMediaType(text: String): String? {
        return when {
            text.contains("\uD83D\uDCF7") -> MediaBackupHelper.TYPE_IMAGES  // 📷
            text.contains("\uD83C\uDFA5") -> MediaBackupHelper.TYPE_VIDEOS  // 🎥
            text.contains("\uD83C\uDFA4") -> MediaBackupHelper.TYPE_AUDIO   // 🎤 Voice
            text.contains("\uD83C\uDFB5") -> MediaBackupHelper.TYPE_AUDIO   // 🎵 Audio
            text.contains("\uD83D\uDCC4") -> MediaBackupHelper.TYPE_DOCUMENTS // 📄
            else -> null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pack = sbn.packageName
        if (pack != "com.whatsapp") return

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

        // Deduplicate: skip if we already processed this exact title+text combo recently.
        // BUT: don't dedup media notifications — text is always the same (e.g. "📷 Photo")
        // so each new image from the same person would be wrongly skipped.
        val isMediaNotification = detectMediaType(text) != null
        val dedupeKey = "$title|$text"
        synchronized(recentNotificationKeys) {
            if (!isMediaNotification && recentNotificationKeys.contains(dedupeKey)) {
                Timber.d("Skipping duplicate notification: title=%s, text=%s", title, text)
                return
            }
            recentNotificationKeys.add(dedupeKey)
            if (recentNotificationKeys.size > MAX_RECENT_KEYS) {
                recentNotificationKeys.remove(recentNotificationKeys.first())
            }
        }

        Timber.d("NOTIF_DEBUG: title='%s', ticker='%s', text='%s', isMedia=%b", title, ticker, text, isMediaNotification)

        // Android 11+ SAF: trigger media caching IMMEDIATELY for media notifications.
        // This runs BEFORE the filter so even if the notification is filtered for DB,
        // the media file still gets cached.
        if (isMediaNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val detectedType = detectMediaType(text)
            Timber.d("MEDIA_CACHE: detected type=[%s] from onNotificationPosted, scheduling SAF scan", detectedType)
            CoroutineScope(Dispatchers.IO).launch {
                delay(2_000)
                try {
                    Timber.d("MEDIA_CACHE: SAF scan [%s] starting (from onNotificationPosted)", detectedType)
                    MediaBackupHelper.onMediaNotificationReceived(applicationContext, detectedType)
                    Timber.d("MEDIA_CACHE: SAF scan [%s] done (from onNotificationPosted)", detectedType)
                } catch (e: Exception) {
                    Timber.w(e, "MEDIA_CACHE: SAF scan [%s] failed", detectedType)
                }
            }
        }

        // Immediate reveal: when "This message was deleted" is detected, check for
        // deleted media right away instead of waiting for the 15s polling loop.
        if (text == "This message was deleted") {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1_500) // small delay for WhatsApp to finish deleting the file
                try {
                    val revealed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        MediaBackupHelper.checkRevealsSaf(applicationContext)
                    } else if (WA_PATH.isNotEmpty()) {
                        MediaBackupHelper.checkReveals(applicationContext, "$WA_PATH/Media")
                    } else 0

                    if (revealed > 0) {
                        Timber.d("REVEAL_IMMEDIATE: %d file(s) revealed after delete notification", revealed)
                        createDeletedMessageNotification(
                            "Deleted media recovered",
                            "$revealed deleted media file(s) recovered. Tap to view."
                        )
                    }
                } catch (e: Exception) {
                    Timber.w(e, "REVEAL_IMMEDIATE: check failed")
                }
            }
        }

        if (
            (
                    ticker != "WhatsApp" &&
                            ticker != "WhatsApp Web" &&
                            !ticker.contains("@") &&
                            ticker != "Backup in progress" &&
                            ticker != "Finished backup"
                    )
            &&
            title != "You" &&
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
            // Use Mutex to serialize DB writes -- prevents race condition on first install
            CoroutineScope(Dispatchers.Main).launch {
                dbMutex.withLock {
                    doInBackground(text, title, ticker, pack, byteArray)
                }
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

        // Group format: "GroupName (N messages): SenderName"
        var isGroupByTitle = false
        val firstParen = hFinalTitle.indexOf('(')
        if (firstParen > 0) {
            val insideParen = hFinalTitle.substring(firstParen + 1).trimStart()
            if (insideParen.firstOrNull()?.isDigit() == true) {
                isGroupByTitle = true
                // Sender is after the first ":" that comes after "("
                val colonAfterParen = hFinalTitle.indexOf(':', firstParen)
                if (colonAfterParen > firstParen) {
                    hUserName = hFinalTitle.substring(colonAfterParen + 1).trim()
                }
                // Group name is everything before "("
                hFinalTitle = hFinalTitle.substring(0, firstParen).trim()
            }
        }

        // Other group formats
        if (!isGroupByTitle) {
            if (hFinalText.startsWith("~ ")) {
                val colonIdx = hFinalText.indexOf(":", 2)
                if (colonIdx > 2) {
                    hUserName = hFinalText.substring(2, colonIdx).trim()
                    isGroupByTitle = true
                }
            } else if (hFinalTitle.contains(":")) {
                val token = hFinalTitle.split(":").toTypedArray()
                if (token.size > 1) {
                    hUserName = token.last().trim()
                    hFinalTitle = token[0].split("\\(").toTypedArray()[0].trim { it <= ' ' }
                    isGroupByTitle = true
                }
            }
        }

        var chatEntity = AppHelperDb.getChatByTitle(hFinalTitle)
        val message = hFinalText
        val chatType: String
        if (isGroupByTitle || hUserName != null) {
            chatType = MyAppUtils.GROUP_CHAT
            // For old format (title had "Group: Sender"), text is just the message — prepend sender
            // For new formats, text already has "Sender: message" — don't double it
            if (hUserName != null && !hFinalText.startsWith("$hUserName:") && !hFinalText.startsWith("~ ")) {
                hFinalText = "$hUserName: $hFinalText"
            }
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
            messagesEntity.title = hUserName ?: hFinalTitle
            messagesEntity.ticker = finalTicker
            val notificationId = AppHelperDb.insertChildNotification(messagesEntity)

            messagesEntity.id = notificationId

            // "This message was deleted" notification is now handled by the
            // immediate reveal check in onNotificationPosted — no separate
            // notification here to avoid duplicates.
        }

        // Android 11+: cache the media file via SAF scan of the specific folder.
        val mediaFolder = when (messageType) {
            MyAppUtils.IMAGE -> MediaBackupHelper.TYPE_IMAGES
            MyAppUtils.VIDEO -> MediaBackupHelper.TYPE_VIDEOS
            MyAppUtils.VOICE, MyAppUtils.AUDIO -> MediaBackupHelper.TYPE_AUDIO
            MyAppUtils.DOCUMENT -> MediaBackupHelper.TYPE_DOCUMENTS
            else -> null
        }
        Timber.d("MEDIA_CACHE: messageType=%s, mediaFolder=%s, api=%d", messageType, mediaFolder, Build.VERSION.SDK_INT)
        if (mediaFolder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(2_000)
                try {
                    Timber.d("MEDIA_CACHE: scanning [%s] for new file", mediaFolder)
                    MediaBackupHelper.onMediaNotificationReceived(applicationContext, mediaFolder)
                    Timber.d("MEDIA_CACHE: scan [%s] done", mediaFolder)
                } catch (e: Exception) {
                    Timber.w(e, "MEDIA_CACHE: scan failed")
                }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service onStartCommand - flags=%d startId=%d", flags, startId)
        Timber.d("SERVICE_DEBUG: WA_PATH='%s' API=%d", WA_PATH, Build.VERSION.SDK_INT)
        val safPerms = contentResolver.persistedUriPermissions
        Timber.d("SERVICE_DEBUG: SAF permissions count=%d", safPerms.size)
        for (perm in safPerms) {
            Timber.d("SERVICE_DEBUG: SAF perm uri=%s read=%b write=%b", perm.uri, perm.isReadPermission, perm.isWritePermission)
        }

        createNotificationChannel()
        showForegroundNotification()

        if (wakeLock?.isHeld != true) {
            wakeLock?.acquire(10 * 60 * 1000L)
        }

        // Pre-Android 11: FileObserver watches WA folders directly (gives exact filename)
        // Android 11+: FileObserver blocked by Scoped Storage, SAF used from notification trigger
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && WA_PATH.isNotEmpty()) {
            setupFileObservers()
        } else {
            Timber.d("Android 11+: using SAF for media caching (notification-triggered)")
        }

        // Polling loop for deletion detection (reveals) — all Android versions
        startMediaPollingLoop()

        return START_STICKY
    }

    /**
     * Sets up FileObserver on ALL WhatsApp media folders.
     * Works on all Android versions — the path under Android/media/ is accessible.
     * FileObserver tells us exactly which file was created, so we copy just that one.
     */
    private fun setupFileObservers() {
        stopAllFileObservers()

        val mediaBase = "$WA_PATH/Media/"
        val wa = MyAppUtils.WA // "WhatsApp"

        val imagePath = "$mediaBase$wa Images/"
        val videoPath = "$mediaBase$wa Video/"
        val audioPath = "$mediaBase$wa Audio/"
        val docPath = "$mediaBase$wa Documents/"

        imageFileObserver = startObserverIfExists(imagePath, MediaBackupHelper.TYPE_IMAGES)
        videoFileObserver = startObserverIfExists(videoPath, MediaBackupHelper.TYPE_VIDEOS)
        audioFileObserver = startObserverIfExists(audioPath, MediaBackupHelper.TYPE_AUDIO)
        documentFileObserver = startObserverIfExists(docPath, MediaBackupHelper.TYPE_DOCUMENTS)
    }

    private fun startObserverIfExists(path: String, mediaType: String): FileObserver? {
        val dir = File(path)
        if (!dir.exists()) {
            Timber.d("FileObserver: %s does not exist, skipping", path)
            return null
        }
        val observer = createMediaObserver(path, mediaType)
        observer.startWatching()
        Timber.d("FileObserver started: [%s] %s", mediaType, path)
        return observer
    }


}