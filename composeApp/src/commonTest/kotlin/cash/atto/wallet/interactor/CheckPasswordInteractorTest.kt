package cash.atto.wallet.interactor

import cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckPasswordInteractorTest {
    private val checkPassword = CheckPasswordInteractor()

    @Test
    fun `random manager passwords need no symbol to be accepted`() {
        // Given
        val passwords = listOf("fK7q9Vm2xT6n4Rb", "fK7q9Vm2xT6n4Rb8")

        // When
        val assessments = passwords.map(checkPassword::assess)

        // Then
        assertEquals(15, passwords.first().length)
        assertTrue(assessments.all { it.score == 4 })
        assertTrue(assessments.all { it.isAcceptable })
        assertTrue(passwords.all { checkPassword.invoke(it) == PasswordCheckState.VALID })
    }

    @Test
    fun `lowercase passphrases need no uppercase digits or symbols`() {
        // Given
        val password = "amber otter acorn violet monsoon"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertTrue(assessment.minimumLengthMet)
        assertTrue(assessment.isAcceptable)
    }

    @Test
    fun `fair and good passwords pass the minimum strength threshold`() {
        // Given
        val passwords = listOf("Password123456!" to 2, "AttoWallet2026!!" to 3)

        // When
        val assessments = passwords.map { (password, _) -> checkPassword.assess(password) }

        // Then
        assertEquals(passwords.map { it.second }, assessments.map { it.score })
        assertTrue(assessments.all { it.minimumLengthMet && it.isAcceptable })
        assertTrue(passwords.all { (password, _) -> checkPassword.invoke(password) == PasswordCheckState.VALID })
    }

    @Test
    fun `very weak and weak passwords remain below the strength threshold`() {
        // Given
        val passwords =
            listOf(
                "passwordpassword",
                "12345678901234567890",
                "aaaaaaaaaaaaaaaaaaaaaaaa",
                "qwertyuiopasdfghjkl",
            )

        // When
        val assessments = passwords.map(checkPassword::assess)

        // Then
        assertTrue(assessments.all { it.minimumLengthMet })
        assertEquals(setOf(0, 1), assessments.map { it.score }.toSet())
        assessments.forEachIndexed { index, assessment ->
            assertFalse(assessment.isAcceptable, "Predictable case $index received score ${assessment.score}")
        }
    }

    @Test
    fun `short passwords remain ineligible even if their characters look random`() {
        // Given
        val password = "fK7q9Vm2xT6n4R"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertFalse(assessment.minimumLengthMet)
        assertFalse(assessment.isAcceptable)
        assertEquals(PasswordCheckState.INVALID, checkPassword.invoke(password))
    }

    @Test
    fun `minimum length counts Unicode code points rather than surrogate halves`() {
        // Given
        val eightCharacters = "😀".repeat(8)
        val fifteenCharacters = "😀".repeat(15)

        // When
        val short = checkPassword.assess(eightCharacters)
        val long = checkPassword.assess(fifteenCharacters)

        // Then
        assertFalse(short.minimumLengthMet)
        assertTrue(long.minimumLengthMet)
        assertFalse(long.isAcceptable)
    }

    @Test
    fun `long passphrases remain supported beyond sixty four characters`() {
        // Given
        val password = "amber otter acorn violet monsoon lantern glacier meadow sapphire thimble"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertTrue(password.length > 64)
        assertTrue(assessment.isAcceptable)
    }

    @Test
    fun `missing input has no estimated strength and cannot be saved`() {
        // Given
        val passwords = listOf(null, "")

        // When
        val assessments = passwords.map(checkPassword::assess)

        // Then
        assertTrue(assessments.all { it.score == 0 })
        assertTrue(assessments.none { it.isAcceptable })
    }
}
