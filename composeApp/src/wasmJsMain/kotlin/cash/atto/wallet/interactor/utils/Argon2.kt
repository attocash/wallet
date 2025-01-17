@file:JsModule("argon2-browser")
package cash.atto.wallet.interactor.utils

import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

external class Argon2 {
    fun hash(password: String): Promise<ArrayBuffer>
}