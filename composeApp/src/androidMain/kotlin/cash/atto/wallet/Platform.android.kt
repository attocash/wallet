package cash.atto.wallet

import android.os.Build

class AndroidPlatform : Platform {
    override val name = "Android ${Build.VERSION.SDK_INT}"
    override val type = PlatformType.ANDROID
}

actual fun getPlatform(): Platform = AndroidPlatform()