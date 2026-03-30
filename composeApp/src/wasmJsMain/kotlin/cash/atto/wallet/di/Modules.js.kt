package cash.atto.wallet.di

import androidx.room3.Room
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import cash.atto.wallet.datasource.*
import cash.atto.wallet.interactor.SeedAESInteractor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.w3c.dom.Worker

private val database by lazy {
    Room.inMemoryDatabaseBuilder<AppDatabaseWasmJs>(
        factory = AppDatabaseWasmJsConstructor::initialize
    )
        .setDriver(WebWorkerSQLiteDriver(createWorker()))
        .build()
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun createWorker(): Worker =
    js("""new Worker(new URL("sqlite-web-worker/worker.js", import.meta.url))""")

actual val databaseModule = module {
    single<AppDatabase> { database }
}

actual val dataSourceModule = module {
    includes(databaseModule)
    singleOf(::PasswordDataSource)
    singleOf(::SeedAESInteractor)
    singleOf(::SaltDataSource)
    singleOf(::SeedDataSource)
}
