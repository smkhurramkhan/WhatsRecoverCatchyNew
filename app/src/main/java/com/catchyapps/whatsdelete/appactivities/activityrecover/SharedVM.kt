package com.catchyapps.whatsdelete.appactivities.activityrecover

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.catchyapps.whatsdelete.BaseApplication
import com.catchyapps.whatsdelete.appclasseshelpers.MyAppSharedPrefs
import com.catchyapps.whatsdelete.basicapputils.MyAppShareModel
import com.catchyapps.whatsdelete.appactivities.activitywhatscleaner.CleanerConstans
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recoverchat.FragmentChatRecover
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reoverdoc.FragmentRecoverDoc
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.recovermedia.RecoverMediaFragment
import com.catchyapps.whatsdelete.appactivities.activityrecover.recoverfragments.recovermainpager.reovervoice.VoiceMediaFragment
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_AUDIO_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_CHATS_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_DOCUMENT_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_IMAGES_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_IS_AUDIO
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_IS_IMAGE
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_VIDEO_FRAGMENT
import com.catchyapps.whatsdelete.basicapputils.MyAppConstants.H_VOICE_FRAGMENT

class SharedVM(application: Application) : AndroidViewModel(application) {

    private val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    private val ANDROID_DOCID = "primary:Android/media/com.whatsapp/WhatsApp/Media"
    var uri: Uri? = DocumentsContract.buildDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        ANDROID_DOCID
    )
    private var mApplication:Application

    private val hAppSharedPrefs = MyAppSharedPrefs(getApplication())
    private val hViewStateSharedMLD = MutableLiveData<ViewStateShared>()
    val hSharedViewStateLD = liveData {
        emitSource(hViewStateSharedMLD)
    }


    private var hFragmentOrderList: List<MyAppShareModel>

    init {
        mApplication= application
        hFragmentOrderList = hAppSharedPrefs.hGetPagerList(application)
    }


    fun hSetFirstTime(value: Boolean) {
        hAppSharedPrefs.hSetFirstTime(value)

    }

    private fun hAddFragment(
        hFragment: Fragment,
        hName: String,
        hBundle: Bundle? = null,
        hPagerFragmentList: MutableList<Fragment>,
        hFragmentNamesList: MutableList<String>,
    ) {
        hFragment.arguments = hBundle

        hPagerFragmentList.add(hFragment)
        hFragmentNamesList.add(hName)
    }

    fun hGetPagerList() {
        val hFragmentNamesList = mutableListOf<String>()
        val hPagerFragmentList = mutableListOf<Fragment>()
        hFragmentOrderList.forEach {
            when (it.title) {
                mApplication.getString(H_CHATS_FRAGMENT) -> {
                    hAddFragment(
                        hFragment = FragmentChatRecover(),
                        hName = mApplication.getString(H_CHATS_FRAGMENT),
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
                mApplication.getString(H_DOCUMENT_FRAGMENT) -> {
                    hAddFragment(
                        hFragment = FragmentRecoverDoc(),
                        hName =  mApplication.getString(H_DOCUMENT_FRAGMENT),
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
                mApplication.getString(H_IMAGES_FRAGMENT) -> {
                    hAddFragment(
                        hFragment = RecoverMediaFragment(),
                        hName = mApplication.getString(H_IMAGES_FRAGMENT),
                        hBundle = Bundle().also {
                            it.putBoolean(H_IS_IMAGE, true)
                        },
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
                mApplication.getString(H_VIDEO_FRAGMENT) -> {
                    hAddFragment(
                        hFragment = RecoverMediaFragment(),
                        hName = mApplication.getString(
                            H_VIDEO_FRAGMENT),
                        hBundle = Bundle().also {
                            it.putBoolean(H_IS_IMAGE, false)
                        },
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
                mApplication.getString(H_VOICE_FRAGMENT) -> {

                    hAddFragment(
                        hFragment = VoiceMediaFragment(),
                        hName = mApplication.getString(H_VOICE_FRAGMENT),
                        hBundle = Bundle().also {
                            it.putBoolean(H_IS_AUDIO, false)
                        },
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
                mApplication.getString(H_AUDIO_FRAGMENT) -> {
                    hAddFragment(
                        hFragment = VoiceMediaFragment(),
                        hName = mApplication.getString(H_AUDIO_FRAGMENT),
                        hBundle = Bundle().also {
                            it.putBoolean(H_IS_AUDIO, true)
                        },
                        hPagerFragmentList = hPagerFragmentList,
                        hFragmentNamesList = hFragmentNamesList
                    )
                }
            }
        }


        hViewStateSharedMLD.postValue(
            ViewStateShared.OnUpdatePager(
                hFragmentNamesList = hFragmentNamesList,
                hPagerFragmentsList = hPagerFragmentList

            )
        )
    }

    fun hLaunchIntent(intentTypes: TypesIntent) {
        val hOnLaunchIntent = ViewStateShared.OnLaunchIntent()
        when (intentTypes) {
            TypesIntent.H_SETTINGS -> {
                hOnLaunchIntent.hIsLaunchSettingsIntent = true
            }
            TypesIntent.H_URI -> {
                hOnLaunchIntent.hIsLaunchUriIntent = true
            }
        }
        hViewStateSharedMLD.value = hOnLaunchIntent
    }

    fun hUpdateSettings(
        intentTypes: TypesIntent,
        result: ActivityResult? = null,
    ) {
        when (intentTypes) {
            TypesIntent.H_SETTINGS -> {
                hUpdatePager()
            }
            TypesIntent.H_URI -> {
                hUpdateUriSettings(result)
            }
        }


    }

    private fun hUpdateUriSettings(result: ActivityResult?) {

        val takeFlags = result?.data?.flags?.and(
            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        )!!
        result.data?.data?.let { hDirectoryUri ->
            getApplication<BaseApplication>().contentResolver.takePersistableUriPermission(
                hDirectoryUri,
                takeFlags
            )

            hAppSharedPrefs.hSetWhatsAppPermission(
                schemas = CleanerConstans.hGetSchemaTypeFromUri(hDirectoryUri)!!,
                value = true
            )

            hViewStateSharedMLD.value = ViewStateShared.OnUpdateDocs
        }
    }

    private fun hUpdatePager() {
        val hUpdatedList = hAppSharedPrefs.hGetPagerList(mApplication)
        var hCount = 0
        hUpdatedList.forEachIndexed { index, shareModel ->
            if (shareModel.title == hFragmentOrderList[index].title) {
                hCount++
            }
        }
        if (hCount != hUpdatedList.size) {
            hFragmentOrderList = hUpdatedList
            hGetPagerList()
        }
    }

    fun getPositionOfList(name: String?): Int {

        var indexOf = 0
        val hFragmentNamesList = hAppSharedPrefs.hGetPagerList(mApplication)


        hFragmentNamesList.find {
            it.title == name
        }.also {
            indexOf = hFragmentNamesList.indexOf(it)
        }

        return indexOf
    }

}