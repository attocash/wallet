package cash.atto.wallet.components.common

interface QrScanner {
    fun startScanning(onResult: (String) -> Unit)

    fun stopScanning()
}
