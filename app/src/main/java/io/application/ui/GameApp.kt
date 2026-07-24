package io.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.application.game.GameAction
import io.application.game.GameState
import io.application.game.reduce
import io.application.ui.hall.HallScreen
import io.application.ui.story.StoryScreen
import io.application.ui.theme.ApplicationTheme

private sealed class AppScreen {
    object Hall : AppScreen()
    object Story : AppScreen()
}

@Composable
fun GameApp() {
    var gameState by remember { mutableStateOf(GameState.initial()) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Hall) }

    ApplicationTheme {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(400))
            },
            label = "app-screen",
        ) { screen ->
            when (screen) {
                is AppScreen.Hall -> HallScreen(
                    gameState = gameState,
                    onAction = { gameState = gameState.reduce(it) },
                    onEnterStory = { currentScreen = AppScreen.Story },
                )
                is AppScreen.Story -> StoryScreen(
                    gameState = gameState,
                    onAction = { gameState = gameState.reduce(it) },
                    onExitStory = { currentScreen = AppScreen.Hall },
                )
            }
        }
    }
}
