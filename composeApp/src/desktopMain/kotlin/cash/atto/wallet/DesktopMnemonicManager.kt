package cash.atto.wallet

import androidx.compose.runtime.Composable
import cash.atto.commons.AttoMnemonic


class DesktopMnemonicManager : MnemonicManager {
    private var mnemonic: AttoMnemonic? = null
    override suspend fun save(mnemonic: AttoMnemonic) {
        this.mnemonic = mnemonic
    }

    override suspend fun find(): AttoMnemonic? {
        return mnemonic
    }

}

@Composable
actual fun mnemonicManager(): MnemonicManager {
    return DesktopMnemonicManager()
}