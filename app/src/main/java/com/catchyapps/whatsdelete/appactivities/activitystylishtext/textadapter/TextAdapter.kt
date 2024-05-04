package com.catchyapps.whatsdelete.appactivities.activitystylishtext.textadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharFifth
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharFirst
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharFourth
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharSecond
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharSeventh
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharSixth
import com.catchyapps.whatsdelete.basicapputils.AppStylishFontUtils.getSpecialCharThird
import com.catchyapps.whatsdelete.appactivities.activitystylishtext.textdata.TextFont
import com.catchyapps.whatsdelete.databinding.ItemStylishFontsLayoutBinding


class TextAdapter(
    val context: Context,
    private var textFontList: List<TextFont>,
    private val onClick: (item: TextFont, action:String) -> Unit
) : RecyclerView.Adapter<TextVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextVH {
        return TextVH(
            ItemStylishFontsLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return textFontList.size
    }

    override fun onBindViewHolder(holder: TextVH, position: Int) {
        val mFont = textFontList[position]
        holder.binding.apply {
            btnCopy.setOnClickListener {
                onClick(mFont,"copy")
            }
            btnWhatsapp.setOnClickListener {
                onClick(mFont,"whatsapp")
            }

            btnShare.setOnClickListener {
                onClick(mFont,"share")
            }
        }

        val strBld: StringBuilder = StringBuilder(mFont.previewText)
        when (position) {
            0 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharFirst(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            1 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharSecond(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            2 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharThird(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            3 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharFourth(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            4 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharFifth(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            5 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharSixth(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            6 -> {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    val a = strBld[charOne]
                    val newCh: Char = getSpecialCharSeventh(a)
                    strBld.setCharAt(charOne, newCh)
                    charOne++
                }
            }

            7 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♥')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♥")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♥')
                    }
                    charOne++
                }
            }

            8 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (charOne == 0) {
                        strBld.insert(charOne, '【')
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '】')
                        strBld.insert(++charOne, '【')
                    }
                    if (strBld[charOne] == ' ') {
                        strBld.deleteCharAt(--charOne)
                        strBld.insert(++charOne, '【')
                    }
                    if (strBld.length - 1 == charOne) {
                        strBld.deleteCharAt(charOne)
                    }
                    charOne++
                }
            } 
            else if (!mFont.previewText.contains("【") && !mFont.previewText.contains("】")) {
                var charOne = 0
                while (charOne <= strBld.length - 1) {
                    if (charOne == 0) {
                        strBld.insert(charOne, '【')
                        ++charOne
                    }
                    if (strBld.length - 1 != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '】')
                        strBld.insert(++charOne, '【')
                    } else if (strBld.length - 1 == charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '】')
                    }
                    if (strBld[charOne] == ' ' && charOne != 0) {
                        strBld.deleteCharAt(--charOne)
                        strBld.insert(++charOne, '【')
                    } else if (strBld.length - 1 == charOne && strBld[charOne] == ' ' && charOne != 0) {
                        strBld.deleteCharAt(++charOne)
                    }
                    charOne++
                }
            }

            9 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☆')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☆")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☆')
                    }
                    charOne++
                }
            }

            10 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❦')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("❦")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❦')
                    }
                    charOne++
                }
            }

            11 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❄')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("❄")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❄')
                    }
                    charOne++
                }
            }

            12 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '✾')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("✾")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '✾')
                    }
                    charOne++
                }
            }

            13 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☀')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☀")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☀')
                    }
                    charOne++
                }
            }

            14 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☃')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☃")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☃')
                    }
                    charOne++
                }
            }

            15 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❤')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("❤")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❤')
                    }
                    charOne++
                }
            }

            16 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☘')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☘")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☘')
                    }
                    charOne++
                }
            }

            17 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☕')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☕")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☕')
                    }
                    charOne++
                }
            }

            18 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☝')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("☝")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '☝')
                    }
                    charOne++
                }
            }

            19 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❁')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("❁")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '❁')
                    }
                    charOne++
                }
            }

            20 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♈')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♈")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♈')
                    }
                    charOne++
                }
            }

            21 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♉')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♉")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♉')
                    }
                    charOne++
                }
            }

            22 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♊')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♊")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♊')
                    }
                    charOne++
                }
            }

            23 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♋')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♋")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♋')
                    }
                    charOne++
                }
            }

            24 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♌')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♌")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♌')
                    }
                    charOne++
                }
            }

            25 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♍')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♍")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♍')
                    }
                    charOne++
                }
            }

            26 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♎')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♎")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♎')
                    }
                    charOne++
                }
            }

            27 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♏')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♏")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♏')
                    }
                    charOne++
                }
            }

            28 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♐')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♐")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♐')
                    }
                    charOne++
                }
            }

            29 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♑')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♑")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♑')
                    }
                    charOne++
                }
            }

            30 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♒')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♒")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♒')
                    }
                    charOne++
                }
            }

            31 -> if ("Preview text" == mFont.previewText) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♓')
                    }
                    charOne++
                }
            } else if (!mFont.previewText.contains("♓")) {
                var charOne = 0
                while (charOne < strBld.length) {
                    if (strBld[charOne] == ' ' && strBld.length - 1 != charOne) {
                        ++charOne
                    }
                    if (strBld.length != charOne && strBld[charOne] != ' ') {
                        strBld.insert(++charOne, '♓')
                    }
                    charOne++
                }
            }
        }
        mFont.previewText= (strBld.toString())
        holder.binding.fontTextView.setText(mFont.previewText)
    }


    fun setFontsData(dataList: List<TextFont>) {
        this.textFontList = dataList
        notifyDataSetChanged()
    }
}