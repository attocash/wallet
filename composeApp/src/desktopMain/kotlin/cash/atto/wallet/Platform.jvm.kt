package cash.atto.wallet

class JVMPlatform: Platform {
    override val name = "Java ${System.getProperty("java.version")}"
    override val type: PlatformType
        get() {
            val osName = System.getProperty("os.name").lowercase()

            return when {
                "windows" in osName -> PlatformType.WINDOWS
                "mac" in osName -> PlatformType.MACOS
                else -> PlatformType.LINUX
            }
        }
}

actual fun getPlatform(): Platform = JVMPlatform()