package cash.atto.wallet

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoHash
import cash.atto.commons.AttoNetwork
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoReceivable
import cash.atto.commons.AttoTransaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.measureTime

class AccountManager(
    private val endpoint: String,
    private val signer: Signer,
    private val authenticator: Authenticator,
    private val representative: AttoPublicKey,
    private val workerClient: WorkerClient,
    private var autoReceive: Boolean,
    transactions: List<AttoTransaction> = arrayListOf()
) : AutoCloseable {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(HttpTimeout)
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    val publicKey = signer.publicKey

    private val _accountState = MutableStateFlow<AttoAccount?>(null)
    val accountState = _accountState.asStateFlow()

    private val _receivableFlow = MutableSharedFlow<AttoReceivable>()
    val receivableFlow = _receivableFlow.asSharedFlow()

    private val _receivableState =
        MutableStateFlow<MutableMap<AttoHash, AttoReceivable>>(mutableMapOf())
    val receivableState = _receivableState.asStateFlow()

    private val _transactionFlow = MutableSharedFlow<AttoTransaction>()
    val transactionFlow = _transactionFlow.asSharedFlow()

    private val _transactionState = MutableStateFlow(transactions.toMutableList())
    val transactionState = _transactionState.asStateFlow()

    private val _publishFlow = MutableSharedFlow<AttoTransaction>()

    private suspend fun headers(): HttpRequestBuilder.() -> Unit {
        return {
            headers {
                runBlocking {
                    append("Authorization", "Bearer ${authenticator.getAuthorization()}")
                }
            }
        }
    }


    suspend fun start() {
        require(scope.isActive) {
            "Account manager was cancelled before. Please, recreate it"
        }


        scope.launch {
            _receivableFlow
                .takeWhile { scope.isActive }
                .onEach { receivable ->
                    _receivableState.update {
                        it.apply {
                            put(receivable.hash, receivable)
                        }
                    }
                }
        }

        scope.launch {
            _transactionFlow
                .takeWhile { scope.isActive }
                .onEach { transaction ->
                    _receivableState.update {
                        it.apply {
                            remove(transaction.hash)
                        }
                    }
                    _transactionState.update {
                        it.apply {
                            add(transaction)
                        }
                    }
                }
        }


        run("auto receive") {
            _receivableFlow
                .filter { autoReceive }
                .onEach {
                    receive(it)
                }
        }

        run("accounts") {
            val uri = "$endpoint/accounts/$publicKey/stream"
            val jwt = authenticator.getAuthorization()

            httpClient.prepareGet(uri) {
                timeout {
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                headers {
                    runBlocking {
                        append("Authorization", "Bearer $jwt")
                        append("Accept", "application/x-ndjson")
                    }
                }
            }
                .execute { response ->
                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead && scope.isActive) {
                        val json = channel.readUTF8Line(1000)
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

        run("receivables") {
            val uri = "$endpoint/accounts/$publicKey/receivables/stream"
            val jwt = authenticator.getAuthorization()

            httpClient.prepareGet(uri) {
                timeout {
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                headers {
                    runBlocking {
                        append("Authorization", "Bearer $jwt")
                        append("Accept", "application/x-ndjson")
                    }
                }
            }
                .execute { response ->
                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead && scope.isActive) {
                        val json = channel.readUTF8Line(1000)
                        if (json != null) {
                            println("Received from $uri $json")
                            val receivable = Json.decodeFromString<AttoReceivable>(json)
                            _receivableFlow.emit(receivable)
                        }
                    }
                }
        }

        run("transactions") {
            val fromHeight = accountState.value?.height ?: 1U
            val toHeight = Long.MAX_VALUE
            val uri =
                "$endpoint/accounts/$publicKey/transactions/stream?fromHeight=$fromHeight&toHeight=$toHeight"
            val jwt = authenticator.getAuthorization()

            httpClient.prepareGet(uri) {
                timeout {
                    socketTimeoutMillis = Long.MAX_VALUE
                }
                headers {
                    runBlocking {
                        append("Authorization", "Bearer $jwt")
                        append("Accept", "application/x-ndjson")
                    }
                }
            }
                .execute { response ->
                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead && scope.isActive) {
                        val json = channel.readUTF8Line(1000)
                        if (json != null) {
                            println("Received from $uri $json")
                            val transaction = Json.decodeFromString<AttoTransaction>(json)
                            _transactionFlow.emit(transaction)
                        }
                    }
                }
        }
    }

    override fun close() {
        scope.cancel()
    }


    private suspend fun run(name: String, runnable: suspend () -> Any) {
        scope.launch {
            while (scope.isActive) {
                try {
                    runnable.invoke()
                } catch (e: Exception) {
                    println("Failed to stream $name due to ${e.message}. ${e.stackTraceToString()}")
                    delay(1_000)
                }
            }
        }
    }

    suspend fun receive(receivable: AttoReceivable) {
        mutex.withLock {
            val account = accountState.value
            val (block, work) = if (account == null) {
                val block = AttoAccount.open(representative, receivable, AttoNetwork.LOCAL)
                val work = workerClient.work(block)
                block to work
            } else {
                val block = account.receive(receivable)
                val work = workerClient.work(block)
                block to work
            }

            val transaction = AttoTransaction(
                block = block,
                signature = signer.sign(block.hash),
                work = work
            )
            publish(transaction)
        }
    }

    suspend fun send(publicKey: AttoPublicKey, amount: AttoAmount) {
        mutex.withLock {
            val account =
                accountState.value ?: throw IllegalStateException("Account doesn't exist yet")

            if (account.balance < amount) {
                throw IllegalStateException("Insufficient balance!")
            }

            val block = account.send(AttoAlgorithm.V1, publicKey, amount)
            val transaction = AttoTransaction(
                block = block,
                signature = signer.sign(block.hash),
                work = workerClient.work(block)
            )
            publish(transaction)
        }
    }

    private suspend fun publish(transaction: AttoTransaction) {
        val uri = "$endpoint/transactions/stream"
        val json = Json.encodeToString(transaction)
        val authorization = authenticator.getAuthorization()
        val elapsed: Duration = measureTime {
            val response = httpClient.post(uri) {
                headers {
                    append("Authorization", "Bearer $authorization")
                    append("Accept", "application/x-ndjson")
                }
                setBody(json)
            }

            require(response.status.isSuccess()) {
                "Publishing $transaction failed with ${response.status}"
            }
        }

        println("Sent request to $uri $json in $elapsed")
    }
}
