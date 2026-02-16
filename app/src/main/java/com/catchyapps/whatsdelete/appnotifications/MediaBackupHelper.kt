package com.catchyapps.whatsdelete.appnotifications

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.provider.DocumentsContract
import androidx.annotation.RequiresApi
import android.webkit.MimeTypeMap
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.MIME_TYPE_IS_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.NO_MEDIA_DIRECTORY
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.hWhatAppMainUri
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans.Companion.treeUri
import com.catchyapps.whatsdelete.roomdb.appentities.EntityFiles
import timber.log.Timber
import java.io.File
import java.io.IOException

object MediaBackupHelper {

    private const val TAG = "MediaBackupHelper"
    private const val RECENT_WINDOW_MS = 5 * 60 * 1000L // 5 minutes

    const val TYPE_IMAGES = "images"
    const val TYPE_VIDEOS = "videos"
    const val TYPE_AUDIO = "audio"
    const val TYPE_DOCUMENTS = "documents"

    // WA folder name → our local type
    private data class MediaFolder(val waFolder: String, val localDir: String)
    private val ALL_MEDIA_FOLDERS = listOf(
        MediaFolder("WhatsApp Images", TYPE_IMAGES),
        MediaFolder("WhatsApp Video", TYPE_VIDEOS),
        MediaFolder("WhatsApp Audio", TYPE_AUDIO),
        MediaFolder("WhatsApp Documents", TYPE_DOCUMENTS)
    )

    // ══════════════════════════════════════════════
    // PRE-ANDROID 11: Called from FileObserver
    // ══════════════════════════════════════════════

