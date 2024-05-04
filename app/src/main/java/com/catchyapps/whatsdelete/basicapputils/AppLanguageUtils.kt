package com.catchyapps.whatsdelete.basicapputils

import com.catchyapps.whatsdelete.R
import com.catchyapps.whatsdelete.appactivities.myapplanguage.countrymodel.ModelCountry
import com.zeugmasolutions.localehelper.Locales
import java.util.Locale

object AppLanguageUtils {

     fun getLanguages(): MutableList<ModelCountry> {
        val languageList = mutableListOf<ModelCountry>()
        languageList.add(
            ModelCountry(
                Locales.English, "English (US)", R.drawable.flag_united_states_of_america
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Portuguese, "Português", R.drawable.flag_portugal
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Spanish, "Español", R.drawable.flag_spain
            )
        )

        languageList.add(
            ModelCountry(
                Locales.German, "Deutsch", R.drawable.flag_germany
            )
        )

        languageList.add(
            ModelCountry(
                Locales.French, "Français", R.drawable.flag_france
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Japanese, "日本語", R.drawable.flag_japan
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Korean, "한국인", R.drawable.flag_south_korea
            )
        )
        languageList.add(
            ModelCountry(
                Locales.Indonesian, "Bahasa Indonesia", R.drawable.flag_indonesia
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Russian, "русский", R.drawable.flag_russian_federation
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Arabic, "العربية", R.drawable.flag_saudi_arabia
            )
        )


        languageList.add(
            ModelCountry(
                Locales.Persian, "فارسی", R.drawable.flag_iran
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Italian, "Italian", R.drawable.flag_italy
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Dutch, "Dutch", R.drawable.flag_poland
            )
        )

        languageList.add(
            ModelCountry(
                Locales.Swedish, "Swedish", R.drawable.flag_sweden
            )
        )
        languageList.add(
            ModelCountry(
                Locales.Vietnamese, "Tiếng Việt", R.drawable.flag_vietnam
            )
        )

         languageList.add(
             ModelCountry(
                 Locales.NorwegianNynorsk, "Norsk", R.drawable.flag_indonesia
             )
         )
        languageList.add(
            ModelCountry(
                Locales.Thai, "คนไทย", R.drawable.flag_thailand
            )
        )
        languageList.add(
            ModelCountry(
                Locale("zh"), "中国人", R.drawable.flag_china
            )
        )
        return languageList
    }
}