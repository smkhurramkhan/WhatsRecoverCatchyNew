package com.catchyapps.whatsdelete.appactivities.activitywhatscleaner

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppUtils
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.CleanerItemPath
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas

class CleanerConstans {
    companion object {
        const val H_IMAGE = "Images"
        const val H_DOCUMENTS = "Documents"
        const val H_VIDEOS = "Videos"
        const val H_AUDIO = "Audio files"
        const val H_WALLPAPERS = "Wallpapers"
        const val H_GIFS = "GIFs"
        const val H_VOICE = "Voice files"
        const val H_STATUS = "Statuses"
        const val H_CLEANER_IC = "hDetailsItem"
        const val H_FILES_IC = "hFilesDetail"
        const val H_POSITION = "hPosition"

        const val MIME_TYPE_IS_DIRECTORY = "vnd.android.document/directory"
        const val NO_MEDIA_DIRECTORY = "application/octet-stream"

        const val hWhatAppMainUri =
            "primary:Android/media/com.whatsapp/WhatsApp/Media"

        val hFilesToExclude = arrayOf("Sent", "Private", ".nomedia")

        const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"

        var treeUri: Uri = DocumentsContract.buildTreeDocumentUri(
            EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
            hWhatAppMainUri
        )

        var uri: Uri = DocumentsContract.buildDocumentUri(
            EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
            hWhatAppMainUri
        )

        //            "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2FWallPaper%2FSent"
        const val hWhatsAppStatusUri = "$hWhatAppMainUri/.Statuses"

        const val hWhatsAppImageUri = "$hWhatAppMainUri/WhatsApp Images"
        const val hWhatsAppSentImageUri = "$hWhatsAppImageUri/Sent"

        const val hWhatsAppDocsUri = "$hWhatAppMainUri/WhatsApp Documents"
        const val hWhatsAppDocsSentUri = "$hWhatsAppDocsUri/Sent"

        const val hWhatsAppVideoUri = "$hWhatAppMainUri/WhatsApp Video"
        const val hWhatsAppVideoSentUri = "$hWhatsAppVideoUri/Sent"

        const val hWhatsAppVoiceUri = "$hWhatAppMainUri/WhatsApp Voice Notes"
        const val hWhatsAppVoiceSentUri = "$hWhatsAppVoiceUri/Sent"

        const val hWhatsAppAudioUri = "$hWhatAppMainUri/WhatsApp Audio"
        const val hWhatsAppAudioSentUri = "$hWhatsAppAudioUri/Sent"

        const val hWhatsAppWallPaperUri = "$hWhatAppMainUri/WallPaper"
        const val hWhatsAppWallPaperSentUri = "$hWhatsAppWallPaperUri/Sent"

        const val hWhatsAppGifUri = "$hWhatAppMainUri/WhatsApp Animated Gifs"
        const val hWhatsAppGifSentUri = "$hWhatsAppGifUri/Sent"


        fun hGetSchemaTypeFromUri(uri: Uri): MyAppSchemas? {
            return when (uri.toString()) {
                hWhatsAppDocsUri -> MyAppSchemas.H_DOCUMENT_TYPE
                hWhatsAppStatusUri -> MyAppSchemas.H_STATUS_TYPE
                hWhatsAppDocsSentUri -> MyAppSchemas.H_DOCUMENT_TYPE_SENT
                hWhatsAppSentImageUri -> MyAppSchemas.H_IMAGE_TYPE_SENT
                hWhatsAppImageUri -> MyAppSchemas.H_IMAGE_TYPE
                hWhatsAppVideoUri -> MyAppSchemas.H_VIDEO_TYPE
                hWhatsAppVideoSentUri -> MyAppSchemas.H_VIDEO_TYPE_SENT
                hWhatsAppAudioUri -> MyAppSchemas.H_AUDIO_TYPE
                hWhatsAppAudioSentUri -> MyAppSchemas.H_AUDIO_TYPE_SENT
                hWhatsAppVoiceUri -> MyAppSchemas.H_VOICE_TYPE
                hWhatsAppVoiceSentUri -> MyAppSchemas.H_VOICE_TYPE_SENT
                hWhatsAppWallPaperUri -> MyAppSchemas.H_WALLPAPER_TYPE
                hWhatsAppWallPaperSentUri -> MyAppSchemas.H_WALLPAPER_SENT_TYPE
                hWhatsAppGifUri -> MyAppSchemas.H_GIFS_TYPE
                hWhatsAppGifSentUri -> MyAppSchemas.H_GIFS_SENT_TYPE
                else -> null
            }
        }

        fun hGetUriFromSchema(schemas: MyAppSchemas): Uri {
            return when (schemas) {
                MyAppSchemas.H_DOCUMENT_TYPE -> Uri.parse(
                    hWhatsAppDocsUri
                )
                MyAppSchemas.H_STATUS_TYPE -> Uri.parse(
                    hWhatsAppStatusUri
                )
                MyAppSchemas.H_DOCUMENT_TYPE_SENT -> Uri.parse(
                    hWhatsAppDocsSentUri
                )
                MyAppSchemas.H_IMAGE_TYPE_SENT -> Uri.parse(
                    hWhatsAppSentImageUri
                )
                MyAppSchemas.H_IMAGE_TYPE -> Uri.parse(
                    hWhatsAppImageUri
                )
                MyAppSchemas.H_VIDEO_TYPE -> Uri.parse(
                    hWhatsAppVideoUri
                )
                MyAppSchemas.H_VIDEO_TYPE_SENT -> Uri.parse(
                    hWhatsAppVideoSentUri
                )
                MyAppSchemas.H_AUDIO_TYPE -> Uri.parse(
                    hWhatsAppAudioUri
                )
                MyAppSchemas.H_AUDIO_TYPE_SENT -> Uri.parse(
                    hWhatsAppAudioSentUri
                )
                MyAppSchemas.H_VOICE_TYPE -> Uri.parse(
                    hWhatsAppVoiceUri
                )
                MyAppSchemas.H_VOICE_TYPE_SENT -> Uri.parse(
                    hWhatsAppVoiceSentUri
                )
                MyAppSchemas.H_WALLPAPER_TYPE -> Uri.parse(
                    hWhatsAppWallPaperUri
                )
                MyAppSchemas.H_WALLPAPER_SENT_TYPE -> Uri.parse(
                    hWhatsAppWallPaperSentUri
                )
                MyAppSchemas.H_GIFS_TYPE -> Uri.parse(
                    hWhatsAppGifUri
                )
                MyAppSchemas.H_GIFS_SENT_TYPE -> Uri.parse(
                    hWhatsAppGifSentUri
                )
            }
        }

        fun hSetPathForFilesDetails(hPostion: Int, hTitle: String): CleanerItemPath? {
            return when (hPostion) {
                0 -> {
                    hGetZeroIndexPath(hTitle)
                }
                1 -> {
                    hSetIstIndexPath(hTitle)
                }
                else -> null
            }
        }

        private fun hSetIstIndexPath(hTitle: String): CleanerItemPath? {
            return when (hTitle) {
                H_IMAGE -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_IMAGE_TYPE_SENT).toString(),
                            hMyAppSchemas = MyAppSchemas.H_IMAGE_TYPE_SENT
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetImagesPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_IMAGE_TYPE_SENT
                        )
                }
                H_DOCUMENTS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_DOCUMENT_TYPE_SENT).toString(),
                            hMyAppSchemas = MyAppSchemas.H_DOCUMENT_TYPE_SENT
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetDocPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_DOCUMENT_TYPE_SENT
                        )
                }
                H_VIDEOS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_VIDEO_TYPE_SENT).toString(),
                            hMyAppSchemas = MyAppSchemas.H_VIDEO_TYPE_SENT
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetVideoPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_VIDEO_TYPE_SENT
                        )
                }
                H_AUDIO -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_AUDIO_TYPE_SENT).toString(),
                            hMyAppSchemas = MyAppSchemas.H_AUDIO_TYPE_SENT
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetAudioPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_AUDIO_TYPE_SENT
                        )

                }
                H_WALLPAPERS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_WALLPAPER_SENT_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_WALLPAPER_SENT_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetWallpaperPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_WALLPAPER_SENT_TYPE
                        )
                }
                H_GIFS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_GIFS_SENT_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_GIFS_SENT_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetGifsPath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_GIFS_SENT_TYPE
                        )
                }
                H_VOICE -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_VOICE_TYPE_SENT).toString(),
                            hMyAppSchemas = MyAppSchemas.H_VOICE_TYPE_SENT
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = "$hGetVoicePath/Sent",
                            hMyAppSchemas = MyAppSchemas.H_VOICE_TYPE_SENT
                        )
                }
                H_STATUS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_STATUS_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_STATUS_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetStatusPath,
                            hMyAppSchemas = MyAppSchemas.H_STATUS_TYPE
                        )
                }
                else -> null
            }
        }

        fun hCheck11thUpSdk(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }

        private fun hGetZeroIndexPath(hTitle: String): CleanerItemPath? {
            return when (hTitle) {
                H_IMAGE -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_IMAGE_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_IMAGE_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetImagesPath,
                            hMyAppSchemas = MyAppSchemas.H_IMAGE_TYPE
                        )
                }
                H_DOCUMENTS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_DOCUMENT_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_DOCUMENT_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetDocPath,
                            hMyAppSchemas = MyAppSchemas.H_DOCUMENT_TYPE
                        )
                }
                H_VIDEOS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_VIDEO_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_VIDEO_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetVideoPath,
                            hMyAppSchemas = MyAppSchemas.H_VIDEO_TYPE
                        )
                }
                H_AUDIO -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_AUDIO_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_AUDIO_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetAudioPath,
                            hMyAppSchemas = MyAppSchemas.H_AUDIO_TYPE
                        )

                }
                H_WALLPAPERS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_WALLPAPER_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_WALLPAPER_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetWallpaperPath,
                            hMyAppSchemas = MyAppSchemas.H_WALLPAPER_TYPE
                        )
                }
                H_GIFS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_GIFS_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_GIFS_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetGifsPath,
                            hMyAppSchemas = MyAppSchemas.H_GIFS_TYPE
                        )
                }
                H_VOICE -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_VOICE_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_VOICE_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetVoicePath,
                            hMyAppSchemas = MyAppSchemas.H_VOICE_TYPE
                        )
                }
                H_STATUS -> {
                    if (hCheck11thUpSdk())
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetUriFromSchema(MyAppSchemas.H_STATUS_TYPE).toString(),
                            hMyAppSchemas = MyAppSchemas.H_STATUS_TYPE
                        )
                    else
                        CleanerItemPath(
                            hType = hTitle,
                            hPath = hGetStatusPath,
                            hMyAppSchemas = MyAppSchemas.H_STATUS_TYPE
                        )
                }
                else -> null
            }
        }

        fun hGetCleanerPathList(): List<CleanerItemPath> {
            val hList = mutableListOf<CleanerItemPath>()
            return hList.apply {
                add(hSetPathForFilesDetails(0, H_IMAGE)!!)
                add(hSetPathForFilesDetails(1, H_IMAGE)!!)

                add(hSetPathForFilesDetails(0, H_DOCUMENTS)!!)
                add(hSetPathForFilesDetails(1, H_DOCUMENTS)!!)

                add(hSetPathForFilesDetails(0, H_VIDEOS)!!)
                add(hSetPathForFilesDetails(1, H_VIDEOS)!!)


                add(hSetPathForFilesDetails(0, H_AUDIO)!!)
                add(hSetPathForFilesDetails(1, H_AUDIO)!!)

                add(hSetPathForFilesDetails(0, H_WALLPAPERS)!!)
                add(hSetPathForFilesDetails(1, H_WALLPAPERS)!!)


                add(hSetPathForFilesDetails(0, H_GIFS)!!)
                add(hSetPathForFilesDetails(1, H_GIFS)!!)

                add(hSetPathForFilesDetails(0, H_VOICE)!!)
                add(hSetPathForFilesDetails(1, H_VOICE)!!)

                add(hSetPathForFilesDetails(0, H_STATUS)!!)
                add(hSetPathForFilesDetails(1, H_STATUS)!!)

            }
        }

        fun hGetDialogText(
            schemas: MyAppSchemas,
            context: Context
        ): String {
            val hEndString = context.getString(R.string.rest_assured_permission_text)
            return when (schemas) {
                MyAppSchemas.H_IMAGE_TYPE -> {
                    context.getString(R.string.image_permission) + hEndString
                }
                MyAppSchemas.H_STATUS_TYPE -> {
                    context.getString(R.string.status_permission) + hEndString
                }
                MyAppSchemas.H_DOCUMENT_TYPE -> {
                    context.getString(R.string.doc_permission) + hEndString
                }
                MyAppSchemas.H_DOCUMENT_TYPE_SENT -> {
                    context.getString(R.string.doc_sent_permission) + hEndString
                }
                MyAppSchemas.H_IMAGE_TYPE_SENT -> {
                    context.getString(R.string.image_sent_permission) + hEndString
                }
                MyAppSchemas.H_VIDEO_TYPE -> {
                    context.getString(R.string.video_permission) + hEndString
                }
                MyAppSchemas.H_VIDEO_TYPE_SENT -> {
                    context.getString(R.string.video_sent_permission) + hEndString
                }
                MyAppSchemas.H_AUDIO_TYPE -> {
                    context.getString(R.string.audio_permission) + hEndString
                }
                MyAppSchemas.H_AUDIO_TYPE_SENT -> {
                    context.getString(R.string.audio_sent_permission) + hEndString
                }
                MyAppSchemas.H_VOICE_TYPE -> {
                    context.getString(R.string.voice_permission) + hEndString
                }
                MyAppSchemas.H_VOICE_TYPE_SENT -> {
                    context.getString(R.string.voice_sent_permission) + hEndString
                }
                MyAppSchemas.H_WALLPAPER_TYPE -> {
                    context.getString(R.string.wallpaper_permission) + hEndString
                }
                MyAppSchemas.H_WALLPAPER_SENT_TYPE -> {
                    context.getString(R.string.wallpaper_sent_permission) + hEndString
                }
                MyAppSchemas.H_GIFS_TYPE -> {
                    context.getString(R.string.gif_permission) + hEndString
                }
                MyAppSchemas.H_GIFS_SENT_TYPE -> {
                    context.getString(R.string.gif_sent_permission) + hEndString
                }
            }

        }

        fun hGetAllSchemes(): List<MyAppSchemas> {
            val hList = mutableListOf<MyAppSchemas>()
            hList.apply {
                add(MyAppSchemas.H_STATUS_TYPE)
                add(MyAppSchemas.H_DOCUMENT_TYPE)
                add(MyAppSchemas.H_DOCUMENT_TYPE_SENT)
                add(MyAppSchemas.H_IMAGE_TYPE_SENT)
                add(MyAppSchemas.H_IMAGE_TYPE)
                add(MyAppSchemas.H_VIDEO_TYPE)
                add(MyAppSchemas.H_VIDEO_TYPE_SENT)
                add(MyAppSchemas.H_AUDIO_TYPE)
                add(MyAppSchemas.H_AUDIO_TYPE_SENT)
                add(MyAppSchemas.H_VOICE_TYPE)
                add(MyAppSchemas.H_VOICE_TYPE_SENT)
                add(MyAppSchemas.H_WALLPAPER_TYPE)
                add(MyAppSchemas.H_WALLPAPER_SENT_TYPE)
                add(MyAppSchemas.H_GIFS_TYPE)
                add(MyAppSchemas.H_GIFS_SENT_TYPE)
            }
            return hList.toList()

        }


        private val H_DOC_PATH = "${MyAppUtils.whatsappPath}/Media/WhatsApp Documents"
        private val H_AUDIO_PATH = "${MyAppUtils.whatsappPath}/Media/WhatsApp Audio"
        private val H_VOICE_PATH = "${MyAppUtils.whatsappPath}/Media/WhatsApp Voice Notes"

        private val H_IMAGES_PATH = "${MyAppUtils.whatsappPath}/Media/WhatsApp Images"
        private val H_VIDEO_PATH = "${MyAppUtils.whatsappPath}/Media/WhatsApp Video"
        private val H_STATUS_PATH = "${MyAppUtils.whatsappPath}/Media/.Statuses"
        private val H_WALLPAPER_PATH = "${MyAppUtils.whatsappPath}/Media/WallPaper"
        private val H_GIFS_PATH = "${MyAppUtils.whatsappPath}//Media/WhatsApp Animated Gifs"

        val hGetAudioPath
            get() = H_AUDIO_PATH

        val hGetDocPath
            get() = H_DOC_PATH

        val hGetVoicePath
            get() = H_VOICE_PATH

        val hGetImagesPath
            get() = H_IMAGES_PATH

        val hGetVideoPath
            get() = H_VIDEO_PATH

        val hGetStatusPath
            get() = H_STATUS_PATH

        val hGetWallpaperPath
            get() = H_WALLPAPER_PATH

        val hGetGifsPath
            get() = H_GIFS_PATH
    }


}