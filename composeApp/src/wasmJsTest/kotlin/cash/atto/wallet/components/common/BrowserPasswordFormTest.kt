package cash.atto.wallet.components.common

import androidx.compose.ui.text.input.ImeAction
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.w3c.dom.EventInit
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.KeyboardEventInit
import kotlin.coroutines.resume
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BrowserPasswordFormTest {
    @Test
    fun `silent autofill updates eligibility once without native value events`() =
        runTest {
            // Given
            val container = document.createElement("div") as HTMLDivElement
            val form = BrowserPasswordForm("browser-password-observer-test", {})
            val button = document.createElement("button") as HTMLButtonElement
            var passwordValue = ""
            var confirmationValue = ""
            var changes = 0
            val updateEligibility = {
                button.disabled = passwordValue.isEmpty() || passwordValue != confirmationValue
            }
            val password =
                input(form, container, "password", ImeAction.Next) {
                    passwordValue = it
                    changes++
                    updateEligibility()
                }
            val confirmation =
                input(form, container, "confirm-password") {
                    confirmationValue = it
                    changes++
                    updateEligibility()
                }
            button.disabled = true
            container.appendChild(form.element)
            container.appendChild(button)
            assertNotNull(document.body).appendChild(container)
            try {
                // When
                password.element.value = "Generated7!"
                confirmation.element.value = "Generated7!"
                password.updateValue("")
                confirmation.updateValue("")
                awaitAnimationFrame()

                // Then
                assertFalse(button.disabled)
                assertEquals("Generated7!", passwordValue)
                assertEquals("Generated7!", confirmationValue)
                assertEquals(2, changes)

                // When
                password.element.dispatchEvent(Event("input"))
                password.element.dispatchEvent(Event("change"))
                awaitAnimationFrame()

                // Then
                assertEquals(2, changes)

                // When
                form.dispose()
                password.element.value = "detached autofill"
                awaitAnimationFrame()

                // Then
                assertEquals(2, changes)
            } finally {
                form.dispose()
                container.remove()
            }
        }

    @Test
    fun `separately rendered password inputs share their native form owner`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password", ImeAction.Next)
            val confirmation = input(form, container, "confirm-password")

            // When
            val passwordOwner = password.element.form
            val confirmationOwner = confirmation.element.form

            // Then
            assertEquals(form.element, passwordOwner)
            assertEquals(form.element, confirmationOwner)
            assertEquals("browser-password-test-password", password.element.id)
            assertEquals("confirm-password", confirmation.element.name)
            assertEquals("password", password.element.type)
            assertEquals("next", password.element.getAttribute("enterkeyhint"))
            assertEquals(2, form.element.elements.length)
        }

    @Test
    fun `input and change events use the latest value callback`() =
        withPasswordForm { form, container ->
            // Given
            var oldCalls = 0
            val values = mutableListOf<String>()
            val password = input(form, container, "password", onValueChange = { oldCalls++ })
            password.onValueChange = { values.add(it) }

            // When
            password.element.value = "typed"
            password.element.dispatchEvent(Event("input"))
            password.element.value = "autofilled"
            password.element.dispatchEvent(Event("change"))

            // Then
            assertEquals(0, oldCalls)
            assertEquals(listOf("typed", "autofilled"), values)
        }

    @Test
    fun `submission reads silent autofill preserved through unrelated state updates`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password", ImeAction.Next)
            val confirmation = input(form, container, "confirm-password")
            var submitted: PasswordFormValues? = null
            form.onSubmit = { submitted = it }
            password.element.value = "Generated7!"
            confirmation.element.value = "Generated7!"

            // When
            password.updateValue("")
            confirmation.updateValue("")
            form.requestSubmit()

            // Then
            assertEquals(PasswordFormValues("Generated7!", "Generated7!"), submitted)
            password.updateValue("cleared explicitly")
            assertEquals("cleared explicitly", password.element.value)
        }

    @Test
    fun `native button submission is canceled before the latest callback`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password")
            password.element.value = "Synthetic7!"
            var oldCalls = 0
            var currentCalls = 0
            var submitEvent: Event? = null
            form.onSubmit = { oldCalls++ }
            form.element.addEventListener("submit", { event -> submitEvent = event })
            form.onSubmit = { currentCalls++ }
            val button =
                (document.createElement("button") as HTMLButtonElement).apply {
                    type = "submit"
                    setAttribute("form", form.element.id)
                }
            container.appendChild(button)

            // When
            button.click()

            // Then
            assertEquals(0, oldCalls)
            assertEquals(1, currentCalls)
            assertTrue(assertNotNull(submitEvent).defaultPrevented)
        }

    @Test
    fun `submit event is canceled even when application rejects the submission`() =
        withPasswordForm { form, container ->
            // Given
            input(form, container, "password")
            var calls = 0
            form.onSubmit = { calls++ }
            val event = Event("submit", EventInit(bubbles = true, cancelable = true))

            // When
            val allowed = form.element.dispatchEvent(event)

            // Then
            assertFalse(allowed)
            assertTrue(event.defaultPrevented)
            assertEquals(1, calls)
        }

    @Test
    fun `next focuses confirmation and done submits only once`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password", ImeAction.Next)
            val confirmation = input(form, container, "confirm-password")
            var calls = 0
            form.onSubmit = { calls++ }
            password.element.focus()

            // When
            val next = enter()
            password.element.dispatchEvent(next)

            // Then
            assertTrue(next.defaultPrevented)
            assertEquals(confirmation.element, document.activeElement)
            assertEquals(0, calls)

            // When
            val done = enter()
            confirmation.element.dispatchEvent(done)
            val repeated = enter(repeat = true)
            confirmation.element.dispatchEvent(repeated)

            // Then
            assertTrue(done.defaultPrevented)
            assertTrue(repeated.defaultPrevented)
            assertEquals(1, calls)
        }

    @Test
    fun `composing enter does not submit or move native focus`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password")
            var calls = 0
            form.onSubmit = { calls++ }
            password.element.focus()
            val composing = enter(isComposing = true)

            // When
            password.element.dispatchEvent(composing)

            // Then
            assertFalse(composing.defaultPrevented)
            assertEquals(0, calls)
            assertEquals(password.element, document.activeElement)
        }

    @Test
    fun `revealing preserves selection and does not steal focus from another field`() =
        withPasswordForm { form, container ->
            // Given
            val password = input(form, container, "password", ImeAction.Next)
            val confirmation = input(form, container, "confirm-password")
            password.updateValue("Synthetic7!")
            password.element.focus()
            password.element.setSelectionRange(2, 6, "forward")

            // When
            password.setRevealed(true)

            // Then
            assertEquals("text", password.element.type)
            assertEquals("Synthetic7!", password.element.value)
            assertEquals(password.element, document.activeElement)
            assertEquals(2, password.element.selectionStart)
            assertEquals(6, password.element.selectionEnd)

            // When
            confirmation.element.focus()
            password.setRevealed(false)

            // Then
            assertEquals("password", password.element.type)
            assertEquals(confirmation.element, document.activeElement)
        }

    @Test
    fun `disposing an input clears its secret and unregisters event and submit behavior`() =
        withPasswordForm { form, container ->
            // Given
            var changes = 0
            val password = input(form, container, "password", onValueChange = { changes++ })
            password.element.value = "Synthetic7!"
            var submitted: PasswordFormValues? = null
            form.onSubmit = { submitted = it }

            // When
            password.dispose()
            password.dispose()
            password.element.dispatchEvent(Event("input"))
            password.element.dispatchEvent(enter())
            password.updateValue("must not return")
            form.requestSubmit()

            // Then
            assertEquals("", password.element.value)
            assertFalse(password.element.isConnected)
            assertEquals(0, changes)
            assertEquals(PasswordFormValues(""), submitted)
        }

    @Test
    fun `disposing a form clears its inputs and retained controllers become inert`() =
        withPasswordForm { form, container ->
            // Given
            var changes = 0
            var submissions = 0
            val password = input(form, container, "password", ImeAction.Next, onValueChange = { changes++ })
            val confirmation = input(form, container, "confirm-password")
            password.element.value = "Synthetic7!"
            confirmation.element.value = "Synthetic7!"
            form.onSubmit = { submissions++ }

            // When
            form.dispose()
            form.dispose()
            form.requestSubmit()
            form.element.dispatchEvent(Event("submit", EventInit(cancelable = true)))
            password.element.dispatchEvent(Event("input"))
            confirmation.element.dispatchEvent(enter())

            // Then
            assertEquals("", password.element.value)
            assertEquals("", confirmation.element.value)
            assertFalse(password.element.isConnected)
            assertFalse(confirmation.element.isConnected)
            assertFalse(form.element.isConnected)
            assertEquals(0, changes)
            assertEquals(0, submissions)
        }

    private fun input(
        form: BrowserPasswordForm,
        container: HTMLDivElement,
        name: String,
        imeAction: ImeAction = ImeAction.Done,
        onValueChange: (String) -> Unit = {},
    ): BrowserPasswordInput =
        BrowserPasswordInput(form, name, "", onValueChange, imeAction).also {
            container.appendChild(it.element)
        }

    private fun enter(
        repeat: Boolean = false,
        isComposing: Boolean = false,
    ) = KeyboardEvent("keydown", KeyboardEventInit(key = "Enter", repeat = repeat, isComposing = isComposing, cancelable = true))

    private fun withPasswordForm(test: (BrowserPasswordForm, HTMLDivElement) -> Unit) {
        val container = document.createElement("div") as HTMLDivElement
        val form = BrowserPasswordForm("browser-password-test", {})
        container.appendChild(form.element)
        assertNotNull(document.body).appendChild(container)
        try {
            test(form, container)
        } finally {
            form.dispose()
            container.remove()
        }
    }

    private suspend fun awaitAnimationFrame() =
        suspendCancellableCoroutine<Unit> { continuation ->
            val frame = window.requestAnimationFrame { continuation.resume(Unit) }
            continuation.invokeOnCancellation { window.cancelAnimationFrame(frame) }
        }
}
