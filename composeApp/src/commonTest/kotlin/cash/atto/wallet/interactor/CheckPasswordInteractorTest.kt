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
        val password = "fK7q9Vm"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertEquals(7, password.length)
        assertEquals(7, assessment.characterCount)
        assertTrue(assessment.score > 0)
        assertFalse(assessment.minimumLengthMet)
        assertFalse(assessment.isAcceptable)
        assertEquals(PasswordCheckState.INVALID, checkPassword.invoke(password))
    }

    @Test
    fun `random eight character passwords meet the creation requirements`() {
        // Given
        val password = "fK7q9Vm2"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertEquals(8, password.length)
        assertEquals(8, assessment.characterCount)
        assertTrue(assessment.minimumLengthMet)
        assertTrue(assessment.score >= 2)
        assertTrue(assessment.isAcceptable)
        assertEquals(PasswordCheckState.VALID, checkPassword.invoke(password))
    }

    @Test
    fun `predictable eight character passwords still fail the strength requirement`() {
        // Given
        val password = "password"

        // When
        val assessment = checkPassword.assess(password)

        // Then
        assertEquals(8, password.length)
        assertEquals(8, assessment.characterCount)
        assertTrue(assessment.minimumLengthMet)
        assertTrue(assessment.score < 2)
        assertFalse(assessment.isAcceptable)
        assertEquals(PasswordCheckState.INVALID, checkPassword.invoke(password))
    }

    @Test
    fun `minimum length counts Unicode code points rather than surrogate halves`() {
        // Given
        val sevenCharacters = "😀".repeat(7)
        val eightCharacters = "😀".repeat(8)

        // When
        val short = checkPassword.assess(sevenCharacters)
        val long = checkPassword.assess(eightCharacters)

        // Then
        assertEquals(7, short.characterCount)
        assertEquals(8, long.characterCount)
        assertFalse(short.minimumLengthMet)
        assertTrue(long.minimumLengthMet)
        assertFalse(long.isAcceptable)
    }

    @Test
    fun `unpaired surrogates each count as one character`() {
        // Given
        val passwords = listOf("fK7q9V\uD83D", "fK7q9Vm\uDE00")

        // When
        val assessments = passwords.map(checkPassword::assess)

        // Then
        assertEquals(listOf(7, 8), assessments.map { it.characterCount })
        assertEquals(listOf(false, true), assessments.map { it.minimumLengthMet })
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
        assertTrue(assessments.all { it.characterCount == 0 })
        assertTrue(assessments.all { it.score == 0 })
        assertTrue(assessments.none { it.isAcceptable })
    }
}
