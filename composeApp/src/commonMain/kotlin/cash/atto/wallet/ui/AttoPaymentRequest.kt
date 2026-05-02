package cash.atto.wallet.ui

import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoUnit
import io.ktor.http.encodeURLParameter

data class AttoPaymentRequest(
    val receiverAddress: String,
    val amountRaw: String?,
)

object AttoPaymentRequests {
    private const val WALLET_BASE_URL = "https://wallet.atto.cash"

    fun extractBareAddress(value: String?): String? {
        val trimmed = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return trimmed
            .removePrefix("atto:///")
            .removePrefix("atto://")
            .trimStart('/')
            .takeIf { it.isNotBlank() }
    }

    fun sanitizeRawAmount(value: String?): String? =
        value
            ?.filter(Char::isDigit)
            ?.takeIf { it.isNotBlank() }

    fun build(
        address: String,
        amountRaw: String?,
    ): String {
        val sanitizedAmount = sanitizeRawAmount(amountRaw) ?: return address
        return "$address?amount=$sanitizedAmount"
    }

    fun buildFromAtto(
        address: String,
        amountAtto: String?,
    ): String {
        val rawAmount =
            amountAtto?.let { attoValue ->
                try {
                    AttoAmount
                        .from(
                            unit = AttoUnit.ATTO,
                            string = attoValue,
                        ).toString(AttoUnit.RAW)
                } catch (_: Exception) {
                    null
                }
            }

        return build(address, rawAmount)
    }

    fun buildWalletDeepLink(
        receiverAddress: String,
        amountRaw: String?,
    ): String {
        val sanitizedAmount = sanitizeRawAmount(amountRaw)

        val query =
            buildList {
                add("receiverAddress=${receiverAddress.encodeURLParameter()}")
                sanitizedAmount?.let { add("amount=$it") }
            }.joinToString("&")

        return "$WALLET_BASE_URL?$query"
    }

    fun buildWalletDeepLinkFromPaymentRequest(paymentRequest: String?): String? {
        val parsed = parse(paymentRequest) ?: return null
        return buildWalletDeepLink(
            receiverAddress = parsed.receiverAddress,
            amountRaw = parsed.amountRaw,
        )
    }

    fun parse(value: String?): AttoPaymentRequest? {
        val rawValue = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val parts = rawValue.split("?", limit = 2)
        val query = parts.getOrNull(1).orEmpty()

        val receiverAddressFromQuery =
            query
                .split("&")
                .asSequence()
                .mapNotNull { parameter ->
                    val pair = parameter.split("=", limit = 2)
                    val key = pair.firstOrNull()?.trim()
                    val valuePart = pair.getOrNull(1)?.trim()
                    if (key == "receiverAddress") valuePart else null
                }.firstOrNull()

        val address = receiverAddressFromQuery ?: parts.firstOrNull() ?: return null

        val amountRaw =
            query
                .split("&")
                .asSequence()
                .mapNotNull { parameter ->
                    val pair = parameter.split("=", limit = 2)
                    val key = pair.firstOrNull()?.trim()
                    val valuePart = pair.getOrNull(1)?.trim()
                    if (key == "amount") valuePart else null
                }.mapNotNull(::sanitizeRawAmount)
                .firstOrNull()

        return AttoPaymentRequest(
            receiverAddress = address,
            amountRaw = amountRaw,
        )
    }
}
