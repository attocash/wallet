package cash.atto.wallet.repository

import cash.atto.commons.AttoWork
import cash.atto.commons.wallet.AttoWorkCache
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.Work

class PersistentWorkCache(
    appDatabase: AppDatabase
) : AttoWorkCache {

    private val dao = appDatabase.workDao()

    override suspend fun get(): AttoWork? = dao.get()
        ?.let { AttoWork(it.value) }

    override suspend fun save(work: AttoWork) {
        clear()
        dao.set(Work(ByteArray(32), work.value))
    }

    suspend fun clear() {
        dao.clear()
    }
}