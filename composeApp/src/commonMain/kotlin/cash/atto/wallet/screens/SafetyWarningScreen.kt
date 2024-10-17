package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.safety_warning_confirm
import attowallet.composeapp.generated.resources.safety_warning_description
import attowallet.composeapp.generated.resources.safety_warning_hint
import attowallet.composeapp.generated.resources.safety_warning_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SafetyWarningScreen(
    onBackNavigation: () -> Unit,
    onConfirmClicked: () -> Unit,
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.safety_warning_title),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.h5
                    )

                    Text(text = stringResource(Res.string.safety_warning_description))

                    Text(
                        text = stringResource(Res.string.safety_warning_hint),
                        color = MaterialTheme.colors.primary
                    )
                }

                Button(
                    onClick = onConfirmClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.safety_warning_confirm))
                }
            }
        }
    )
}

@Preview
@Composable
fun SafetyWarningScreenPreview() {
    AttoWalletTheme {
        SafetyWarningScreen({}, {})
    }
}