    /**
     * FileObserver detected a new file. Copy it with .cache suffix.
     */
    fun cacheFile(context: Context, mediaType: String, sourceFile: File) {
        if (!sourceFile.exists() || !sourceFile.isFile) return
        val backupDir = getBackupDir(context, mediaType)
        val destFile = File(backupDir, "${sourceFile.name}.cache")
        if (destFile.exists()) return
        try {
            sourceFile.copyTo(destFile, overwrite = false)
            Timber.tag(TAG).d("Cached [%s]: %s", mediaType, sourceFile.name)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "Failed to cache %s", sourceFile.name)
            if (destFile.exists()) destFile.delete()
        }
    }

    // ══════════════════════════════════════════════
    // ANDROID 11+: Called from notification trigger (SAF)
    // ══════════════════════════════════════════════

    /**
     * Media notification received on Android 11+.
     * Scans ONLY the specific WA media folder matching [mediaType].
     * Caches files modified in the last 5 minutes. Also checks reveals.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun onMediaNotificationReceived(context: Context, mediaType: String? = null) {
        Timber.tag(TAG).d("onMediaNotificationReceived CALLED: mediaType=%s", mediaType ?: "null")
        if (!hasSafPermission(context)) {
            Timber.tag(TAG).w("No SAF permission, skipping media cache")
            return
        }
        val now = System.currentTimeMillis()
        val cutoff = now - RECENT_WINDOW_MS
        Timber.tag(TAG).d("SAF cache window: now=%d cutoff=%d (window=%dms)", now, cutoff, RECENT_WINDOW_MS)

        val foldersToScan = if (mediaType != null) {
            ALL_MEDIA_FOLDERS.filter { it.localDir == mediaType }
        } else {
            ALL_MEDIA_FOLDERS
        }
        Timber.tag(TAG).d("SAF scan: type=%s, folders=%d", mediaType ?: "all", foldersToScan.size)

        for (folder in foldersToScan) {
            try {
                Timber.tag(TAG).d("Scanning WA folder: '%s' → local dir '%s'", folder.waFolder, folder.localDir)
                val waFiles = getFilesWithTimestamps(context, folder.waFolder)
                Timber.tag(TAG).d("[%s] found %d files in WA folder", folder.localDir, waFiles.size)

                cacheRecentFilesSaf(context, folder, waFiles, cutoff)
                val revealed = revealDeletedFilesSaf(context, folder, waFiles.keys)
                Timber.tag(TAG).d("[%s] revealed=%d files", folder.localDir, revealed)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Error processing %s via SAF", folder.localDir)
            }
        }
        Timber.tag(TAG).d("onMediaNotificationReceived DONE")
    }

    /**
     * Polling on Android 11+: only check reveals via SAF.
     */
    /**
     * Returns total number of files revealed (deleted media detected).
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun checkRevealsSaf(context: Context): Int {
        Timber.tag(TAG).d("checkRevealsSaf called")
        if (!hasSafPermission(context)) {
            Timber.tag(TAG).w("checkRevealsSaf: NO SAF permission, returning 0")
            return 0
        }
        var totalRevealed = 0
        for (folder in ALL_MEDIA_FOLDERS) {
            try {
                val waFileNames = getFileNames(context, folder.waFolder)
                totalRevealed += revealDeletedFilesSaf(context, folder, waFileNames)
            } catch (e: Exception) {
                Timber.tag(TAG).w(e, "Error checking SAF reveals in %s", folder.localDir)
            }
        }
        return totalRevealed
    }

    // ══════════════════════════════════════════════
    // PRE-ANDROID 11: Reveal using direct File access
    // ══════════════════════════════════════════════

    /**
     * Returns total number of files revealed.
     */
    fun checkReveals(context: Context, waMediaBasePath: String): Int {
        var total = 0
        total += revealForType(context, TYPE_IMAGES, File(waMediaBasePath, "WhatsApp Images"))
        total += revealForType(context, TYPE_VIDEOS, File(waMediaBasePath, "WhatsApp Video"))
        total += revealForType(context, TYPE_AUDIO, File(waMediaBasePath, "WhatsApp Audio"))
        total += revealForType(context, TYPE_DOCUMENTS, File(waMediaBasePath, "WhatsApp Documents"))
        return total
    }

    private fun revealForType(context: Context, mediaType: String, waDir: File): Int {
        val backupDir = getBackupDir(context, mediaType)
        val cachedFiles = backupDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".cache") }
            ?: return 0

        var count = 0
        for (cachedFile in cachedFiles) {
            val originalName = cachedFile.name.removeSuffix(".cache")
            if (!existsInDirOrSubdirs(waDir, originalName)) {
                val revealedFile = File(backupDir, originalName)
                val renamed = cachedFile.renameTo(revealedFile)
                if (renamed) {
                    count++
                    Timber.tag(TAG).d("Revealed [%s]: %s (deleted from WA)",
                        mediaType, originalName)
                }
            }
        }
        return count
    }

    private fun existsInDirOrSubdirs(dir: File, fileName: String): Boolean {
        if (!dir.exists()) return false
        if (File(dir, fileName).exists()) return true
        dir.listFiles()?.forEach { sub ->
            if (sub.isDirectory && !sub.name.equals("Sent", ignoreCase = true)) {
                if (File(sub, fileName).exists()) return true
            }
        }
        return false
    }

    // ══════════════════════════════════════════════
    // Fragment API
    // ══════════════════════════════════════════════

    fun getRevealedFiles(context: Context, mediaType: String): List<File> {
        val dir = getBackupDir(context, mediaType)
        return dir.listFiles()
            ?.filter { it.isFile && !it.name.endsWith(".cache") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    fun getRevealedFilesAsEntities(
        context: Context,
        mediaType: String
    ): MutableList<EntityFiles> {
        return getRevealedFiles(context, mediaType).map { file ->
            EntityFiles().apply {
                title = file.name
                filePath = file.absolutePath
                fileUri = file.absolutePath
                timeStamp = file.lastModified().toString()
                mimeType = getMimeTypeFromName(file.name)
                fileSize = file.length()
            }
        }.toMutableList()
    }

    private fun getMimeTypeFromName(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            ?: "application/octet-stream"
    }

    fun getBackupDir(context: Context, mediaType: String): File {
        val baseDir = context.getDir("wa_recover", Context.MODE_PRIVATE)
        val dir = File(baseDir, mediaType)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // ══════════════════════════════════════════════
    // SAF internals (Android 11+)
    // ══════════════════════════════════════════════

    @RequiresApi(Build.VERSION_CODES.R)
    private fun cacheRecentFilesSaf(
        context: Context,
        folder: MediaFolder,
        waFiles: Map<String, Long>,
        cutoff: Long
    ) {
        val backupDir = getBackupDir(context, folder.localDir)
        val resolver = context.contentResolver

        val backedUpNames = backupDir.listFiles()
            ?.filter { it.isFile }
            ?.map { it.name.removeSuffix(".cache") }
            ?.toSet() ?: emptySet()

        var newCount = 0
        var skippedOld = 0
        var skippedBacked = 0

        for ((name, lastModified) in waFiles) {
            if (backedUpNames.contains(name)) {
                skippedBacked++
                continue
            }
            // If lastModified is 0, SAF didn't provide it — don't skip (treat as recent)
            if (lastModified > 0 && lastModified < cutoff) {
                skippedOld++
                continue
            }

            newCount++
            Timber.tag(TAG).d("[%s] Caching: %s (modified=%d)", folder.localDir, name, lastModified)
            val safFile = findFileByName(resolver, folder.waFolder, name)
            if (safFile != null) {
                val destFile = File(backupDir, "${name}.cache")
                if (!destFile.exists()) {
                    val copied = copyFromSaf(resolver, safFile.uri, destFile)
                    if (copied) {
                        Timber.tag(TAG).d("SAF Cached [%s]: %s", folder.localDir, name)
                    }
                }
            }
        }
        Timber.tag(TAG).d("[%s] Summary: %d new, %d skipped(old), %d skipped(backed)",
            folder.localDir, newCount, skippedOld, skippedBacked)
    }

    private fun revealDeletedFilesSaf(
        context: Context,
        folder: MediaFolder,
        waFileNames: Set<String>
    ): Int {
        val backupDir = getBackupDir(context, folder.localDir)
        val cachedFiles = backupDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".cache") }
            ?: return 0

        var count = 0
        for (cachedFile in cachedFiles) {
            val originalName = cachedFile.name.removeSuffix(".cache")
            if (!waFileNames.contains(originalName)) {
                val revealedFile = File(backupDir, originalName)
                val renamed = cachedFile.renameTo(revealedFile)
                if (renamed) {
                    count++
                    Timber.tag(TAG).d("SAF Revealed [%s]: %s (deleted from WA)",
                        folder.localDir, originalName)
                }
            }
        }
        return count
    }

    // ── SAF: scan files with timestamps ──

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getFilesWithTimestamps(
        context: Context,
        waFolderName: String
    ): Map<String, Long> {
        val docId = "$hWhatAppMainUri/$waFolderName"
        val resolver = context.contentResolver
        val files = mutableMapOf<String, Long>()
        collectFilesWithTime(resolver, docId, files)
        collectSubfoldersWithTime(resolver, docId, files)
        return files
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getFileNames(
        context: Context,
        waFolderName: String
    ): Set<String> {
        val docId = "$hWhatAppMainUri/$waFolderName"
        val resolver = context.contentResolver
        val names = mutableSetOf<String>()
        collectNames(resolver, docId, names)
        collectSubfolderNames(resolver, docId, names)
        return names
    }

    private fun collectFilesWithTime(
        resolver: ContentResolver, parentDocId: String,
        files: MutableMap<String, Long>
    ) {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ), null, null, null)
        } catch (e: Exception) { return }

        cursor?.use {
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val modCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
            while (it.moveToNext()) {
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                val mod = it.getLong(modCol)
                if (mime != MIME_TYPE_IS_DIRECTORY && mime != NO_MEDIA_DIRECTORY) {
                    files[name] = mod
                }
            }
        }
    }

    private fun collectSubfoldersWithTime(
        resolver: ContentResolver, parentDocId: String,
        files: MutableMap<String, Long>
    ) {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ), null, null, null)
        } catch (e: Exception) { return }

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val subId = it.getString(idCol)
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                if (mime == MIME_TYPE_IS_DIRECTORY && !name.equals("Sent", ignoreCase = true)) {
                    collectFilesWithTime(resolver, subId, files)
                }
            }
        }
    }

    private fun collectNames(
        resolver: ContentResolver, parentDocId: String,
        names: MutableSet<String>
    ) {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ), null, null, null)
        } catch (e: Exception) { return }

        cursor?.use {
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                if (mime != MIME_TYPE_IS_DIRECTORY && mime != NO_MEDIA_DIRECTORY) {
                    names.add(name)
                }
            }
        }
    }

    private fun collectSubfolderNames(
        resolver: ContentResolver, parentDocId: String,
        names: MutableSet<String>
    ) {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ), null, null, null)
        } catch (e: Exception) { return }

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val subId = it.getString(idCol)
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                if (mime == MIME_TYPE_IS_DIRECTORY && !name.equals("Sent", ignoreCase = true)) {
                    collectNames(resolver, subId, names)
                }
            }
        }
    }

    // ── Find file by name (SAF) ──

    private data class SafFile(val name: String, val docId: String) {
        val uri get() = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
    }

    private fun findFileByName(
        resolver: ContentResolver, waFolderName: String, targetName: String
    ): SafFile? {
        val docId = "$hWhatAppMainUri/$waFolderName"
        return findInFolder(resolver, docId, targetName)
            ?: findInSubfolders(resolver, docId, targetName)
    }

    private fun findInFolder(
        resolver: ContentResolver, parentDocId: String, targetName: String
    ): SafFile? {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ), null, null, null)
        } catch (e: Exception) { return null }

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val fileDocId = it.getString(idCol)
                val fileName = it.getString(nameCol)
                val mimeType = it.getString(mimeCol)
                if (mimeType == MIME_TYPE_IS_DIRECTORY || mimeType == NO_MEDIA_DIRECTORY) continue
                if (fileName == targetName) return SafFile(fileName, fileDocId)
            }
        }
        return null
    }

    private fun findInSubfolders(
        resolver: ContentResolver, parentDocId: String, targetName: String
    ): SafFile? {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val cursor = try {
            resolver.query(uri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ), null, null, null)
        } catch (e: Exception) { return null }

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            while (it.moveToNext()) {
                val subId = it.getString(idCol)
                val name = it.getString(nameCol)
                val mime = it.getString(mimeCol)
                if (mime == MIME_TYPE_IS_DIRECTORY && !name.equals("Sent", ignoreCase = true)) {
                    val found = findInFolder(resolver, subId, targetName)
                    if (found != null) return found
                }
            }
        }
        return null
    }

    // ── Copy from SAF ──

    private fun copyFromSaf(
        resolver: ContentResolver, sourceUri: android.net.Uri, destFile: File
    ): Boolean {
        return try {
            resolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: IOException) {
            Timber.tag(TAG).w("Copy failed: %s", e.message)
            if (destFile.exists()) destFile.delete()
            false
        } catch (e: SecurityException) {
            Timber.tag(TAG).w("Permission denied: %s", e.message)
            false
        }
    }

    private fun hasSafPermission(context: Context): Boolean {
        val perms = context.contentResolver.persistedUriPermissions
        Timber.tag(TAG).d("SAF_CHECK: treeUri=%s", treeUri)
        Timber.tag(TAG).d("SAF_CHECK: persisted permissions count=%d", perms.size)
        for (p in perms) {
            Timber.tag(TAG).d("SAF_CHECK: perm uri=%s read=%b match=%b", p.uri, p.isReadPermission, p.uri == treeUri)
        }
        val has = perms.any { it.uri == treeUri && it.isReadPermission }
        Timber.tag(TAG).d("SAF_CHECK: result=%b", has)
        return has
    }
}
