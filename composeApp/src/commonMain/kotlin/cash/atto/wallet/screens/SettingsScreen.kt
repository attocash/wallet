package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.settings_subtitle
import attowallet.composeapp.generated.resources.settings_title
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.settings.SettingsUiState
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBackClick: () -> Unit,
    onBackupClick: () -> Unit,
    onLockClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    AttoPageFrame(
        title = stringResource(Res.string.settings_title),
        subtitle = stringResource(Res.string.settings_subtitle),
        onBack = onBackClick,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = isCompactWidth()

            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SettingsActionsPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        onBackupClick = onBackupClick,
                        onLockClick = onLockClick,
                        onLogoutClick = onLogoutClick,
                    )
                    SettingsMetadataPanel(modifier = Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    SettingsActionsPanel(
                        modifier = Modifier.weight(5f),
                        uiState = uiState,
                        onBackupClick = onBackupClick,
                        onLockClick = onLockClick,
                        onLogoutClick = onLogoutClick,
                    )
                    SettingsMetadataPanel(modifier = Modifier.weight(7f))
                }
            }
        }
    }
}

@Composable
private fun SettingsActionsPanel(
    modifier: Modifier,
    uiState: SettingsUiState,
    onBackupClick: () -> Unit,
    onLockClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AttoSettingsActionRow(
            icon = Icons.Outlined.Key,
            title = "Recovery Phrase",
            subtitle = "View your 24-word recovery phrase",
            onClick = onBackupClick,
        )

        AttoSettingsActionRow(
            icon = Icons.AutoMirrored.Outlined.Logout,
            title = "Logout",
            subtitle = "Clear keys and return to welcome screen",
            accent = dark_danger,
            onClick = onLogoutClick,
        )
    }
}

@Composable
private fun SettingsMetadataPanel(modifier: Modifier) {
    AttoPanelCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        SettingsInfoRow(
            label = "Version",
            value = "1.0.0",
            valueColor = dark_text_primary,
        )
        HorizontalDivider(color = dark_border)
        SettingsInfoRow(
            label = "Network",
            value = "Live",
            valueColor = dark_success,
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String,
    valueColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500),
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.W500),
        )
    }
}

@Composable
private fun AttoSettingsActionRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accent: Color = dark_text_primary,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onClick() }
                .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = dark_text_primary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
            )
            subtitle?.let {
                Text(
                    text = it,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = if (accent == dark_danger) dark_danger else dark_text_dim,
            modifier = Modifier.size(20.dp),
        )
    }
}
