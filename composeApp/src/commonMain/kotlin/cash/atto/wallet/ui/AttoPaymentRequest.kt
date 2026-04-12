package cash.atto.wallet.ui

import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoUnit

data class AttoPaymentRequest(
    val address: String,
    val amountRaw: String?,
)

object AttoPaymentRequests {
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

    fun parse(value: String?): AttoPaymentRequest? {
        val rawValue = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val parts = rawValue.split("?", limit = 2)
        val address = parts.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val query = parts.getOrNull(1).orEmpty()

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
            address = address,
            amountRaw = amountRaw,
        )
    }
}
