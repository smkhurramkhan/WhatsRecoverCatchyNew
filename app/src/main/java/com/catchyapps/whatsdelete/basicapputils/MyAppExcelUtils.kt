package com.catchyapps.whatsdelete.basicapputils

import android.content.res.AssetManager
import com.catchyapps.whatsdelete.appactivities.activityquatations.data.QuotationsDataClass
import timber.log.Timber
import java.io.InputStreamReader

object MyAppExcelUtils {

    private lateinit var assetManager: AssetManager // Declare the AssetManager

    fun handleAssetManager(manager: AssetManager) {
        assetManager = manager

    }

    fun readCategoryAndQuoteFromCSV(assetFileName: String):List<QuotationsDataClass> {
        val quotesList = mutableListOf<QuotationsDataClass>()
        val inputStream = assetManager.open(assetFileName)
        val reader = InputStreamReader(inputStream)

        reader.use { inputReader ->
            val csvData = inputReader.readLines().drop(1) // Skip header
            csvData.forEach { line ->
                val columns = line.split(",") // Split by comma assuming CSV format
                val category = columns.getOrNull(0) ?: ""
                val quote = columns.getOrNull(1) ?: ""

                quotesList.add(QuotationsDataClass(category, quote))

                // Process category and quote data
                Timber.d("Category: $category, Quote: $quote")
            }
        }
        return quotesList
    }


}