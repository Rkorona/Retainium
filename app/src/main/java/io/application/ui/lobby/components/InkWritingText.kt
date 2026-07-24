package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

/**
 * 墨水书写动画文字 — 字符逐一浮现，带淡入效果
 *
 * @param text        要显示的完整文字
 * @param style       文字样式
 * @param delayMs     开始前的延迟（ms）
 * @param charDelayMs 每个字符之间的间隔（ms）
 */
@Composable
fun InkWritingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    delayMs: Long = 0L,
    charDelayMs: Long = 55L,
    onComplete: () -> Unit = {}
) {
    var visibleCount by remember(text) { mutableIntStateOf(0) }

    // 最后一个字符的淡入 alpha，制造「墨迹晕散」感
    val lastCharAlpha by animateFloatAsState(
        targetValue = if (visibleCount > 0) 1f else 0f,
        animationSpec = tween(durationMillis = 120, easing = EaseOut),
        label = "inkAlpha"
    )

    LaunchedEffect(text) {
        visibleCount = 0
        delay(delayMs)
        for (i in text.indices) {
            visibleCount = i + 1
            delay(charDelayMs)
        }
        onComplete()
    }

    Text(
        text = text.take(visibleCount),
        style = style,
        color = color,
        modifier = modifier
    )
}
