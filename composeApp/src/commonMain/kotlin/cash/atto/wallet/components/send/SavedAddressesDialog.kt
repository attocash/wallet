package cash.atto.wallet.components.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.model.LabeledPreferenceEntry
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_text_dim
import cash.atto.wallet.ui.dark_text_secondary

@Composable
fun SavedAddressesDialog(
    entries: List<LabeledPreferenceEntry>,
    onDismissRequest: () -> Unit,
    onSelectAddress: (String) -> Unit,
) {
    AttoModal(
        title = "Saved Addresses",
        onDismiss = onDismissRequest,
    ) {
        Text(
            text = "Choose one of your stored labeled addresses.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            entries
                .sortedBy { it.label.lowercase() }
                .forEach { entry ->
                    AttoCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        onClick = { onSelectAddress(entry.value) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = entry.label,
                                    modifier = Modifier.widthIn(max = 220.dp),
                                    color = dark_accent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = attoFontFamily(),
                                            fontWeight = FontWeight.W600,
                                            fontSize = 12.sp,
                                        ),
                                )
                                Text(
                                    text = entry.value,
                                    color = dark_text_dim,
                                    maxLines = 1,
                                    overflow = TextOverflow.MiddleEllipsis,
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.W500,
                                            fontSize = 12.sp,
                                        ),
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = dark_text_secondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
        }

        AttoButton(
            text = "Close",
            onClick = onDismissRequest,
            modifier = Modifier.fillMaxWidth(),
            variant = AttoButtonVariant.Outlined,
        )
    }
}
