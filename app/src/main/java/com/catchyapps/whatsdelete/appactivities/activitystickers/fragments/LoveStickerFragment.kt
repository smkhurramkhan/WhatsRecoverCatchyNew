package com.catchyapps.whatsdelete.appactivities.activitystickers.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.catchyapps.whatsdelete.BuildConfig
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickeradapter.AdapterSingleStickerCategory
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.DescSticker
import com.catchyapps.whatsdelete.basicapputils.MyAppDataUtils
import com.catchyapps.whatsdelete.databinding.FragmentLoveStickerBinding
import timber.log.Timber

class LoveStickerFragment : Fragment() {
    lateinit var binding : FragmentLoveStickerBinding

    private var stickersList = listOf<DescSticker>()
    private var stickerLauncher: ActivityResultLauncher<Intent>? = null


    val EXTRA_STICKER_PACK_ID = "sticker_pack_id"
    val EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority"
    val EXTRA_STICKER_PACK_NAME = "sticker_pack_name"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val validationError = data.getStringExtra("validation_error")
                    if (validationError != null) {
                        if (BuildConfig.DEBUG) {
                            // Validation error should be shown to the developer only, not users.
                            Timber.d("validation error")
                        }
                        Timber.e( "Validation failed:$validationError")
                    }
                } else {
                    Timber.d("stickers pack not added")
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoveStickerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        stickersList = MyAppDataUtils.loadStickersSingleList(requireActivity().assets, "Love")
        setUpStickersRV()
    }

    private fun setUpStickersRV() {

        val stickersAdapter = AdapterSingleStickerCategory(requireContext(),
            onAddStickerClick = {stickerItem->
                addStickerPackToWhatsApp(
                    stickerItem
                )
            }
            ,
            stickersList)

        binding.rvStickers.adapter = stickersAdapter
        binding.rvStickers.layoutManager =
            GridLayoutManager(requireContext(), 3)
    }

    private fun addStickerPackToWhatsApp(sp: DescSticker) {
        val intent = Intent()
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK")

        intent.putExtra(EXTRA_STICKER_PACK_ID, "sp.category1")
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY,  activity?.packageName + ".provider")
        intent.putExtra(EXTRA_STICKER_PACK_NAME, sp.name)
        try {
            stickerLauncher?.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_adding_sticker_pack, Toast.LENGTH_LONG).show()
        }
    }

}