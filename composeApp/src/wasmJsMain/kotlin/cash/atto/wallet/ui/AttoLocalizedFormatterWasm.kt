package cash.atto.wallet.ui

@JsFun("() => navigator.language || 'en-US'")
private external fun currentLocale(): String

@JsFun("""
    (value, locale) => new Intl.NumberFormat(locale, {
        maximumFractionDigits: 2,
        minimumFractionDigits: 0
    }).format(value)
""")
private external fun intlFormat(value: Double, locale: String): String

actual object AttoLocalizedFormatter {
    actual fun format(value: String): String =
        value.toDoubleOrNull()?.let { intlFormat(it, currentLocale()) } ?: "…"
}
