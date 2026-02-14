package com.catchyapps.whatsdelete.appclasseshelpers

import android.content.Context
import android.content.SharedPreferences
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.cleanerdata.Details
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_AUDIO_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_CHATS_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_DOCUMENT_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_IMAGES_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_VIDEO_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_VOICE_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppSchemas
import com.catchyapps.whatsdelete.basicapputils.MyAppShareModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import timber.log.Timber


class MyAppSharedPrefs(context: Context?) {
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null


    fun hSetFirstTime(isFirstRun: Boolean) {
        editor?.putBoolean(H_IS_FIRST_RUN, isFirstRun)
        editor?.commit()
    }

    fun hGetFirstTime(): Boolean? {
        return sharedPreferences?.getBoolean(H_IS_FIRST_RUN, true)
    }


    fun setPremium(flag: Boolean) {
        editor?.putBoolean("premium", flag)
        editor?.commit()
    }

    val isPremium: Boolean
        get() = sharedPreferences!!.getBoolean("premium", false)
    var `package`: String?
        get() = sharedPreferences!!.getString("selectedPackage", "sixMonth")
        set(flag) {
            editor?.putString("selectedPackage", flag)
            editor?.commit()
        }

    fun setShowHowWorkDialog() {
        editor?.putBoolean("showHowWorkDialog", true)
        editor?.commit()
    }

    val isShowHowWorkDialog: Boolean
        get() = sharedPreferences!!.getBoolean("showHowWorkDialog", false)
    var isSaveImage: Boolean
        get() = sharedPreferences!!.getBoolean("saveImage", true)
        set(saveImage) {
            editor?.putBoolean("saveImage", saveImage)
            editor?.commit()
        }
    var isSaveVideo: Boolean
        get() = sharedPreferences!!.getBoolean("saveVideo", true)
        set(saveVideo) {
            editor?.putBoolean("saveVideo", saveVideo)
            editor?.commit()
        }
    var isSaveVoiceNotes: Boolean
        get() = sharedPreferences!!.getBoolean("saveVoiceNotes", true)
        set(saveVoiceNotes) {
            editor?.putBoolean("saveVoiceNotes", saveVoiceNotes)
            editor?.commit()
        }


    fun setFirstTimeLanguageSelected(isLangSelected: Boolean) {
        editor?.putBoolean("firstTimeLanguageSelected", isLangSelected)
        editor?.commit()
    }

    fun getFirstTimeLanguageSelected(): Boolean? {
        return sharedPreferences?.getBoolean("firstTimeLanguageSelected", false)
    }


    var isSaveAudio: Boolean
        get() = sharedPreferences!!.getBoolean("saveAudio", true)
        set(saveAudio) {
            editor?.putBoolean("saveAudio", saveAudio)
            editor?.commit()
        }
    var isSaveDocument: Boolean
        get() = sharedPreferences!!.getBoolean("saveImage", true)
        set(saveDocument) {
            editor?.putBoolean("saveDocument", saveDocument)
            editor?.commit()
        }
    var isFirstTimeStatusDialog: Boolean
        get() = sharedPreferences!!.getBoolean("statusdialog", true)
        set(flag) {
            editor?.putBoolean("statusdialog", flag)
            editor?.commit()
        }

    fun <T> setList(key: String?, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(key, json)
    }

    operator fun set(key: String?, value: String?) {
        editor?.putString(key, value)
        editor?.commit()
    }

    private val hIsWhatsAppPermissionMap: MutableMap<String, Boolean>?
        get() {
            val string = sharedPreferences?.getString(H_WHATSAPP_PERMISSION_MAP, null)
            return if (string.isNullOrEmpty()) {
                null
            } else {
                val gson = GsonBuilder().setPrettyPrinting().create()
                val type = object : TypeToken<MutableMap<String, Boolean>>() {}.type
                return gson.fromJson(string, type)
            }
        }

