package cash.atto.wallet.di

import cash.atto.wallet.repository.AccountsRepository
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.AuthRepository
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.SecretPhraseViewModel
import cash.atto.wallet.viewmodel.SettingsViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val httpClientModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(
                    json = Json { ignoreUnknownKeys = true },
                    contentType = ContentType.Any
                )
            }
            install(Logging) {
                level = LogLevel.ALL // Logs everything (headers, bodies, etc.)
                logger = Logger.SIMPLE
            }
            install(HttpTimeout)
        }
    }
}

val repositoryModule = module {
    includes(httpClientModule)
    singleOf(::AccountsRepository)
    singleOf(::AppStateRepository)
    singleOf(::AuthRepository)
}

val viewModelModule = module {
    includes(repositoryModule)
    viewModelOf(::OverviewViewModel)
    viewModelOf(::SecretPhraseViewModel)
    viewModelOf(::SettingsViewModel)
}