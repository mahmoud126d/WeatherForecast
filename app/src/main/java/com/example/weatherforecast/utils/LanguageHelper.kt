package com.example.weatherforecast.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

object LanguageHelper {
    fun changeLanguage(  context: Context,languageCode: String) {
        //version >= 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            //version < 13
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }

    fun getDefaultLanguage() = Locale.getDefault().language
    fun formatNumber(value: Int): String {
        val formatter = NumberFormat.getInstance(Locale.getDefault())
        return formatter.format(value)
    }
    fun formatDateBasedOnLocale(dateString: String): String {
        val locale = Locale.getDefault() // Get current device language
        val inputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", locale)

        return try {
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: "Invalid date"
        } catch (e: Exception) {
            "Invalid date format"
        }
    }
}