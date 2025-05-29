package cash.atto.wallet.ui

import kotlinx.datetime.Instant

expect object AttoDateFormatter {
    fun format(value: Instant): String
}
