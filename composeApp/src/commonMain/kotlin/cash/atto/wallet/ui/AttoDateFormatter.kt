package cash.atto.wallet.ui

import kotlin.time.ExperimentalTime
import kotlin.time.Instant


expect object AttoDateFormatter {
    @OptIn(ExperimentalTime::class)
    fun format(value: Instant): String

    @OptIn(ExperimentalTime::class)
    fun formatDate(value: Instant): String
}
