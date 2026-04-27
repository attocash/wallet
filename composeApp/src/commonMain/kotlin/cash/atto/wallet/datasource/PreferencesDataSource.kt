package cash.atto.wallet.datasource

import kotlinx.coroutines.flow.Flow

expect class PreferencesDataSource {
    val blob: Flow<String?>

    suspend fun setBlob(blob: String)
}
