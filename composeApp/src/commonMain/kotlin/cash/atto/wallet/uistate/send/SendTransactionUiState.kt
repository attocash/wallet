package cash.atto.wallet.uistate.send

data class SendTransactionUiState(
    val sendFromUiState: SendFromUiState,
    val sendConfirmUiState: SendConfirmUiState
) {
    companion object {
        val DEFAULT = SendTransactionUiState(
            sendFromUiState = SendFromUiState.DEFAULT,
            sendConfirmUiState = SendConfirmUiState.DEFAULT
        )
    }
}