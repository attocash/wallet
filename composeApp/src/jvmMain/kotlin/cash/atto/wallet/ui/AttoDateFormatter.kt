package cash.atto.wallet.ui

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

actual object AttoDateFormatter {
    actual fun format(value: Instant): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
        val zonedDateTime = value.toJavaInstant().atZone(ZoneId.systemDefault())
        return formatter.format(zonedDateTime)
    }
}
