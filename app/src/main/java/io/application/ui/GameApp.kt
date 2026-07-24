package io.application.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.application.game.GameAction
import io.application.game.GameState
import io.application.game.reduce
import io.application.ui.hall.HallScreen
import io.application.ui.theme.ApplicationTheme

@Composable
fun GameApp() {
    var gameState by remember { mutableStateOf(GameState.initial()) }

    ApplicationTheme {
        HallScreen(
            gameState = gameState,
            onAction = { action ->
                gameState = gameState.reduce(action)
            },
        )
    }
}