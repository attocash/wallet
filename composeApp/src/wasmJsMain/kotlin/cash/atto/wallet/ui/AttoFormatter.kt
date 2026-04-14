package cash.atto.wallet.ui

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@JsFun(
    """
    () => {
        const locale =
            navigator.language ||
            (Array.isArray(navigator.languages) ? navigator.languages[0] : null) ||
            'en-US'
        return String(locale).replace(/_/g, '-')
    }
""",
)
private external fun currentLocale(): String

@JsFun(
    """
    (value, locale) => new Intl.NumberFormat(locale, {
        maximumFractionDigits: 2,
        minimumFractionDigits: 0
    }).format(value)
""",
)
private external fun intlFormatNumber(
    value: Double,
    locale: String,
): String

@JsFun(
    """
    (iso, locale) => new Intl.DateTimeFormat(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(iso))
""",
)
private external fun intlFormatDateTime(
    isoString: String,
    locale: String,
): String

@JsFun(
    """
    (iso, locale) => new Intl.DateTimeFormat(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
    }).format(new Date(iso))
""",
)
private external fun intlFormatDateOnly(
    isoString: String,
    locale: String,
): String

internal actual fun formatLocalized(value: String): String =
    value.toDoubleOrNull()?.let { intlFormatNumber(it, currentLocale()) } ?: "…"

@OptIn(ExperimentalTime::class)
internal actual fun formatDateTime(value: Instant): String = intlFormatDateTime(value.toString(), currentLocale())

@OptIn(ExperimentalTime::class)
internal actual fun formatDateOnly(value: Instant): String = intlFormatDateOnly(value.toString(), currentLocale())
