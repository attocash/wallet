package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.representative_change
import attowallet.composeapp.generated.resources.representative_change_title
import attowallet.composeapp.generated.resources.representative_close
import attowallet.composeapp.generated.resources.representative_error_address
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EnterRepresentativeBottomSheet(
    onChange: (String) -> Unit,
    onClose: () -> Unit,
    showError: Boolean
) {
    val input = remember {
        mutableStateOf("")
    }

    BottomSheet {
        Text(
            text = stringResource(Res.string.representative_change_title),
            style = MaterialTheme.typography.h5
        )

        TextField(
            value = input.value,
            onValueChange = { input.value = it }
        )

        if (showError) {
            Text(
                text = stringResource(Res.string.representative_error_address),
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = { onChange.invoke(input.value) },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondaryVariant,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Text(text = stringResource(Res.string.representative_change))
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.onSecondary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Text(text = stringResource(Res.string.representative_close))
        }
    }
}

@Preview
@Composable
fun EnterRepresentativeBottomSheetPreview() {
    AttoWalletTheme {
        EnterRepresentativeBottomSheet(
            onChange = {},
            onClose = {},
            showError = true
        )
    }
}