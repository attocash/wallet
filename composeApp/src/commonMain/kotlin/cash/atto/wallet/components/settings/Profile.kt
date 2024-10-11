package cash.atto.wallet.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.ProfileUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Profile(
    uiState: ProfileUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier.padding(16.dp)) {
        Column(Modifier.align(Alignment.TopStart)) {
            Text(uiState.name)
            Text(uiState.hash)
        }

        Icon(
            modifier = Modifier.align(Alignment.TopEnd)
                .clickable {},
            imageVector = Icons.Filled.Person,
            contentDescription = "Accounts"
        )
    }
}

@Preview
@Composable
fun ProfilePreview() {
    AttoWalletTheme {
        Profile(ProfileUiState.DEFAULT)
    }
}