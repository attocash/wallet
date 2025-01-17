package cash.atto.wallet

class WebPlatform : Platform {
    override val name = "Web"
    override val type = PlatformType.WEB
}

actual fun getPlatform(): Platform = WebPlatform()