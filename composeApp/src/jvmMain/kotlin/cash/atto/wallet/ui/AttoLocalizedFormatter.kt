package cash.atto.wallet.ui

import java.text.NumberFormat
import java.util.*

actual object AttoLocalizedFormatter {
    actual fun format(value: String): String = try {
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }.format(value.toBigDecimal())
    } catch (_: Exception) { "…" }
}
