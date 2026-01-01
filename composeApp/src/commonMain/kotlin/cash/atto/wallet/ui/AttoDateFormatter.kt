package cash.atto.wallet.ui

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


expect object AttoDateFormatter {
    @OptIn(ExperimentalTime::class)
    fun format(value: Instant): String

    @OptIn(ExperimentalTime::class)
    fun formatDate(value: Instant): String
}


fun AttoDateFormatter.formatRelativeDate(value: Instant): String {
    if (value.toEpochMilliseconds() == 0L) {
        return "Unknown"
    }

    val difference = Clock.System.now() - value
    return when (difference.inWholeDays) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> "$difference days ago"
    }
}