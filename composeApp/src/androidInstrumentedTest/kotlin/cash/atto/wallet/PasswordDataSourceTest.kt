package cash.atto.wallet

import androidx.test.ext.junit.rules.activityScenarioRule
import cash.atto.wallet.datasource.PasswordDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PasswordDataSourceTest {
    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    private lateinit var passwordDataSource: PasswordDataSource

    private val user1 = User(
        seed = "seed1",
        password = "password1"
    )

    private val user2 = User(
        seed = "seed2",
        password = "password2"
    )

    @Before
    fun createDataSource() {
        activityScenarioRule.scenario
            .onActivity {
                passwordDataSource = PasswordDataSource(it)
            }
    }

    @Test
    fun testClear() {
        activityScenarioRule.scenario
            .onActivity {
                runBlocking {
                    passwordDataSource.setPassword(user1.seed, user1.password)
                    passwordDataSource.clear()

                    val expected = null
                    val actual = passwordDataSource.getPassword(user1.seed)

                    assertEquals(expected, actual)
                }
            }
    }

    @Test
    fun testTransaction() {
        activityScenarioRule.scenario
            .onActivity {
                runBlocking {
                    passwordDataSource.clear()
                    passwordDataSource.setPassword(
                        user1.seed,
                        user1.password
                    )

                    val expected = user1.password
                    val actual = passwordDataSource.getPassword(user1.seed)

                    assertEquals(expected, actual)

                    passwordDataSource.clear()
                }
            }
    }

    @Test
    fun testMultipleTransactions() {
        activityScenarioRule.scenario
            .onActivity {
                runBlocking {
                    passwordDataSource.clear()

                    passwordDataSource.setPassword(
                        user1.seed,
                        user1.password
                    )

                    passwordDataSource.setPassword(
                        user2.seed,
                        user2.password
                    )

                    assertEquals(
                        user1.password,
                        passwordDataSource.getPassword(user1.seed)
                    )

                    assertEquals(
                        user2.password,
                        passwordDataSource.getPassword(user2.seed)
                    )

                    passwordDataSource.clear()
                }
            }
    }

    inner class User(
        val seed: String,
        val password: String
    )
}