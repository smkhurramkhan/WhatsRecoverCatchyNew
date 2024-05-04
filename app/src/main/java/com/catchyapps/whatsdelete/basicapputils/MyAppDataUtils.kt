package com.catchyapps.whatsdelete.basicapputils

import android.content.Context
import android.content.res.AssetManager
import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.activityhome.menumodels.ModelMenu
import com.catchyapps.whatsdelete.appactivities.activityquatations.data.QuotationsDataClass
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.DescSticker
import com.catchyapps.whatsdelete.appactivities.activitystickers.stickerdata.ListModelStickers

object MyAppDataUtils {

    fun setRecoveryData(context: Context): List<ModelMenu> {
        val recoveryCardList = mutableListOf<ModelMenu>()
        recoveryCardList.add(
            ModelMenu(
                "chat",
                context.getString(R.string.chat),
                R.drawable.ic_deleted_chat_svg
            )
        )

        recoveryCardList.add(
            ModelMenu(
                "documents",
                context.getString(R.string.document),
                R.drawable.ic_document_svg
            )
        )

        //status
        recoveryCardList.add(
            ModelMenu(
                "images",   context.getString(R.string.image),
                R.drawable.ic_deleted_images

            )
        )

        recoveryCardList.add(
            ModelMenu(
                "video", context.getString(R.string.video),  R.drawable.ic_deleted_videos

            )
        )

        recoveryCardList.add(
            ModelMenu(
                "audio", context.getString(R.string.audio),  R.drawable.ic_deleted_audio

            )
        )

        recoveryCardList.add(
            ModelMenu(
                "voice", context.getString(R.string.voice),  R.drawable.ic_deleted_voices

            )
        )

        return recoveryCardList
    }

    fun setStatusMenu(context: Context): List<ModelMenu> {
        val statusList = mutableListOf<ModelMenu>()
        //status

        statusList.add(
            ModelMenu(
                "statusImages", context.getString(R.string.image_status), R.drawable.ic_images

            )
        )

        statusList.add(
            ModelMenu(
                "statusVideos", context.getString(R.string.video_status), R.drawable.ic_videos

            )
        )

        statusList.add(
            ModelMenu(
                "savedstatus", context.getString(R.string.saved_status), R.drawable.ic_saved_statuses

            )
        )

        return statusList

    }

    fun setToolsData(context: Context): List<ModelMenu> {
        val toolsCardList = mutableListOf<ModelMenu>()
        toolsCardList.add(
            ModelMenu(
                "whatsweb",
                context.getString(R.string.whatsapp_web),
                R.drawable.ic_whatsap_svg
            )
        )

        //status
        toolsCardList.add(
            ModelMenu(
                "textStyle", context.getString(R.string.stylish_text_), R.drawable.ic_stylish_text_svg

            )
        )

        toolsCardList.add(
            ModelMenu(
                "textRepeater",
                context.getString(R.string.text_repeater_), R.drawable.ic_text_repeater_svg

            )
        )

        toolsCardList.add(
            ModelMenu(
                "whatscleaner",
                context.getString(R.string.whats_cleaner), R.drawable.ic_whatsup_cleaner_svg

            )
        )

        toolsCardList.add(
            ModelMenu(
                "directchat",
                context.getString(R.string.direct_chat_), R.drawable.ic_direct_chat_svg

            )
        )


        toolsCardList.add(
            ModelMenu(
                "quotations", context.getString(R.string.famous_quotes), R.drawable.ic_famous_quates

            )
        )

        toolsCardList.add(
            ModelMenu(
                "texttoemoji",
                context.getString(R.string.text_to_emoji_), R.drawable.ic_text_to_imoji_svg

            )
        )

        toolsCardList.add(
            ModelMenu(
                "stickers", context.getString(R.string.whatsapp_stickers), R.drawable.ic_sticker_svg

            )
        )

        toolsCardList.add(
            ModelMenu(
                "ascii", context.getString(R.string.ascii_faces), R.drawable.ic_ascii_faces_svg

            )
        )

        return toolsCardList
    }

    fun setQuotes(): List<QuotationsDataClass> {
        val quotesList = MyAppExcelUtils.readCategoryAndQuoteFromCSV("quotes.csv")
        return quotesList
    }


    fun loadStickersByCategory(assetManager: AssetManager): List<ListModelStickers> {
        val stickersByCategory = mutableListOf<ListModelStickers>()
        val stickerFolders = assetManager.list("stickers") ?: return stickersByCategory

        for (category in stickerFolders) {
            val stickerFiles = assetManager.list("stickers/$category") ?: continue
            val descStickerList = mutableListOf<DescSticker>()

            for (sticker in stickerFiles) {
                val stickerPath = "stickers/$category/$sticker"
                val descSticker = DescSticker(sticker, stickerPath)
                descStickerList.add(descSticker)
            }

            val stickersModel = ListModelStickers(category, descStickerList)
            stickersByCategory.add(stickersModel)
        }

        return stickersByCategory
    }

    fun loadStickersSingleList(assetManager: AssetManager, categoryName : String): List<DescSticker> {
        val descStickerList = mutableListOf<DescSticker>()

            val stickerFiles = assetManager.list("stickers/$categoryName")

            for (sticker in stickerFiles!!) {
                val stickerPath = "stickers/$categoryName/$sticker"
                val descSticker = DescSticker(sticker, stickerPath)
                descStickerList.add(descSticker)
            }


        return descStickerList
    }


    fun getAsciiList():List<String>{
       return arrayListOf(
            "⸜(｡˃ ᵕ ˂ )⸝♡",
            "( ͡° ͜ʖ ͡°)",
            "≽^•⩊•^≼",
            "¯\\_(ツ)_/¯",
            "(☞ ͡° ͜ʖ ͡°)☞",
            "(⊙ _ ⊙ )",
            "(づ๑•ᴗ•๑)づ♡",
            "≽^•⩊•^≼",
            "¯\\_(ツ)_/¯",
            "(☞ ͡° ͜ʖ ͡°)☞",
            "(⊙ _ ⊙ )",
            "(づ๑•ᴗ•๑)づ♡",
            "(づ ᴗ _ᴗ)づ♡"
        )

    }
}
