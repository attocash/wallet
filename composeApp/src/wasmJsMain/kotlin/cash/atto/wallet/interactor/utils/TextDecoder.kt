package cash.atto.wallet.interactor.utils

import org.khronos.webgl.Uint8Array

external class TextDecoder : JsAny {
    fun decode(str: Uint8Array): String
}