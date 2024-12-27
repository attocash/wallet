package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.back
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
            style = MaterialTheme.typography.headlineMedium
        )

        TextField(
            value = input.value,
            onValueChange = { input.value = it }
        )

        if (showError) {
            Text(
                text = stringResource(Res.string.representative_error_address),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = { onChange.invoke(input.value) },
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Text(text = stringResource(Res.string.representative_change))
        }

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.back
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