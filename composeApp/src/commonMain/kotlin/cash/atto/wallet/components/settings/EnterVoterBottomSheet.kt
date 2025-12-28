package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.staking_change
import attowallet.composeapp.generated.resources.staking_change_title
import attowallet.composeapp.generated.resources.staking_close
import attowallet.composeapp.generated.resources.staking_error_address
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterVoterBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onChange: (String) -> Unit,
    onClose: () -> Unit,
    showError: Boolean
) {
    val input = remember {
        mutableStateOf("")
    }

    BottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Text(
            text = stringResource(Res.string.staking_change_title),
            style = MaterialTheme.typography.headlineMedium
        )

        TextField(
            value = input.value,
            onValueChange = { input.value = it }
        )

        if (showError) {
            Text(
                text = stringResource(Res.string.staking_error_address),
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
            Text(text = stringResource(Res.string.staking_change))
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Text(text = stringResource(Res.string.staking_close))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EnterRepresentativeBottomSheetPreview() {
    AttoWalletTheme {
        EnterVoterBottomSheet(
            onDismissRequest = {},
            onChange = {},
            onClose = {},
            showError = true
        )
    }
}