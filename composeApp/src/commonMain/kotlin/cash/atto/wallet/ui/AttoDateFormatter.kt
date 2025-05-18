package cash.atto.wallet.ui

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object AttoDateFormatter {
    fun format(value: Instant): String {
        val dt     = value.toLocalDateTime(TimeZone.currentSystemDefault())
        val day    = dt.dayOfMonth.toString().padStart(2, '0')
        val month  = localizedMonth(dt.monthNumber, Locale.current.language)
        val year   = dt.year
        val hour   = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        return "$day $month $year, $hour:$minute"
    }

    private fun localizedMonth(month: Int, lang: String): String {
        val en = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val de = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
        val fr = listOf("janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc.")
        val pl = listOf("sty", "lut", "mar", "kwi", "maj", "cze", "lip", "sie", "wrz", "paź", "lis", "gru")

        val set = when (lang) {
            "de" -> de
            "fr" -> fr
            "pl" -> pl
            else -> en
        }
        return set.getOrElse(month - 1) { "???" }
    }
}
