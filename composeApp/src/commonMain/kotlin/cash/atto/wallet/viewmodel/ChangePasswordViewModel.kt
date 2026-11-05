package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.wallet.interactor.CheckPasswordInteractor
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.WalletStorageException
import cash.atto.wallet.uistate.settings.ChangePasswordUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChangePasswordViewModel(
    private val appStateRepository: AppStateRepository,
    private val checkPasswordInteractor: CheckPasswordInteractor,
) : ViewModel() {
    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state = _state.asStateFlow()

    fun reset() {
        if (!state.value.saving) _state.value = ChangePasswordUiState()
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmation: String,
    ) {
        if (state.value.saving || state.value.changed) return
        val validationError =
            when {
                currentPassword.isEmpty() -> "Enter your current password."
                !checkPasswordInteractor.assess(newPassword).isAcceptable -> "Choose a stronger new password."
                newPassword != confirmation -> "The new passwords do not match."
                newPassword == currentPassword -> "Choose a password different from your current password."
                else -> null
            }
        if (validationError != null) {
            _state.value = ChangePasswordUiState(error = validationError)
            return
        }

        _state.value = ChangePasswordUiState(saving = true)
        try {
            _state.value =
                if (appStateRepository.changePassword(currentPassword, newPassword)) {
                    ChangePasswordUiState(changed = true)
                } else {
                    ChangePasswordUiState(error = "The current password is incorrect.", incorrectCurrentPassword = true)
                }
        } catch (error: CancellationException) {
            throw error
        } catch (error: WalletStorageException) {
            _state.value = ChangePasswordUiState(error = error.message ?: "Unable to change your password. Please try again.")
        } catch (_: Exception) {
            _state.value = ChangePasswordUiState(error = "Unable to change your password. Please try again.")
        } finally {
            if (state.value.saving) _state.value = ChangePasswordUiState()
        }
    }
}
