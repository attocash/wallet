package cash.atto.wallet.ui

import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

internal actual fun formatLocalized(value: String): String =
    try {
        NumberFormat
            .getNumberInstance(Locale.getDefault())
            .apply {
                maximumFractionDigits = 2
                minimumFractionDigits = 0
            }.format(value.toBigDecimal())
    } catch (_: Exception) {
        "…"
    }

@OptIn(ExperimentalTime::class)
internal actual fun formatDateTime(value: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
    val zonedDateTime = value.toJavaInstant().atZone(ZoneId.systemDefault())
    return formatter.format(zonedDateTime)
}

@OptIn(ExperimentalTime::class)
internal actual fun formatDateOnly(value: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    val zonedDateTime = value.toJavaInstant().atZone(ZoneId.systemDefault())
    return formatter.format(zonedDateTime)
}
