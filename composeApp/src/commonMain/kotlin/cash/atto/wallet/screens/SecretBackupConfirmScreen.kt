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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.backup_confirm_no
import attowallet.composeapp.generated.resources.backup_confirm_subtitle
import attowallet.composeapp.generated.resources.backup_confirm_title
import attowallet.composeapp.generated.resources.backup_confirm_yes
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SecretBackupConfirmScreen(
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
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    )
                    .padding(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.backup_confirm_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W300,
                        fontFamily = attoFontFamily(),
                        lineHeight = 40.sp
                    )

                    Text(
                        text = stringResource(Res.string.backup_confirm_subtitle),
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.body2
                    )
                }

                AttoButton(
                    onClick = onConfirmClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.backup_confirm_yes))
                }

                AttoOutlinedButton(
                    onClick = onBackNavigation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.backup_confirm_no))
                }
            }
        }
    )
}

@Preview
@Composable
fun SecretBackupConfirmScreenPreview() {
    AttoWalletTheme {
        SecretBackupConfirmScreen({}, {})
    }
}