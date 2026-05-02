package cash.atto.wallet.uistate.welcome

import cash.atto.wallet.model.MetricsResponse
import cash.atto.wallet.model.getCirculatingSupply
import cash.atto.wallet.model.getConfirmationMs
import cash.atto.wallet.model.getPriceUsd
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode

data class WelcomeMetricsUiState(
    val marketCapValue: String,
    val confirmationValue: String,
    val priceUsdValue: String,
) {
    companion object {
        val DEFAULT =
            WelcomeMetricsUiState(
                marketCapValue = "…",
                confirmationValue = "…",
                priceUsdValue = "…",
            )
    }
}

fun MetricsResponse.toWelcomeMetricsUiState(): WelcomeMetricsUiState {
    val priceUsd = metrics.getPriceUsd()?.toBigDecimalOrNull()
    val circulatingSupply = metrics.getCirculatingSupply()?.toBigDecimalOrNull()
    val confirmation = metrics.getConfirmationMs()?.toBigDecimalOrNull()

    return WelcomeMetricsUiState(
        marketCapValue =
            formatCompactUsd(
                priceUsd?.let { price ->
                    circulatingSupply?.let { supply -> price * supply }
                },
            ),
        confirmationValue =
            confirmation
                ?.roundToDigitPositionAfterDecimalPoint(
                    0,
                    RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                )?.toPlainString() ?: "…",
        priceUsdValue = formatUsdPrice(priceUsd),
    )
}

private fun String.toBigDecimalOrNull(): BigDecimal? =
    try {
        BigDecimal.parseString(this)
    } catch (_: Exception) {
        null
    }

private fun formatCompactUsd(value: BigDecimal?): String {
    if (value == null) return "…"

    val billion = BigDecimal.parseString("1000000000")
    val million = BigDecimal.parseString("1000000")
    val thousand = BigDecimal.parseString("1000")

    return when {
        value >= billion -> {
            "$" +
                (value / billion)
                    .roundToDigitPositionAfterDecimalPoint(
                        2,
                        RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                    ).toPlainString() + "B"
        }

        value >= million -> {
            "$" +
                (value / million)
                    .roundToDigitPositionAfterDecimalPoint(
                        2,
                        RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                    ).toPlainString() + "M"
        }

        value >= thousand -> {
            "$" +
                (value / thousand)
                    .roundToDigitPositionAfterDecimalPoint(
                        2,
                        RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                    ).toPlainString() + "K"
        }

        else -> {
            "$" +
                value
                    .roundToDigitPositionAfterDecimalPoint(
                        2,
                        RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
                    ).toPlainString()
        }
    }
}

private fun formatUsdPrice(value: BigDecimal?): String {
    if (value == null) return "…"

    val rounded =
        value.roundToDigitPositionAfterDecimalPoint(
            8,
            RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
        )

    return "$${rounded.toPlainString()}"
}
