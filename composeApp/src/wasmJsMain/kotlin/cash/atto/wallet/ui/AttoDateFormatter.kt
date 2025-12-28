package cash.atto.wallet.ui

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@JsFun("() => navigator.language || 'en-US'")
private external fun currentLocale(): String

@JsFun(
    """
    (iso, locale) => new Intl.DateTimeFormat(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(iso))
"""
)
private external fun formatDateTime(isoString: String, locale: String): String

@JsFun(
    """
    (iso, locale) => new Intl.DateTimeFormat(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    }).format(new Date(iso))
"""
)
private external fun formatDateOnly(isoString: String, locale: String): String

actual object AttoDateFormatter {
    @OptIn(ExperimentalTime::class)
    actual fun format(value: Instant): String =
        formatDateTime(value.toString(), currentLocale())

    @OptIn(ExperimentalTime::class)
    actual fun formatDate(value: Instant): String =
        formatDateOnly(value.toString(), currentLocale())
}
