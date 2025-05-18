package cash.atto.wallet.ui

import androidx.compose.ui.text.intl.Locale
import com.ionspin.kotlin.bignum.decimal.BigDecimal

object AttoFormatter {

    fun format(value: BigDecimal?): String {
        if (value == null) return "…"

        val rounded = value.roundToDigitPosition(2)
        val parts = rounded.toStringExpanded().split(".")

        val wholePart = parts.firstOrNull()?.toULongOrNull()?.let { format(it) } ?: return "…"

        return if (parts.size == 2 && parts[1].isNotBlank()) {
            "$wholePart.${parts[1]}"
        } else {
            wholePart
        }
    }

    fun format(value: String?): String =
        value?.toULongOrNull()?.let { format(it) } ?: "…"

    fun format(value: ULong): String {
        val separator = when (Locale.current.language) {
            "en", "pl", "nl" -> ","
            "de", "fr", "es" -> "."
            else -> ","
        }

        val raw = value.toString()
        return buildString {
            var count = 0
            for (i in raw.indices.reversed()) {
                insert(0, raw[i])
                count++
                if (count % 3 == 0 && i != 0) insert(0, separator)
            }
        }
    }
}
