package cash.atto.wallet

import cash.atto.commons.*
import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class Authenticator(
    private val endpoint: String,
    private val signer: Signer
) {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private val leeway = 60.seconds.inWholeSeconds
    private val loginUri = "$endpoint/login"
    private val challengeUri = "$loginUri/challenges"
    private var jwt: DecodedJWT? = null

    private val mutex = Mutex()

    suspend fun getAuthorization(): String? {
        mutex.withLock {
            val jwt = this.jwt
            if (jwt == null || jwt.expiresAtAsInstant < Instant.now().minusSeconds(leeway)) {
                return login()
            }
            return jwt.token
        }
    }

    private suspend fun login(): String {
        val initRequest = TokenInitRequest(AttoAlgorithm.V1, signer.publicKey)
        val initResponse = httpClient.post(loginUri) {
            contentType(ContentType.Application.Json)
            setBody(initRequest)
        }.body<TokenInitResponse>()


        val challenge = initResponse.challenge.fromHexToByteArray()
        val hash = AttoHash.hash(64, signer.publicKey.value, challenge)

        val signature = signer.sign(hash)
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