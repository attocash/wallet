package cash.atto.wallet.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_atto
import attowallet.composeapp.generated.resources.main_title
import attowallet.composeapp.generated.resources.profile_lock
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.ProfileUiState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileSmall(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = uiState.hash,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun ProfileExtended(
    uiState: ProfileUiState,
    onLockClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_atto),
            contentDescription = "Atto",
            modifier = Modifier.size(28.dp, 28.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(Res.string.main_title),
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 26.sp,
            fontWeight = FontWeight.W300,
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = uiState.hash,
                modifier = Modifier.width(180.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        onLockClick?.let {
            Row(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(onClick = it)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(Res.string.profile_lock),
                    tint = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.profile_lock),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview
@Composable
fun ProfileSmallPreview() {
    AttoWalletTheme {
        ProfileSmall(ProfileUiState.DEFAULT)
    }
}

@Preview
@Composable
fun ProfileExtendedPreview() {
    AttoWalletTheme {
        ProfileExtended(ProfileUiState.DEFAULT)
    }
}
