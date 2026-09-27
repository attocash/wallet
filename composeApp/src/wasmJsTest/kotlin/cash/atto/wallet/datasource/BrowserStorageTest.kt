@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package cash.atto.wallet.datasource

import cash.atto.wallet.repository.WalletStorageException
import cash.atto.wallet.support.LEGACY_PREFERENCES_KEY
import cash.atto.wallet.support.failStorageWritesTo
import cash.atto.wallet.support.installLegacyPreferences
import cash.atto.wallet.support.restoreStorageWrites
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BrowserStorageTest {
    @Test
    fun `legacy preferences are read without mutation and migrated with all settings`() =
        runTest {
            // Given: encoded using the actual DataStore serializer used by the old wallet.
            val encrypted = """{"version":2,"ciphertext":"synthetic"}"""
            val legacy = installLegacyPreferences(encrypted)
            val storage = PreferencesDataSource()

            // When / Then: reads alone do not migrate or require writable storage.
            assertEquals(encrypted, storage.blob.first())
            assertEquals("2026-09-14", storage.termsAndConditionsDate.first())
            assertEquals(legacy, localStorage.getItem(LEGACY_PREFERENCES_KEY))
            assertNull(localStorage.getItem("preferences"))
            storage.migrateStorage()
            val record = Json.parseToJsonElement(assertNotNull(localStorage.getItem("preferences"))).jsonObject
            assertEquals(Json.parseToJsonElement(encrypted), record["wallet"])
            assertEquals(
                "preserve",
                record
                    .getValue("work")
                    .jsonObject
                    .getValue("future")
                    .jsonPrimitive.content,
            )
            assertTrue(
                record
                    .getValue("b/future_setting")
                    .jsonPrimitive.content
                    .toBoolean(),
            )
            assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
            val saved = localStorage.getItem("preferences")
            storage.migrateStorage()
            assertEquals(saved, localStorage.getItem("preferences"))
        }

    @Test
    fun `failed preference migration preserves the legacy container and retries`() =
        runTest {
            // Given
            val legacy = installLegacyPreferences("unreadable ciphertext")
            val storage = PreferencesDataSource()
            failStorageWritesTo("preferences")
            try {
                // When / Then
                assertFailsWith<WalletStorageException> { storage.migrateStorage() }
                assertEquals(legacy, localStorage.getItem(LEGACY_PREFERENCES_KEY))
                assertNull(localStorage.getItem("preferences"))
                assertEquals("unreadable ciphertext", storage.blob.first())
            } finally {
                restoreStorageWrites()
            }
            storage.migrateStorage()
            assertEquals("unreadable ciphertext", storage.blob.first())
            assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
        }

    @Test
    fun `changing public settings migrates the container without changing encrypted preferences`() =
        runTest {
            // Given
            installLegacyPreferences("  malformed encrypted data  ")
            val storage = PreferencesDataSource()

            // When
            storage.clearTermsAndConditionsDate()
            storage.setWork("""{"source":"REMOTE"}""")
            storage.setTermsAndConditionsDate("2026-09-15")

            // Then
            assertEquals("  malformed encrypted data  ", storage.blob.first())
            assertEquals("2026-09-15", storage.termsAndConditionsDate.first())
            assertEquals("""{"source":"REMOTE"}""", storage.work.first())
            assertNull(localStorage.getItem(LEGACY_PREFERENCES_KEY))
        }

    @Test
    fun `preferences observe writes from another instance and other browser tabs`() =
        runTest {
            // Given
            localStorage.removeItem(LEGACY_PREFERENCES_KEY)
            localStorage.removeItem("preferences")
            val reader = PreferencesDataSource()
            val localChange = async(start = CoroutineStart.UNDISPATCHED) { reader.work.first { it == "local" } }
            runCurrent()

            // When / Then
            PreferencesDataSource().setWork("local")
            assertEquals("local", localChange.await())
            val remoteChange = async(start = CoroutineStart.UNDISPATCHED) { reader.work.first { it == "remote" } }
            runCurrent()
            localStorage.setItem("preferences", """{"work":"remote"}""")
            notifyStorageChange()
            assertEquals("remote", remoteChange.await())
        }

    @Test
    fun `mnemonic key migration preserves the old value on failure and removes it after success`() =
        runTest {
            // Given
            localStorage.removeItem("mnemonic")
            localStorage.setItem("seed", "legacy encrypted mnemonic")
            val storage = SeedDataSource()
            assertEquals("legacy encrypted mnemonic", storage.getSeed())
            failStorageWritesTo("mnemonic")
            try {
                // When / Then
                assertFailsWith<WalletStorageException> { storage.setSeed("new encrypted mnemonic") }
                assertEquals("legacy encrypted mnemonic", localStorage.getItem("seed"))
                assertNull(localStorage.getItem("mnemonic"))
            } finally {
                restoreStorageWrites()
            }
            storage.setSeed("new encrypted mnemonic")
            assertEquals("new encrypted mnemonic", localStorage.getItem("mnemonic"))
            assertNull(localStorage.getItem("seed"))
            localStorage.setItem("seed", "stale encrypted mnemonic")
            assertEquals("new encrypted mnemonic", storage.getSeed())
            storage.clearSeed()
            assertNull(localStorage.getItem("mnemonic"))
            assertNull(localStorage.getItem("seed"))
        }
}

private fun notifyStorageChange(): Unit =
    js("window.dispatchEvent(new StorageEvent('storage', {key:'preferences', storageArea:localStorage}))")
