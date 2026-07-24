package io.application.ui.hall.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.game.Echo
import io.application.game.StoryGateState
import io.application.ui.theme.Amber
import io.application.ui.theme.Mist

@Composable
fun HallTopBar(
    worldTitle: String,
    hallTitle: String,
    countdown: String,
    onMenuClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = worldTitle,
                color = Mist.copy(alpha = .62f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.4.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hallTitle,
                color = Mist,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-.4).sp,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = Color.White.copy(alpha = .06f),
                shape = RoundedCornerShape(100.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .1f)),
            ) {
                Text(
                    text = countdown,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Amber.copy(alpha = .92f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            TextButton(onClick = onMenuClick) {
                Text(
                    text = "≡",
                    color = Mist.copy(alpha = .8f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
fun Whisper(
    echo: Echo?,
    echoCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(154.dp)
            .alpha(.78f),
    ) {
        Text(
            text = "余响 / ${echoCount.toString().padStart(2, '0')}",
            color = Amber.copy(alpha = .82f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.2.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "“${echo?.text ?: "书房正在等待你的声音。"}”",
            color = Mist.copy(alpha = .7f),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            lineHeight = 22.sp,
        )
    }
}

@Composable
fun StoryGate(
    modifier: Modifier = Modifier,
    gate: StoryGateState,
    isEntering: Boolean,
    onEnter: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "gate")
    val drift by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            tween(2_300, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "gate-drift",
    )

    Column(
        modifier = modifier
            .offset(y = drift.dp)
            .width(166.dp)
            .clip(RoundedCornerShape(26.dp, 8.dp, 26.dp, 8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4C3349).copy(alpha = .82f),
                        Color(0xFF161923).copy(alpha = .94f),
                    ),
                ),
            )
            .border(
                1.dp,
                Amber.copy(alpha = .3f),
                RoundedCornerShape(26.dp, 8.dp, 26.dp, 8.dp),
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onEnter() },
                    onLongPress = { onEnter() },
                )
            }
            .padding(16.dp),
    ) {
        Text(
            text = if (isEntering) "门正在回应" else gate.title,
            color = Mist,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isEntering) "不要回头" else gate.subtitle,
            color = Amber.copy(alpha = .82f),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = gate.description,
            color = Mist.copy(alpha = .55f),
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
        )
    }
}

@Composable
fun OrbitButton(
    label: String,
    detail: String,
    modifier: Modifier,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(92.dp)
            .clip(RoundedCornerShape(24.dp, 10.dp, 24.dp, 10.dp))
            .background(Color.White.copy(alpha = .045f))
            .border(
                1.dp,
                tint.copy(alpha = .25f),
                RoundedCornerShape(24.dp, 10.dp, 24.dp, 10.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = .13f))
                .border(1.dp, tint.copy(alpha = .65f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "·",
                color = tint,
                fontSize = 24.sp,
                lineHeight = 20.sp,
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            color = Mist,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = detail,
            color = Mist.copy(alpha = .47f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@Composable
fun BottomPrompt(onEnter: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "准备好了吗？",
                color = Mist.copy(alpha = .46f),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "触碰无名门，继续前行",
                color = Mist.copy(alpha = .78f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OutlinedButton(
            onClick = onEnter,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Mist),
            border = androidx.compose.foundation.BorderStroke(1.dp, Mist.copy(alpha = .22f)),
        ) {
            Text(text = "进入下一幕")
        }
    }
}