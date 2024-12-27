package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_atto
import attowallet.composeapp.generated.resources.main_title
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.settings.ProfileUiState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProfileSmall(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = uiState.hash,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ProfileExtended(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_atto),
            contentDescription = "Atto",
            modifier = Modifier.size(28.dp, 28.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(Res.string.main_title),
            modifier = Modifier.padding(start = 4.dp),
            fontSize = 26.sp,
            fontWeight = FontWeight.W300,
            fontFamily = attoFontFamily(),
        )

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiState.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = uiState.hash,
                modifier = Modifier.width(180.dp),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
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