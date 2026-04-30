package cash.atto.wallet.repository

import cash.atto.commons.AttoWork
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.Work
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class PersistentWorkCache(
    appDatabase: AppDatabase,
) {
    private val dao = appDatabase.workDao()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _hasCachedWork = MutableStateFlow(false)

    val hasCachedWork: StateFlow<Boolean> = _hasCachedWork.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                _hasCachedWork.value = dao.get() != null
                delay(1.seconds)
            }
        }
    }

    suspend fun get(): AttoWork? = dao.get()?.let { AttoWork(it.value) }

    suspend fun save(work: AttoWork) {
        dao.clear()
        dao.set(
            Work(
                publicKey = ByteArray(32),
                value = work.value,
            ),
        )
        _hasCachedWork.value = true
    }

    suspend fun clear() {
        dao.clear()
        _hasCachedWork.value = false
    }
}
