package cash.atto.wallet

interface Platform {
    val name: String
    val type: PlatformType
}

enum class PlatformType {
    ANDROID, WINDOWS, LINUX, MACOS;
}

expect fun getPlatform(): Platform