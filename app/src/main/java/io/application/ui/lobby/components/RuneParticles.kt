package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import io.application.ui.theme.GoldAncient
import io.application.ui.theme.RuneBlue
import io.application.ui.theme.RuneViolet
import kotlin.math.sin

private val RUNES = listOf("ᚠ","ᚢ","ᚦ","ᚨ","ᚱ","ᚲ","ᚷ","ᚹ","ᚺ","ᚾ","ᛁ","ᛃ","ᛇ","ᛈ","ᛉ","ᛊ","ᛏ","ᛒ","ᛖ","ᛗ","ᛚ","ᛜ","ᛞ","ᛟ")
private val RUNE_COLORS = listOf(
    GoldAncient.copy(alpha = 0.30f),
    RuneBlue.copy(alpha = 0.20f),
    RuneViolet.copy(alpha = 0.18f),
    Color.White.copy(alpha = 0.10f)
)

private data class RuneParticle(
    val rune: String,
    val startX: Float,   // 相对于宽度的比例 0..1
    val startY: Float,   // 相对于高度的比例 0..1
    val speed: Float,    // 上升速度因子
    val size: Float,     // 字号 sp
    val color: Color,
    val phaseSin: Float, // 水平漂移相位
    val rotationSpeed: Float
)

/**
 * 环境符文粒子 — 在屏幕上缓慢漂浮上升的古文字
 */
@Composable
fun RuneParticles(
    modifier: Modifier = Modifier,
    count: Int = 18
) {
    val particles = remember {
        val rng = java.util.Random(42L) // 固定种子，保证稳定分布
        List(count) {
            RuneParticle(
                rune         = RUNES[rng.nextInt(RUNES.size)],
                startX       = rng.nextFloat(),
                startY       = rng.nextFloat(),
                speed        = 0.012f + rng.nextFloat() * 0.018f,
                size         = 10f + rng.nextFloat() * 14f,
                color        = RUNE_COLORS[rng.nextInt(RUNE_COLORS.size)],
                phaseSin     = rng.nextFloat() * (2f * Math.PI).toFloat(),
                rotationSpeed= (rng.nextFloat() - 0.5f) * 12f
            )
        }
    }

    // 全局时间驱动，单一 infiniteTransition 驱动所有粒子
    val infiniteTransition = rememberInfiniteTransition(label = "runes")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 16_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "runeTick"
    )

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            // 每个粒子以自己的 speed 为周期在 Y 轴循环
            val cycleY = ((tick * p.speed * 120f) + p.startY) % 1.2f  // 超出顶部后重置
            val rawY   = size.height * (1.0f - cycleY)

            // 水平正弦漂移
            val sinOffset = sin((tick * 6.28f * p.speed * 60f + p.phaseSin).toDouble()).toFloat()
            val rawX = size.width * p.startX + sinOffset * 18f

            // 淡入淡出（接近顶部时消隐）
            val fadeAlpha = when {
                cycleY > 1.1f -> 0f
                cycleY > 0.9f -> (1.2f - cycleY) / 0.2f
                cycleY < 0.05f -> cycleY / 0.05f
                else          -> 1f
            }

            val alpha = fadeAlpha.coerceIn(0f, 1f)
            if (alpha < 0.01f) return@forEach

            // 旋转绘制符文
            rotate(
                degrees = tick * p.rotationSpeed * 360f,
                pivot   = Offset(rawX, rawY)
            ) {
                val measured = textMeasurer.measure(
                    AnnotatedString(p.rune),
                    style = TextStyle(
                        color    = p.color.copy(alpha = p.color.alpha * alpha),
                        fontSize = p.size.sp
                    )
                )
                drawText(measured, topLeft = Offset(rawX - measured.size.width / 2f, rawY - measured.size.height / 2f))
            }
        }
    }
}
