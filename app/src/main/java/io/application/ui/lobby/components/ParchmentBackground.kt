package io.application.ui.lobby.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import io.application.ui.theme.GoldDim
import io.application.ui.theme.InkDeep
import io.application.ui.theme.InkVoid
import io.application.ui.theme.ParchmentNight

/**
 * 多层 Canvas 绘制的羊皮纸背景
 *  Layer 0 — 纯黑基底
 *  Layer 1 — 中央暖光晕（篝火/蜡烛氛围）
 *  Layer 2 — 四角浓重暗晕（vignette）
 *  Layer 3 — 细腻网格纹（模拟纸纹）
 *  Layer 4 — 顶部标题区淡金光
 */
@Composable
fun ParchmentBackground(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF2A1E08)   // 可按区域替换
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // ── Layer 0: 基底 ──────────────────────────────────────────
        drawRect(color = InkVoid)

        // ── Layer 1: 中央暖光晕 ────────────────────────────────────
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.55f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = w * 0.85f
            )
        )

        // ── Layer 2: Vignette 四角 ─────────────────────────────────
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    InkVoid.copy(alpha = 0.80f)
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.75f
            )
        )

        // ── Layer 3: 水平扫描纹（模拟纸纤维）─────────────────────────
        val stripeColor = Color(0xFF18140A).copy(alpha = 0.25f)
        var y = 0f
        while (y < h) {
            drawLine(
                color = stripeColor,
                start = Offset(0f, y),
                end   = Offset(w, y),
                strokeWidth = 1.2f
            )
            y += 6f
        }

        // ── Layer 4: 顶部标题区淡金渐变 ───────────────────────────────
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    GoldDim.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = 0f,
                endY   = h * 0.35f
            )
        )
    }
}
