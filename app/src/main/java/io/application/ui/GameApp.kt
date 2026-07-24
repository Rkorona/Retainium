package io.application.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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

// Material 3 Expressive 标准曲线
// 强调减速：用于进入屏幕的元素——快速启动，平滑落地
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
// 强调加速：用于离开屏幕的元素——缓起迅速离场
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

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
                if (targetState is AppScreen.Story) {
                    // ── 进入副本 ──────────────────────────────────────────────
                    // 副本页从屏幕底部滑入（方向暗示：深入另一层空间）
                    // 大厅同时轻微缩退 + 淡出，营造层次纵深感
                    (slideInVertically(
                        animationSpec = tween(durationMillis = 520, easing = EmphasizedDecelerate),
                        initialOffsetY = { fullHeight -> fullHeight },
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 160),
                    )) togetherWith (
                        scaleOut(
                            animationSpec = tween(durationMillis = 340, easing = EmphasizedAccelerate),
                            targetScale = 0.94f,
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 260, easing = EmphasizedAccelerate),
                        )
                    )
                } else {
                    // ── 返回大厅 ──────────────────────────────────────────────
                    // 大厅从微缩状态弹回（对应进入时的缩退）
                    // 副本页向下滑出，完成对称的空间退出
                    (scaleIn(
                        animationSpec = tween(durationMillis = 520, easing = EmphasizedDecelerate),
                        initialScale = 0.94f,
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 350, easing = EmphasizedDecelerate),
                    )) togetherWith (
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 360, easing = EmphasizedAccelerate),
                            targetOffsetY = { fullHeight -> fullHeight },
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 220),
                        )
                    )
                }
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
