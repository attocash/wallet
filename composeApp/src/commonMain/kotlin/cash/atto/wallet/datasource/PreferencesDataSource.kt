package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

expect class PreferencesDataSource {
    val blob: Flow<String?>
    val termsAndConditionsDate: Flow<String?>
    val work: Flow<String?>

    suspend fun setBlob(blob: String)

    suspend fun setTermsAndConditionsDate(date: String)

    suspend fun clearTermsAndConditionsDate()

    suspend fun setWork(work: String)
}
