package cash.atto.wallet.ui

import kotlinx.datetime.Instant

@JsFun("() => navigator.language || 'en-US'")
private external fun currentLocale(): String

@JsFun("""
    (iso, locale) => new Intl.DateTimeFormat(locale, {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(iso))
""")
private external fun formatDate(isoString: String, locale: String): String

actual object AttoDateFormatter {
    actual fun format(value: Instant): String =
        formatDate(value.toString(), currentLocale())
}
