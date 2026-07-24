package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 烛火插图 — 火焰高度对应 HP 比例
 * 完全替代进度条，是书页边注风格的手绘插画
 */
@Composable
fun FlameStatIcon(
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    val flicker by infiniteTransition.animateFloat(
        initialValue  = -1f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(380, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker"
    )
    val flicker2 by infiniteTransition.animateFloat(
        initialValue  = 0.8f,
        targetValue   = 1.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(260, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flicker2"
    )

    Canvas(modifier = modifier.size(width = 24.dp, height = 36.dp)) {
        val w = size.width
        val h = size.height
        val clamped = fraction.coerceIn(0f, 1f)

        // 蜡烛柱
        val candleH = h * 0.35f
        val candleW = w * 0.28f
        val candleLeft  = w * 0.5f - candleW / 2f
        val candleTop   = h - candleH
        drawRect(
            color   = Color(0xFFD4C090),
            topLeft = Offset(candleLeft, candleTop),
            size    = androidx.compose.ui.geometry.Size(candleW, candleH)
        )
        // 蜡烛顶部高光
        drawRect(
            color   = Color(0xFFEEDEB0),
            topLeft = Offset(candleLeft, candleTop),
            size    = androidx.compose.ui.geometry.Size(candleW * 0.3f, candleH)
        )

        // 灯芯
        val wickX = w * 0.5f
        val wickTop = candleTop - h * 0.05f
        drawLine(
            color       = Color(0xFF2A1A08),
            start       = Offset(wickX, candleTop),
            end         = Offset(wickX + flicker * 2f, wickTop),
            strokeWidth = 1.5f
        )

        if (clamped > 0.02f) {
            // 火焰：高度根据 HP 比例，闪烁叠加
            val maxFlameH = h * 0.55f
            val baseFlameH = maxFlameH * clamped * flicker2
            val flameBase = wickTop
            val flameTip  = flameBase - baseFlameH

            // 外层火焰（橙色）
            val outerFlame = Path().apply {
                moveTo(wickX - w * 0.12f, flameBase)
                cubicTo(
                    wickX - w * 0.22f + flicker * 3f, flameBase - baseFlameH * 0.5f,
                    wickX - w * 0.10f + flicker * 2f, flameTip + baseFlameH * 0.15f,
                    wickX + flicker * 1.5f, flameTip
                )
                cubicTo(
                    wickX + w * 0.10f - flicker * 2f, flameTip + baseFlameH * 0.15f,
                    wickX + w * 0.22f - flicker * 3f, flameBase - baseFlameH * 0.5f,
                    wickX + w * 0.12f, flameBase
                )
                close()
            }
            drawPath(
                outerFlame,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F0C8), Color(0xFFFF8C00), Color(0xFFCC2200)),
                    startY = flameTip,
                    endY   = flameBase
                )
            )

            // 内层火焰（白黄心）
            val innerH = baseFlameH * 0.45f
            val innerFlame = Path().apply {
                moveTo(wickX - w * 0.06f, flameBase)
                cubicTo(
                    wickX - w * 0.10f, flameBase - innerH * 0.6f,
                    wickX - w * 0.04f, flameBase - innerH,
                    wickX, flameBase - innerH
                )
                cubicTo(
                    wickX + w * 0.04f, flameBase - innerH,
                    wickX + w * 0.10f, flameBase - innerH * 0.6f,
                    wickX + w * 0.06f, flameBase
                )
                close()
            }
            drawPath(
                innerFlame,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFE0), Color(0xFFFFCC44).copy(alpha = 0.6f)),
                    startY = flameBase - innerH,
                    endY   = flameBase
                )
            )

            // 光晕
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF8C00).copy(alpha = 0.25f * clamped), Color.Transparent),
                    radius = w * 0.7f
                ),
                radius = w * 0.7f,
                center = Offset(wickX, flameBase - baseFlameH * 0.4f)
            )
        }
    }
}

/**
 * 月相插图 — 月亮盈亏对应 MP 比例
 */
@Composable
fun MoonStatIcon(
    fraction: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moon")
    val glow by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moonGlow"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        val clamped = fraction.coerceIn(0f, 1f)
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r  = size.minDimension * 0.42f

        // 外发光
        drawCircle(
            brush  = Brush.radialGradient(
                listOf(Color(0xFF9090D8).copy(alpha = glow * clamped), Color.Transparent)
            ),
            radius = r * 1.6f,
            center = Offset(cx, cy)
        )

        // 月亮底层（满月轮廓）
        drawCircle(
            color  = Color(0xFF2A2840),
            radius = r,
            center = Offset(cx, cy)
        )

        // 月亮填充（阴影覆盖遮罩，fraction 决定盈亏）
        // 0=新月(全暗) 1=满月(全亮)
        val litColor = Color(0xFFCCCCEE)
        if (clamped > 0.01f) {
            // 用弧形路径模拟月相
            val moonPath = Path().apply {
                val segments = 60
                // 右半弧（固定亮）
                for (i in 0..segments) {
                    val angle = (-PI / 2 + PI * i / segments).toFloat()
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                // 左半弧：根据 fraction 在 内凹(新月)→直(半月)→外凸(满月) 之间变化
                val bulge = (clamped * 2f - 1f) * r  // -r..+r
                for (i in segments downTo 0) {
                    val angle = (-PI / 2 + PI * i / segments).toFloat()
                    val xEdge = cx + r * cos(angle)
                    val yEdge = cy + r * sin(angle)
                    // 用 cos(angle) 的符号决定左边的 bulge 方向
                    val xInner = cx - bulge * cos(0f) + (xEdge - cx) * 0f  // 简化：纵向椭圆
                    lineTo(cx - bulge, yEdge)
                }
                close()
            }
            // 更简单的方式：绘制满月，然后覆盖暗色
            drawCircle(litColor, radius = r, center = Offset(cx, cy))
            // 暗覆盖（根据 fraction 决定半径偏移）
            if (clamped < 0.99f) {
                val darkX = cx - r + (r * 2 * clamped)
                val darkR  = r * 1.01f
                drawCircle(
                    color  = Color(0xFF1A1828),
                    radius = darkR,
                    center = Offset(darkX, cy)
                )
            }
        }

        // 边缘
        drawCircle(
            color  = Color(0xFF6060A0).copy(alpha = 0.5f),
            radius = r,
            center = Offset(cx, cy),
            style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )
    }
}
