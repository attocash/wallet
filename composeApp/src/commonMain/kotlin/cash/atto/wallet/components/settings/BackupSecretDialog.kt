package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.*
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoWordChip
import cash.atto.wallet.ui.isCompactWidth
import cash.atto.wallet.viewmodel.BackupSecretViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupSecretDialog(onDismiss: () -> Unit) {
    val viewModel = koinViewModel<BackupSecretViewModel>()
    val uiState by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    AttoModal(
        title = stringResource(Res.string.secret_title),
        onDismiss = onDismiss,
        desktopWidth = 560.dp,
        contentPadding = PaddingValues(20.dp),
    ) {
        BackupWordGrid(
            words = uiState.words,
            hidden = uiState.hidden,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoButton(
                variant = AttoButtonVariant.Outlined,
                text =
                    stringResource(
                        if (uiState.hidden) {
                            Res.string.settings_backup_show
                        } else {
                            Res.string.settings_backup_hide
                        },
                    ),
                onClick = {
                    if (uiState.hidden) {
                        viewModel.showSecretPhrase()
                    } else {
                        viewModel.hideSecretPhrase()
                    }
                },
                icon =
                    if (uiState.hidden) {
                        Icons.Outlined.Visibility
                    } else {
                        Icons.Outlined.VisibilityOff
                    },
                modifier = Modifier.weight(1f),
            )

            AttoButton(
                text = stringResource(Res.string.secret_copy),
                onClick = {
                    clipboardManager.setText(
                        AnnotatedString(uiState.words.joinToString(" ")),
                    )
                },
                icon = Icons.Outlined.ContentCopy,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BackupWordGrid(
    words: List<String>,
    hidden: Boolean,
    modifier: Modifier = Modifier,
) {
    val midpoint = (words.size + 1) / 2
    val leftColumn = words.take(midpoint)
    val rightColumn = words.drop(midpoint)

    BoxWithConstraints(modifier = modifier) {
        val compact = isCompactWidth()

        if (compact) {
            Column(
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
                modifier = Modifier.fillMaxWidth(),
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
}
