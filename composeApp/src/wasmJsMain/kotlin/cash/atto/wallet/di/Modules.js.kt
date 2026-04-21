package cash.atto.wallet.di

import androidx.room3.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseConstructor
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.w3c.dom.Worker

/**
 * Wraps [WebWorkerSQLiteDriver] to redirect the in-memory path (":memory:") to a named OPFS
 * file for persistent storage. Used with [Room.inMemoryDatabaseBuilder] so that Room selects
 * its single-connection pool (which properly serializes reader/writer access via a semaphore),
 * while the actual data is persisted to OPFS.
 *
 * This avoids two Room3 alpha issues on web:
 * - [PassthroughConnectionPool] (used when hasConnectionPool=true) lacks mutual exclusion,
 *   causing "cannot start a transaction within a transaction" when the invalidation tracker
 *   and DAO operations interleave.
 * - Multiple OPFS connections (used with databaseBuilder + named DB) cause
 *   "createSyncAccessHandle" lock contention.
 */
private class PersistentSingleConnectionDriver(worker: Worker) : SQLiteDriver {
    private val delegate = WebWorkerSQLiteDriver(worker)

    override suspend fun open(fileName: String): SQLiteConnection =
        delegate.open("atto-wallet.db")
}

private val database by lazy {
    Room.inMemoryDatabaseBuilder<AppDatabase>(
        factory = AppDatabaseConstructor::initialize,
    ).setDriver(PersistentSingleConnectionDriver(createWorker()))
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("sqlite-web-worker/worker.js", import.meta.url))""")

actual val databaseModule =
    module {
        single<AppDatabase> { database }
    }

actual val dataSourceModule =
    module {
        includes(databaseModule)
        singleOf(::PasswordDataSource)
        singleOf(::SeedAESInteractor)
        singleOf(::SaltDataSource)
        singleOf(::SeedDataSource)
    }
