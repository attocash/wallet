package cash.atto.wallet

import androidx.compose.runtime.Composable
import cash.atto.commons.AttoMnemonic

interface MnemonicManager {
    suspend fun save(mnemonic: AttoMnemonic)
    suspend fun find(): AttoMnemonic?
}


@Composable
expect fun mnemonicManager(): MnemonicManager