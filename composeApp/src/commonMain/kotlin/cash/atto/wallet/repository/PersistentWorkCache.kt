package cash.atto.wallet.repository

import cash.atto.commons.AttoWork
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.createWork
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersistentWorkCache(
    appDatabase: AppDatabase,
) : AttoWorkCache {
    private val dao = appDatabase.workDao()
    private val _hasCachedWork = MutableStateFlow(false)

    val hasCachedWork: StateFlow<Boolean> = _hasCachedWork.asStateFlow()

    init {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            _hasCachedWork.value = dao.get() != null
        }
    }

    override suspend fun get(): AttoWork? {
        val cachedWork = dao.get()?.let { AttoWork(it.value) }
        _hasCachedWork.value = false
        return cachedWork
    }

    override suspend fun save(work: AttoWork) {
        dao.clear()
        dao.set(createWork(ByteArray(32), work.value))
        _hasCachedWork.value = true
    }
}