    fun hSetWhatsAppPermission(schemas: MyAppSchemas, value: Boolean) {
        var hMap = hIsWhatsAppPermissionMap
        when {
            hMap.isNullOrEmpty() -> {
                hMap = mutableMapOf()
                hMap[schemas.toString()] = value
            }
            hMap.containsKey(schemas.toString()) -> {
                hMap.apply {
                    remove(schemas.toString())
                    put(
                        key = schemas.toString(),
                        value = value
                    )
                }
            }
            else -> {
                hMap[schemas.toString()] = value
            }
        }

        hSetWhatsAppPermissionMap(hMap)
    }

    private fun hSetWhatsAppPermissionMap(map: MutableMap<String, Boolean>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val type = object : TypeToken<MutableMap<String, Boolean>>() {}.type
        editor?.putString(H_WHATSAPP_PERMISSION_MAP, gson.toJson(map, type))
        editor?.commit()
    }

    fun hSavePagerList(list: List<MyAppShareModel>) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val type = object : TypeToken<List<MyAppShareModel>>() {}.type
        editor?.putString(H_PAGER_LIST, gson.toJson(list, type))
        editor?.commit()
    }



    fun hGetPagerList(context: Context): List<MyAppShareModel> {
        val string = sharedPreferences?.getString(H_PAGER_LIST, null)
        return if (string.isNullOrEmpty()) {
            hGetDefaultPagerList(context = context)
        } else {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val type = object : TypeToken<List<MyAppShareModel>>() {}.type
            return gson.fromJson(string, type)
        }
    }


    private fun hGetDefaultPagerList(context: Context): List<MyAppShareModel> {
        Timber.d("This list is returned")
        val hList = mutableListOf<MyAppShareModel>()
        hList.apply {
            add(
                MyAppShareModel(R.drawable.icon_chat,context.getString(H_CHATS_FRAGMENT), R.drawable.icon_drag_sequence)
            )
            add(
                MyAppShareModel(R.drawable.icon_document, context.getString(H_DOCUMENT_FRAGMENT), R.drawable.icon_drag_sequence)
            )
            add(
                MyAppShareModel(
                    R.drawable.icon_black_image,
                    context.getString(H_IMAGES_FRAGMENT),
                    R.drawable.icon_drag_sequence
                )
            )
            add(
                MyAppShareModel(R.drawable.icon_video, context.getString(H_VIDEO_FRAGMENT), R.drawable.icon_drag_sequence)
            )
            add(
                MyAppShareModel(R.drawable.icon_notes_voice,context.getString(H_VOICE_FRAGMENT), R.drawable.icon_drag_sequence)
            )
            add(
                MyAppShareModel(R.drawable.icon_audio, context.getString(H_AUDIO_FRAGMENT), R.drawable.icon_drag_sequence)
            )
        }
        return hList
    }


    companion object {
        private const val H_WHATSAPP_PERMISSION_MAP = "hWhatsAppPermissionMap"
        private const val H_PAGER_LIST = "hPagerList"
        private const val H_IS_FIRST_RUN = "hFirstRun"
        private const val H_DETAIL_ITEM = "detail item"
    }

    init {
        if (context != null) {
            sharedPreferences = context.getSharedPreferences("WADelete", Context.MODE_PRIVATE)
            editor = sharedPreferences?.edit()
            editor?.apply()
        }
    }

    fun setPermissionDialogFirstTime(isFirstRun: Boolean) {
        editor?.putBoolean("firstDialog", isFirstRun)
        editor?.commit()
    }

    fun getPermissionDialogFirstTime(): Boolean? {
        return sharedPreferences?.getBoolean("firstDialog", false)
    }

    fun saveDetailItem(toJson: String?) {

        editor?.putString(H_DETAIL_ITEM, toJson)
        editor?.commit()

    }

    fun getDetailItem(): Details? {

        val gson = GsonBuilder().setPrettyPrinting().create()

        val type = object : TypeToken<Details>() {}.type

        val gsonString = sharedPreferences?.getString(H_DETAIL_ITEM, null)

        return if (gsonString != null) {
            gson.fromJson(gsonString, type)
        } else {
            null
        }
    }

    fun clearDetailItem() {

        editor?.remove(H_DETAIL_ITEM)?.apply()

    }
}