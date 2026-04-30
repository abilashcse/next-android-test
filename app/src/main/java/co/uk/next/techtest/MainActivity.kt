package co.uk.next.techtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import co.uk.next.techtest.core.theme.NextTakeHomeTestTheme
import co.uk.next.techtest.core.ui.shell.AppScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextTakeHomeTestTheme {
                AppScaffold(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
