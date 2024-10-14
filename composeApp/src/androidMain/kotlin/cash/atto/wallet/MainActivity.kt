package cash.atto.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
//            statusBarStyle = SystemBarStyle.dark(0xFFFFFFFF.toInt())
        )
        super.onCreate(savedInstanceState)

        setContent {
            AttoAppAndroid()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AttoAppAndroid()
}