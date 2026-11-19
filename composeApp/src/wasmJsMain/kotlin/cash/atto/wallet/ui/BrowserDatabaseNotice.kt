package cash.atto.wallet.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.components.common.AttoCircleIconButton
import cash.atto.wallet.datasource.BrowserDatabaseStorage

@Composable
internal fun BrowserDatabaseNotice(modifier: Modifier = Modifier) {
    val temporary by BrowserDatabaseStorage.isTemporary.collectAsState()
    var dismissed by rememberSaveable { mutableStateOf(false) }
    if (!temporary || dismissed) return

    AttoWalletTheme {
        AttoCard(
            modifier =
                modifier
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .pointerInput(Unit) { detectTapGestures { } }
                    .semantics { liveRegion = LiveRegionMode.Polite },
            border = dark_warning.copy(alpha = 0.3f),
            contentPadding = PaddingValues(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Temporary storage",
                        color = dark_warning,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text =
                            "Cached transaction history and work are kept only until this tab reloads or closes. " +
                                "Keep your recovery phrase backed up.",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                AttoCircleIconButton(
                    icon = Icons.Outlined.Close,
                    contentDescription = "Dismiss temporary storage notice",
                    background = Color.Transparent,
                    onClick = { dismissed = true },
                )
            }
        }
    }
}
