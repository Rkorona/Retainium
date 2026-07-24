package io.application.ui.hall

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.application.game.GameAction
import io.application.game.GameState
import io.application.ui.hall.components.BottomPrompt
import io.application.ui.hall.components.HallTopBar
import io.application.ui.hall.components.OrbitButton
import io.application.ui.hall.components.StoryGate
import io.application.ui.hall.components.Whisper
import io.application.ui.hall.panels.HallPanelSheet
import io.application.ui.hall.scene.FloatingDust
import io.application.ui.hall.scene.RoomScene
import io.application.ui.hall.scene.drawRoomAtmosphere
import io.application.ui.theme.Ink
import io.application.ui.theme.Mist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun HallScreen(
    gameState: GameState,
    onAction: (GameAction) -> Unit,
) {
    var openPanel by remember { mutableStateOf<HallPanel?>(null) }
    var isEntering by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun enterStory() {
        if (!isEntering) {
            isEntering = true
            scope.launch {
                delay(1_400)
                toastMessage = "下一幕正在翻页……"
                delay(1_100)
                isEntering = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .drawBehind { drawRoomAtmosphere(size) },
    ) {
        FloatingDust()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            HallTopBar(
                worldTitle = gameState.worldTitle,
                hallTitle = gameState.hallTitle,
                countdown = gameState.countdown,
                onMenuClick = { toastMessage = "书房的门暂时只向故事敞开" },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                RoomScene(
                    memoryPhrase = gameState.memoryPhrase,
                    memoryCaption = gameState.memoryCaption,
                    isEntering = isEntering,
                )

                Whisper(
                    echo = gameState.echoes.firstOrNull(),
                    echoCount = gameState.echoes.size,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 26.dp),
                )

                StoryGate(
                    modifier = Modifier.align(Alignment.TopEnd),
                    gate = gameState.gate,
                    isEntering = isEntering,
                    onEnter = ::enterStory,
                )

                OrbitButton(
                    label = "余响",
                    detail = "${gameState.unreadEchoCount} 条未读",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 92.dp),
                    tint = Mist,
                    onClick = { openPanel = HallPanel.Echoes },
                )

                OrbitButton(
                    label = "行囊",
                    detail = "${gameState.relics.size} 件遗物",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = 50.dp),
                    tint = io.application.ui.theme.Amber,
                    onClick = { openPanel = HallPanel.Bag },
                )

                OrbitButton(
                    label = "誓言",
                    detail = if (gameState.vow.isKept) "已守住" else "仍在燃烧",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-4).dp),
                    tint = Color(0xFFB88BD9),
                    onClick = { openPanel = HallPanel.Vow },
                )
            }

            BottomPrompt(onEnter = ::enterStory)
        }

        AnimatedVisibility(
            visible = openPanel != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(animationSpec = tween(durationMillis = 180)) +
                slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = 420,
                        easing = FastOutSlowInEasing,
                    ),
                ) { it },
            exit = fadeOut(animationSpec = tween(durationMillis = 130)) +
                slideOutVertically(
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300),
                ) { it },
        ) {
            openPanel?.let { panel ->
                HallPanelSheet(
                    panel = panel,
                    gameState = gameState,
                    onAction = onAction,
                    onDismiss = { openPanel = null },
                )
            }
        }

        AnimatedVisibility(
            visible = toastMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 82.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = Color(0xFF2B2631).copy(alpha = .94f),
                contentColor = Mist,
                shape = RoundedCornerShape(100.dp),
                tonalElevation = 6.dp,
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2_400)
            toastMessage = null
        }
    }
}