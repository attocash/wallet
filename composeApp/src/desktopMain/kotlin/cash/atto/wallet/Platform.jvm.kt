package cash.atto.wallet

class JVMPlatform: Platform {
    override val name = "Java ${System.getProperty("java.version")}"
    override val type = PlatformType.DESKTOP
}

actual fun getPlatform(): Platform = JVMPlatform()