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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import io.application.game.GameAction
import io.application.game.GameRepository
import io.application.game.GameState
import io.application.game.reduce
import io.application.game.restoreFrom
import io.application.ui.hall.HallScreen
import io.application.ui.story.StoryScreen
import io.application.ui.theme.ApplicationTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val repository = remember { GameRepository(context) }
    val scope = rememberCoroutineScope()

    var gameState by remember { mutableStateOf(GameState.initial()) }
    var isLoaded by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Hall) }

    // ── 启动时从 DataStore 读取存档（取第一个值即退出，不持续监听）─────────
    LaunchedEffect(Unit) {
        val saved = repository.savedState.first()
        if (saved != null) {
            gameState = GameState.initial().restoreFrom(saved)
        }
        isLoaded = true
    }

    // ── 每次状态变化后自动保存（仅在初始加载完成后触发）────────────────────
    LaunchedEffect(gameState, isLoaded) {
        if (isLoaded) {
            repository.save(gameState)
        }
    }

    fun dispatch(action: GameAction) {
        gameState = gameState.reduce(action)
    }

    // ── 清除存档：清空 DataStore，重置状态，回到大厅 ─────────────────────────
    fun clearSave() {
        scope.launch {
            repository.clear()
            gameState = GameState.initial()
            currentScreen = AppScreen.Hall
        }
    }

    // 存档尚未加载完毕时不渲染任何内容，避免初始状态闪现
    if (!isLoaded) return

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
                    onAction = ::dispatch,
                    onEnterStory = { currentScreen = AppScreen.Story },
                    onClearSave = ::clearSave,
                )
                is AppScreen.Story -> StoryScreen(
                    gameState = gameState,
                    onAction = ::dispatch,
                    onExitStory = { currentScreen = AppScreen.Hall },
                )
            }
        }
    }
}
