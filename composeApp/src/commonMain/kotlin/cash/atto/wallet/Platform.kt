package cash.atto.wallet

interface Platform {
    val name: String
    val type: PlatformType
}

enum class PlatformType {
    ANDROID, DESKTOP;
}

expect fun getPlatform(): Platform