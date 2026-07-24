package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * 蜡印按钮 — 角落菜单触发器
 *
 * 绘制一枚八角星形蜡印，内刻符文。
 * 按下时弹簧缩放，长按/点击展开菜单。
 * 代替传统的 FAB 和汉堡菜单。
 */
@Composable
fun WaxSealButton(
    modifier: Modifier = Modifier,
    onMenuOpen: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 弹簧按压
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "sealScale"
    )

    // 缓慢自旋（蜡印有轻微转动感）
    val infiniteTransition = rememberInfiniteTransition(label = "sealRotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue  = -2f,
        targetValue   = 2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sealRot"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sealGlow"
    )

    Box(
        modifier        = modifier
            .scale(scale)
            .rotate(rotation)
            .size(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onMenuOpen
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = size.minDimension * 0.46f
            val innerR = outerR * 0.72f
            val points = 8

            // 外发光
            drawCircle(
                brush  = Brush.radialGradient(
                    listOf(BloodHP.copy(alpha = glowAlpha * 0.4f), Color.Transparent)
                ),
                radius = outerR * 1.5f,
                center = Offset(cx, cy)
            )

            // 八角星形路径
            val starPath = Path().apply {
                var first = true
                for (i in 0 until points * 2) {
                    val angle = (PI / points * i - PI / 2).toFloat()
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (first) { moveTo(x, y); first = false } else lineTo(x, y)
                }
                close()
            }

            // 蜡印填充（深红色）
            drawPath(
                starPath,
                brush = Brush.radialGradient(
                    listOf(Color(0xFF8B1A1A), Color(0xFF4A0808)),
                    center = Offset(cx - outerR * 0.15f, cy - outerR * 0.15f),
                    radius = outerR
                )
            )

            // 边缘描边（金色）
            drawPath(
                starPath,
                color = GoldDim.copy(alpha = 0.8f),
                style = Stroke(width = 1.5f)
            )

            // 内圆
            drawCircle(
                color  = Color(0xFF5A1010),
                radius = innerR * 0.65f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color  = GoldDim.copy(alpha = 0.5f),
                radius = innerR * 0.65f,
                center = Offset(cx, cy),
                style  = Stroke(width = 1f)
            )
        }

        // 中心符文
        Text(
            text     = "᛭",
            fontSize = 16.sp,
            color    = GoldAncient,
            modifier = Modifier.rotate(-rotation) // 抵消外层旋转，让符文保持正向
        )
    }
}
