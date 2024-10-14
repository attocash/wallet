package cash.atto.wallet.repository

import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoHash
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoSignature
import cash.atto.commons.fromHexToByteArray
import cash.atto.commons.sign
import cash.atto.wallet.Config
import cash.atto.wallet.state.AppState
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class AuthRepository(private val httpClient: HttpClient) {
    private val leeway = 60.seconds.inWholeSeconds
    private val loginUri = "${Config.ENDPOINT}/login"
    private val challengeUri = "$loginUri/challenges"
    private var jwt: DecodedJWT? = null

    private val mutex = Mutex()

    suspend fun getAuthorization(appState: AppState): String? {
        mutex.withLock {
            val jwt = this.jwt
            if (jwt == null || jwt.expiresAtAsInstant < Instant.now().minusSeconds(leeway)) {
                if (appState.privateKey == null || appState.publicKey == null)
                    return null

                return login(appState)
            }
            return jwt.token
        }
    }

    private suspend fun login(appState: AppState): String {
        val initRequest = TokenInitRequest(AttoAlgorithm.V1, appState.publicKey!!)
        val initResponse = httpClient.post(loginUri) {
            contentType(ContentType.Application.Json)
            setBody(initRequest)
        }.body<TokenInitResponse>()


        val challenge = initResponse.challenge.fromHexToByteArray()
        val hash = AttoHash.hash(64, appState.publicKey.value, challenge)

        val signature = appState.privateKey!!.sign(hash)
        val answer = TokenInitAnswer(signature)

        val challengeUrl = "$challengeUri/${initResponse.challenge}"
        println("Challenge URL $challengeUrl")
        val jwtString = httpClient.post(challengeUrl) {
            contentType(ContentType.Application.Json)
            setBody(answer)
        }.body<String>()

        jwt = JWT.decode(jwtString)
        println("JWT ${jwt?.token}")
        return jwtString
    }
}

@Serializable
data class TokenInitRequest(
    val algorithm: AttoAlgorithm,
    val publicKey: AttoPublicKey,
)

@Serializable
data class TokenInitResponse(
    val challenge: String,
)

@Serializable
data class TokenInitAnswer(
    val signature: AttoSignature,
)