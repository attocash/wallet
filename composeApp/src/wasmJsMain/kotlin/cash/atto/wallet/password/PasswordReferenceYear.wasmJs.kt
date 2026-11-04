@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.password

internal actual fun currentPasswordReferenceYear(): Int = js("new Date().getFullYear()")
