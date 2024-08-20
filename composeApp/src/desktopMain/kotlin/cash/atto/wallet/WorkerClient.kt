package cash.atto.wallet

import cash.atto.commons.AttoBlock
import cash.atto.commons.AttoOpenBlock
import cash.atto.commons.AttoWork
import cash.atto.commons.PreviousSupport
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


class WorkerClient(private val endpoint: String) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun work(block: AttoOpenBlock): AttoWork {
        return this.work(block.timestamp, block.publicKey.toString())
    }

    suspend fun <T> work(block: T): AttoWork where T : PreviousSupport, T : AttoBlock {
        return this.work(block.timestamp, block.previous.toString())
    }

    private suspend fun work(timestamp: Instant, target: String): AttoWork {
        val uri = "$endpoint/works"

        val request = WorkRequest(
            timestamp = timestamp,
            target = target
        )

        return httpClient.post(uri) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AttoWork>()
    }

    @Serializable
    data class WorkRequest(
        val timestamp: Instant,
        val target: String,
    )

}