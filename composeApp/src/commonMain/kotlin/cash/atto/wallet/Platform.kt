package cash.atto.wallet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform