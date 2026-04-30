package cash.atto.wallet.model

import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesTest {
    @Test
    fun `default preferences keep account zero active`() {
        assertEquals(
            AccountPreferenceStatus.ACTIVATED,
            UserPreferences.EMPTY.accountStatus(0U),
        )
    }

    @Test
    fun `normalization preserves deactivated account when another account is active`() {
        val preferences =
            UserPreferences(
                accounts =
                    mapOf(
                        "0" to AccountPreference(AccountPreferenceStatus.DEACTIVATED),
                        "1" to AccountPreference(AccountPreferenceStatus.ACTIVATED),
                    ),
            )

        assertEquals(AccountPreferenceStatus.DEACTIVATED, preferences.accountStatus(0U))
        assertEquals(AccountPreferenceStatus.ACTIVATED, preferences.accountStatus(1U))
    }

    @Test
    fun `normalization limits active accounts to five`() {
        val preferences =
            UserPreferences(
                accounts =
                    (0U..6U).associate { index ->
                        index.toString() to AccountPreference(AccountPreferenceStatus.ACTIVATED)
                    },
            )

        assertEquals(listOf(0U, 1U, 2U, 3U, 4U), preferences.activeAccountIndexes())
        assertEquals(AccountPreferenceStatus.DEACTIVATED, preferences.accountStatus(5U))
        assertEquals(AccountPreferenceStatus.DEACTIVATED, preferences.accountStatus(6U))
    }

    @Test
    fun `next account uses first unknown index`() {
        val preferences =
            UserPreferences(
                accounts =
                    mapOf(
                        "0" to AccountPreference(AccountPreferenceStatus.ACTIVATED),
                        "2" to AccountPreference(AccountPreferenceStatus.DEACTIVATED),
                    ),
            )

        assertEquals(1U, preferences.nextAvailableAccountIndex())
    }
}
