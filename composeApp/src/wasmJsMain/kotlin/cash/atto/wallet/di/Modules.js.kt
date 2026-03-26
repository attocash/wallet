package cash.atto.wallet.di

import androidx.room3.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import cash.atto.wallet.datasource.AppDatabase
import cash.atto.wallet.datasource.AppDatabaseWasmJsConstructor
import cash.atto.wallet.datasource.AppDatabaseWasmJs
import cash.atto.wallet.datasource.PasswordDataSource
import cash.atto.wallet.datasource.SaltDataSource
import cash.atto.wallet.datasource.SeedDataSource
import cash.atto.wallet.interactor.SeedAESInteractor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.w3c.dom.Worker

/**
 * Wraps [WebWorkerSQLiteDriver] and reports [hasConnectionPool] as `true` so that Room uses
 * its single-connection [PassthroughConnectionPool] instead of opening separate reader/writer
 * connections that fight over the same OPFS file lock.
 */
private class SingleConnectionWebDriver(worker: Worker) : SQLiteDriver {
    private val delegate = WebWorkerSQLiteDriver(worker)

    override val hasConnectionPool: Boolean get() = true

    override suspend fun openAsync(fileName: String): SQLiteConnection =
        delegate.openAsync(fileName)
}

private val database by lazy {
    Room.databaseBuilder<AppDatabaseWasmJs>(
        name = "atto-wallet.db",
        factory = AppDatabaseWasmJsConstructor::initialize
    )
        .setDriver(SingleConnectionWebDriver(createWorker()))
        .build()
}

fun getDatabaseBuilder(): AppDatabase {
    return database
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("sqlite-web-worker/worker.js", import.meta.url))""")

actual val databaseModule = module {
    single<AppDatabase> { getDatabaseBuilder() }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedAESInteractor)
    singleOf(::SaltDataSource)
    singleOf(::SeedDataSource)
}
