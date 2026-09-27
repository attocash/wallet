package cash.atto.wallet.viewmodel

import cash.atto.commons.AttoMnemonic
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.datasource.TempSeedDataSource
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.interactor.SeedAESInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.PlatformWalletKeyStore
import cash.atto.wallet.repository.TermsAndConditionsRepository
import cash.atto.wallet.state.AppState.AuthState
import cash.atto.wallet.support.LEGACY_SALT
import cash.atto.wallet.support.encryptLegacy
import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordTermsAcceptanceTest {
    @Test
    fun `reimported mnemonic cannot start a session until current terms are accepted`() =
        runTest {
            // Given
            localStorage.clear()
            val preferences = PreferencesDataSource()
            val terms = TermsAndConditionsRepository(preferences)
            val temporary = TempSeedDataSource()
            val repository = AppStateRepository(keyStore(preferences), temporary, backgroundScope)
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            repository.importSecret(mnemonic.words)
            val viewModel = CreatePasswordViewModel(repository, CheckPasswordInteractor(), terms)
            viewModel.setPassword(PASSWORD)
            viewModel.setPasswordConfirm(PASSWORD)

            // When / Then: neither missing nor obsolete acceptance permits authentication or storage.
            assertFalse(viewModel.savePassword())
            preferences.setTermsAndConditionsDate("2000-01-01")
            assertFalse(viewModel.savePassword())
            assertEquals(AuthState.NEW_ACCOUNT, repository.state.value.authState)
            assertEquals(mnemonic.phrase, temporary.seed)
            assertNull(SeedDataSource().getSeed())
            terms.setCurrentTermsAccepted(true)
            terms.setCurrentTermsAccepted(false)
            assertFalse(viewModel.savePassword())

            // When / Then: explicit acceptance allows the existing immediate sign-in flow.
            terms.setCurrentTermsAccepted(true)
            assertTrue(viewModel.savePassword())
            assertTrue(terms.accepted.first())
            assertEquals(AuthState.SESSION_VALID, repository.state.value.authState)
            assertNull(temporary.seed)
            repository.lock()
        }

    @Test
    fun `unlock submission also requires current terms acceptance`() =
        runTest {
            // Given
            localStorage.clear()
            localStorage.setItem("salt", LEGACY_SALT)
            val mnemonic = AttoMnemonic.fromEntropy(ByteArray(33))
            val encrypted = encryptLegacy(mnemonic.phrase, PASSWORD)
            SeedDataSource().setSeed(encrypted)
            val preferences = PreferencesDataSource()
            val terms = TermsAndConditionsRepository(preferences)
            val repository = AppStateRepository(keyStore(preferences), TempSeedDataSource(), backgroundScope)
            val viewModel = AppViewModel(repository, CheckPasswordInteractor(), terms)

            // When / Then
            assertFalse(viewModel.enterPassword(PASSWORD))
            preferences.setTermsAndConditionsDate("2000-01-01")
            assertFalse(viewModel.enterPassword(PASSWORD))
            assertNull(repository.state.value.mnemonic)
            assertEquals(encrypted, SeedDataSource().getSeed())
            terms.setCurrentTermsAccepted(true)
            assertTrue(viewModel.enterPassword(PASSWORD))
            assertEquals(AuthState.SESSION_VALID, repository.state.value.authState)
            repository.lock()
            terms.setCurrentTermsAccepted(false)
            assertFalse(viewModel.enterPassword(PASSWORD))
            assertNull(repository.state.value.mnemonic)
        }

    private fun keyStore(preferences: PreferencesDataSource) =
        PlatformWalletKeyStore(SeedDataSource(), PasswordDataSource(), SeedAESInteractor(SaltDataSource()), preferences)

    private companion object {
        const val PASSWORD = "Synthetic7!"
    }
}
