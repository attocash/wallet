package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.wallet.Config
import cash.atto.wallet.state.AppState
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent.inject

class AccountsRepository(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository
) {

    private val _accountState = MutableStateFlow<AttoAccount?>(null)
    val accountState = _accountState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    fun openSocket(appState: AppState) {
        run("accounts") {
            val uri = "${Config.ENDPOINT}/accounts/${appState.publicKey.toString()}/stream"
            val jwt = authRepository.getAuthorization(appState)

            httpClient.prepareGet(uri) {
                timeout {
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                headers {
                    append("Authorization", "Bearer $jwt")
                    append("Accept", "application/x-ndjson")
                }
            }
            .execute { response ->
                val channel: ByteReadChannel = response.body()
                while (!channel.isClosedForRead && scope.isActive) {
                    val json = channel.readUTF8Line()
                    if (json != null) {
                        val account = Json.decodeFromString<AttoAccount>(json)
                        val height = _accountState.value?.height
                        if (height == null || account.height > height) {
                            println("Received from $uri $json")
                            _accountState.value = account
                        }
                    }
                }
            }
        }
    }

    private fun run(name: String, runnable: suspend () -> Any) {
        scope.launch {
            while (scope.isActive) {
                 try {
                    runnable.invoke()
                } catch (e: Exception) {
                    println("Failed to stream $name due to ${e.message}. ${e.stackTraceToString()}")
                    delay(10_000)
                }
            }
        }
    }
}