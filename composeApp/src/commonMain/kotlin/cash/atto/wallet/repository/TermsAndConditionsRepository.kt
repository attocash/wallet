package cash.atto.wallet.repository

import cash.atto.wallet.datasource.PreferencesDataSource
import cash.atto.wallet.model.TermsAndConditions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TermsAndConditionsRepository(
    private val preferencesDataSource: PreferencesDataSource,
) {
    val accepted: Flow<Boolean> =
        preferencesDataSource.termsAndConditionsDate.map { storedDate ->
            storedDate == TermsAndConditions.EFFECTIVE_DATE
        }

    suspend fun setCurrentTermsAccepted(accepted: Boolean) {
        if (accepted) {
            preferencesDataSource.setTermsAndConditionsDate(TermsAndConditions.EFFECTIVE_DATE)
        } else {
            preferencesDataSource.clearTermsAndConditionsDate()
        }
    }
}
