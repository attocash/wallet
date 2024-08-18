package cash.atto.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.compose_multiplatform
import cash.atto.commons.AttoMnemonic
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val mnemonicManager = mnemonicManager()


    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var inputText by remember { mutableStateOf("") }
        val dictionary = AttoMnemonic.dictionary.toSortedSet()

        val words = inputText.trim().lowercase().split("\\s+".toRegex())
        val wordsCount = words.size
        val allWordsValid = words.all { it in dictionary }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Button to generate 24 words
            Button(onClick = {
                inputText = AttoMnemonic.generate().words.joinToString(" ")
            }) {
                Text("Generate 24 Words")
            }

            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Words entered: $wordsCount/24")
            if (!allWordsValid) {
                Text("One or more words are not valid", color = MaterialTheme.colors.error)
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        mnemonicManager.save(AttoMnemonic(words))
                        showContent = true  // Show the content after saving
                    }
                },
                enabled = wordsCount == 24 && allWordsValid
            ) {
                Text("Submit")
            }

            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}