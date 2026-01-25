package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork

fun AttoNetwork.gatekeerperUrl(): String {
    return when (this) {
        AttoNetwork.LIVE -> "https://gatekeeper.live.application.atto.cash"
        AttoNetwork.BETA -> "https://gatekeeper.beta.application.atto.cash"
        else -> "https://gatekeeper.dev.application.atto.cash"
    }
}