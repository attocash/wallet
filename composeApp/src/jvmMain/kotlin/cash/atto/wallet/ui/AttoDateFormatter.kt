package cash.atto.wallet.ui

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

actual object AttoDateFormatter {
    @OptIn(ExperimentalTime::class)
    actual fun format(value: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
        val zonedDateTime = value.toJavaInstant().atZone(ZoneId.systemDefault())
        return formatter.format(zonedDateTime)
    }
}
