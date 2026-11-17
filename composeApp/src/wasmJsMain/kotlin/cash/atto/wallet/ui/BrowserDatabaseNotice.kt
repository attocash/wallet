package cash.atto.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.datasource.BrowserDatabaseStorage

@Composable
internal fun BrowserDatabaseNotice() {
    val temporary by BrowserDatabaseStorage.isTemporary.collectAsState()
    if (!temporary) return

    AttoWalletTheme {
        Box(
            modifier = Modifier.fillMaxWidth().background(dark_bg).padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            AttoCard(
                modifier = Modifier.widthIn(max = 1400.dp),
                border = dark_warning.copy(alpha = 0.3f),
                contentPadding = PaddingValues(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            }
        }
    }
}
