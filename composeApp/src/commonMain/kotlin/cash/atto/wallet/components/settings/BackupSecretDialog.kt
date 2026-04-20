package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoWordChip
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupSecretDialog(
    onDismiss: () -> Unit,
    compact: Boolean = false,
) {
    val viewModel = koinViewModel<BackupSecretViewModel>()
    val uiState by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1000L)
            copied = false
        }
    }

    AttoModal(
        title = "Recovery Phrase",
        onDismiss = onDismiss,
    ) {
        BackupWordGrid(
            words = uiState.words,
            hidden = uiState.hidden,
            compact = compact,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoButton(
                variant = AttoButtonVariant.Outlined,
                text = if (uiState.hidden) "Show phrase" else "Hide phrase",
                onClick = {
                    if (uiState.hidden) {
                        viewModel.showSecretPhrase()
                    } else {
                        viewModel.hideSecretPhrase()
                    }
                },
                modifier = Modifier.weight(1f),
            )

            AttoButton(
                text = if (copied) "" else "Copy",
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString(uiState.words.joinToString(" ")),
                    )
                    copied = true
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BackupWordGrid(
    words: List<String>,
    hidden: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val midpoint = (words.size + 1) / 2
    val leftColumn = words.take(midpoint)
    val rightColumn = words.drop(midpoint)

    if (compact) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            words.forEachIndexed { index, word ->
                AttoWordChip(
                    ordinal = index + 1,
                    word = word,
                    hidden = hidden,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leftColumn.forEachIndexed { index, word ->
                    AttoWordChip(
                        ordinal = index + 1,
                        word = word,
                        hidden = hidden,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rightColumn.forEachIndexed { index, word ->
                    AttoWordChip(
                        ordinal = midpoint + index + 1,
                        word = word,
                        hidden = hidden,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
