package lab.jhrodriguezi.tictactoe.ui.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun AdaptiveLayout(
    content: @Composable (isLandscape: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    var isLandscape by remember { mutableStateOf(false) }

    DisposableEffect(configuration) {
        isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        onDispose {}
    }

    content(isLandscape)
}