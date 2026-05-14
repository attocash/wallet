package cash.atto.wallet.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class WorkPreferenceTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    @Test
    fun `remote preference stores remote rate limit`() {
        val preference = WorkPreference.forSource(WorkSourcePreference.REMOTE)

        assertEquals(WorkSourcePreference.REMOTE, preference.source)
        assertEquals(10, preference.rateLimitSeconds)
        assertEquals(10.seconds, preference.rateLimit)
        assertEquals(
            """{"source":"REMOTE","rateLimitSeconds":10}""",
            json.encodeToString(WorkPreference.serializer(), preference),
        )
    }

    @Test
    fun `local preference stores no rate limit`() {
        val preference = WorkPreference.forSource(WorkSourcePreference.LOCAL)

        assertEquals(WorkSourcePreference.LOCAL, preference.source)
        assertEquals(0, preference.rateLimitSeconds)
        assertEquals(0.seconds, preference.rateLimit)
        assertEquals(
            """{"source":"LOCAL","rateLimitSeconds":0}""",
            json.encodeToString(WorkPreference.serializer(), preference),
        )
    }

    @Test
    fun `normalization applies rate limit from source`() {
        val preference =
            json
                .decodeFromString(
                    WorkPreference.serializer(),
                    """{"source":"LOCAL"}""",
                ).normalized()

        assertEquals(WorkSourcePreference.LOCAL, preference.source)
        assertEquals(0, preference.rateLimitSeconds)
    }
}
