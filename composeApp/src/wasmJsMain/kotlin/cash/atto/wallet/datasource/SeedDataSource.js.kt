package cash.atto.wallet.datasource

import kotlinx.browser.localStorage
import org.w3c.dom.get

actual class SeedDataSource {
    actual suspend fun getSeed(): String? = localStorage[SEED_KEY]

    actual suspend fun setSeed(seed: String) {
        localStorage.setItem(SEED_KEY, seed)
    }

    actual suspend fun clearSeed() {
        localStorage.removeItem(SEED_KEY)
    }

    companion object {
        private const val SEED_KEY = "seed"
    }
}
