package io.application.ui.hall.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import io.application.game.GameAction
import io.application.game.GameState
import io.application.game.Relic
import io.application.game.RelicTone
import io.application.ui.hall.HallPanel
import io.application.ui.theme.Amber
import io.application.ui.theme.Ink
import io.application.ui.theme.Mist

@Composable
fun HallPanelSheet(
    panel: HallPanel,
    gameState: GameState,
    onAction: (GameAction) -> Unit,
    onDismiss: () -> Unit,
    onClearSave: () -> Unit = {},
) {
    val dragOffsetPx = remember(panel) { Animatable(0f) }
    val density = LocalDensity.current
    val dismissThreshold = with(density) { 120.dp.toPx() }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .offset { IntOffset(0, dragOffsetPx.value.roundToInt()) }
            .pointerInput(panel) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount > 0f) {
                            scope.launch {
                                dragOffsetPx.snapTo(
                                    (dragOffsetPx.value + dragAmount).coerceAtMost(900f),
                                )
                            }
                        }
                    },
                    onDragEnd = {
                        if (dragOffsetPx.value >= dismissThreshold) {
                            onDismiss()
                        } else {
                            scope.launch {
                                dragOffsetPx.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 220),
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            dragOffsetPx.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 220),
                            )
                        }
                    },
                )
            }
            .padding(10.dp),
        color = Color(0xFF211D27).copy(alpha = .98f),
        contentColor = Mist,
        shape = RoundedCornerShape(32.dp, 32.dp, 18.dp, 18.dp),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = panel.eyebrow,
                        color = Amber,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = panel.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Light,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("收起", color = Mist.copy(alpha = .65f))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            when (panel) {
                HallPanel.Echoes -> EchoContent(
                    gameState = gameState,
                    onReadEcho = { onAction(GameAction.ReadEcho(it)) },
                )
                HallPanel.Bag -> BagContent(
                    gameState = gameState,
                    onSelectRelic = { onAction(GameAction.SelectRelic(it)) },
                )
                HallPanel.Vow -> VowContent(
                    gameState = gameState,
                    onKeepVow = { onAction(GameAction.KeepVow) },
                    onClearSave = onClearSave,
                )
            }
        }
    }
}

@Composable
private fun EchoContent(
    gameState: GameState,
    onReadEcho: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        gameState.echoes.forEach { echo ->
            EchoLine(
                text = echo.text,
                meta = if (echo.isUnread) "未读 · ${echo.meta}" else "已读 · ${echo.meta}",
                onClick = { onReadEcho(echo.id) },
            )
        }
    }
}

@Composable
private fun EchoLine(
    text: String,
    meta: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = .035f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            color = Mist.copy(alpha = .82f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onClick) {
            Text(text = meta, color = Mist.copy(alpha = .4f))
        }
    }
}

@Composable
private fun BagContent(
    gameState: GameState,
    onSelectRelic: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "选择一件会被带入下一幕的遗物。",
            color = Mist.copy(alpha = .6f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            gameState.relics.forEach { relic ->
                RelicCard(
                    relic = relic,
                    isSelected = relic.id == gameState.selectedRelicId,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelectRelic(relic.id) },
                )
            }
        }
    }
}

@Composable
private fun RelicCard(
    relic: Relic,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val tint = relicToneColor(relic.tone)
    Surface(
        modifier = modifier,
        color = tint.copy(alpha = if (isSelected) .18f else .08f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            tint.copy(alpha = if (isSelected) .7f else .25f),
        ),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = relic.name, color = Mist, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = relic.state, color = tint, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun relicToneColor(tone: RelicTone): Color = when (tone) {
    RelicTone.AMBER -> Amber
    RelicTone.MIST -> Mist
    RelicTone.VIOLET -> Color(0xFFB88BD9)
}

@Composable
private fun VowContent(
    gameState: GameState,
    onKeepVow: () -> Unit,
    onClearSave: () -> Unit = {},
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column {
        Text(
            text = gameState.vow.text,
            color = Mist,
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = gameState.vow.description,
            color = Mist.copy(alpha = .55f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onKeepVow,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber.copy(alpha = .88f),
                contentColor = Ink,
            ),
            enabled = !gameState.vow.isKept,
        ) {
            Text(
                text = if (gameState.vow.isKept) "誓言已守住" else "守住这句誓言",
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        TextButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "重置游戏",
                color = Mist.copy(alpha = .32f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = Color(0xFF211D27),
            titleContentColor = Mist,
            textContentColor = Mist.copy(alpha = .65f),
            title = {
                Text("清除存档？")
            },
            text = {
                Text("所有进度、选择和余响都会消失，回到故事最初。此操作无法撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearSave()
                    },
                ) {
                    Text("清除", color = Color(0xFFE07070))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消", color = Mist.copy(alpha = .55f))
                }
            },
        )
    }
}