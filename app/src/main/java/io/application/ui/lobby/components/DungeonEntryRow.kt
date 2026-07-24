package io.application.ui.lobby.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.model.DungeonEntry
import io.application.model.SceneType
import io.application.ui.theme.*
import kotlin.math.sin

/**
 * 副本书页条目 — 古籍词条风格，完全替代卡片
 *
 * 折叠态：一行精简词条
 *   · 「暮色密林」 古树低语未歇  ✦✦✧✧✧  Lv.20+   ▶
 *
 * 展开态（点击后，弹簧展开）：
 *   左侧：Canvas 微型场景插图
 *   右侧：副本详情 + 进入按钮
 */
@Composable
fun DungeonEntryRow(
    dungeon: DungeonEntry,
    modifier: Modifier = Modifier,
    onEnter: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // 展开高度弹簧动画
    val expandFraction by animateFloatAsState(
        targetValue   = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "expandFraction"
    )

    // 场景插图时间轴（仅展开时激活）
    val infiniteTransition = rememberInfiniteTransition(label = "scene_${dungeon.id}")
    val sceneTick by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(10_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick"
    )

    val isLocked = !dungeon.isUnlocked
    val primaryColor = if (isLocked) ScrollTextFaint else dungeon.zone.glowColor

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = !isLocked
            ) {
                expanded = !expanded
            }
    ) {
        // ── 折叠态词条行 ─────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 项目符号
            Text(
                text  = if (isLocked) "✦" else "·",
                color = if (isLocked) ScrollTextFaint else GoldDim,
                fontSize = 14.sp,
                modifier = Modifier.width(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            // 副本名（书名号强调）
            Text(
                text       = "「${dungeon.name}」",
                style      = MaterialTheme.typography.titleSmall,
                color      = if (isLocked) ScrollTextFaint else ScrollText,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            // 简介（等宽斜体，像书中的简注）
            Text(
                text      = dungeon.subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = ScrollTextDim.copy(alpha = if (isLocked) 0.4f else 0.8f),
                fontStyle = FontStyle.Italic,
                modifier  = Modifier.weight(1f),
                maxLines  = 1
            )

            Spacer(Modifier.width(8.dp))

            // 难度星号（✦✧）
            Row {
                repeat(5) { i ->
                    Text(
                        text     = if (i < dungeon.difficulty) "✦" else "✧",
                        fontSize = 9.sp,
                        color    = if (i < dungeon.difficulty) primaryColor else ScrollTextFaint
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // 展开箭头
            if (!isLocked) {
                val arrowRotation by animateFloatAsState(
                    targetValue   = if (expanded) 90f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label         = "arrow"
                )
                Text(
                    text  = "▶",
                    fontSize = 9.sp,
                    color = GoldDim,
                    modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
                )
            } else {
                Text("🔒", fontSize = 11.sp)
            }
        }

        // ── 展开态详情 ────────────────────────────────────────────
        if (expandFraction > 0f) {
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = expandFraction }
                    .padding(start = 22.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0E0B18).copy(alpha = 0.8f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧微型场景插图
                    Box(
                        modifier = Modifier
                            .size(width = 88.dp, height = 72.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        DungeonMiniScene(
                            zone = dungeon.zone,
                            tick = sceneTick,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    // 右侧详情
                    Column(modifier = Modifier.weight(1f)) {
                        // 区域标签
                        Box(
                            modifier = Modifier
                                .background(
                                    dungeon.zone.seedColor.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text  = dungeon.zone.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // 元数据
                        Text(
                            text  = "推荐境界  Lv.${dungeon.recommendedLevel}+  ·  约 ${dungeon.estimatedMinutes} 分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = ScrollTextDim
                        )

                        Spacer(Modifier.height(8.dp))

                        // 进入按钮（古籍印章风格）
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GoldDim.copy(alpha = 0.4f), GoldAncient.copy(alpha = 0.5f))
                                    )
                                )
                                .clickable(onClick = onEnter)
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = "踏入此地  ▶",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 词条下分隔线（淡）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, GoldDim.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
    }
}

// ── 微型场景 Canvas ──────────────────────────────────────────────────
@Composable
private fun DungeonMiniScene(
    zone: io.application.model.DungeonZone,
    tick: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height

        // 背景色
        val bg = when (zone.sceneType) {
            SceneType.FOREST  -> Color(0xFF060E06)
            SceneType.ABYSS   -> Color(0xFF06020F)
            SceneType.TEMPLE  -> Color(0xFF080818)
            SceneType.RUINS   -> Color(0xFF0F0605)
            SceneType.GLACIER -> Color(0xFF040C14)
        }
        drawRect(bg)

        when (zone.sceneType) {
            SceneType.FOREST -> {
                drawCircle(Color(0xFFDDD8B0).copy(alpha = 0.7f), radius = w * 0.08f, center = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.22f))
                val p = Path().apply {
                    moveTo(0f, h); lineTo(w * 0.18f, h * 0.35f); lineTo(w * 0.36f, h)
                    moveTo(w * 0.3f, h); lineTo(w * 0.5f, h * 0.18f); lineTo(w * 0.7f, h)
                    moveTo(w * 0.65f, h); lineTo(w * 0.80f, h * 0.4f); lineTo(w * 0.95f, h)
                }
                drawPath(p, Color(0xFF040B04))
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF1A3A15).copy(alpha = 0.4f)), startY = h * 0.7f, endY = h)
                )
            }
            SceneType.ABYSS -> {
                repeat(3) { i ->
                    val bx = w * (0.2f + i * 0.3f)
                    val sway = sin((tick * 2 * Math.PI + i).toDouble()).toFloat() * 8f
                    drawLine(
                        color = Color(0xFF7B4FC8).copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(bx, h),
                        end   = androidx.compose.ui.geometry.Offset(bx + sway, h * 0.2f),
                        strokeWidth = 5f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                val ra = ((sin(tick * 2 * Math.PI * 2) + 1) / 2).toFloat() * 0.2f
                drawCircle(Brush.radialGradient(listOf(Color(0xFF7B4FC8).copy(alpha = ra + 0.1f), Color.Transparent), radius = w * 0.5f), w * 0.5f, androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.6f))
            }
            SceneType.TEMPLE -> {
                drawCircle(Color(0xFFD8D0F0).copy(alpha = 0.85f), radius = w * 0.09f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.22f))
                val tp = Path().apply {
                    moveTo(w * 0.1f, h); lineTo(w * 0.1f, h * 0.5f); lineTo(w * 0.2f, h * 0.3f); lineTo(w * 0.3f, h * 0.5f); lineTo(w * 0.3f, h)
                    moveTo(w * 0.38f, h); lineTo(w * 0.38f, h * 0.45f); lineTo(w * 0.5f, h * 0.12f); lineTo(w * 0.62f, h * 0.45f); lineTo(w * 0.62f, h)
                    moveTo(w * 0.70f, h); lineTo(w * 0.70f, h * 0.5f); lineTo(w * 0.80f, h * 0.3f); lineTo(w * 0.90f, h * 0.5f); lineTo(w * 0.90f, h)
                }
                drawPath(tp, Color(0xFF0C0C20))
            }
            SceneType.RUINS -> {
                val rp = Path().apply {
                    moveTo(w * 0.05f, h); lineTo(w * 0.05f, h * 0.3f); lineTo(w * 0.2f, h * 0.3f); lineTo(w * 0.2f, h)
                    moveTo(w * 0.05f, h * 0.3f); cubicTo(w * 0.05f, h * 0.05f, w * 0.35f, h * 0.05f, w * 0.35f, h * 0.3f)
                    moveTo(w * 0.5f, h); lineTo(w * 0.5f, h * 0.45f); lineTo(w * 0.62f, h * 0.42f); lineTo(w * 0.65f, h)
                }
                drawPath(rp, Color(0xFF1A0C08))
                repeat(5) { i ->
                    val ea = ((sin((tick * 3 + i * 1.5).toDouble()) + 1) / 2).toFloat() * 0.6f + 0.1f
                    drawCircle(Color(0xFFFF6020).copy(alpha = ea), radius = 2f, center = androidx.compose.ui.geometry.Offset(w * (0.1f + i * 0.17f), h * 0.85f))
                }
            }
            SceneType.GLACIER -> {
                val gp = Path().apply {
                    moveTo(0f, h); lineTo(w * 0.18f, h * 0.35f); lineTo(w * 0.32f, h * 0.55f)
                    lineTo(w * 0.50f, h * 0.20f); lineTo(w * 0.68f, h * 0.45f); lineTo(w * 0.82f, h * 0.30f)
                    lineTo(w, h * 0.55f); lineTo(w, h); close()
                }
                drawPath(gp, Color(0xFF0A1828))
                drawPath(gp, brush = Brush.verticalGradient(listOf(Color(0xFFBBDDEE).copy(alpha = 0.25f), Color.Transparent), startY = 0f, endY = h * 0.6f))
            }
        }

        // 整体渐变叠加，边缘压暗
        drawRect(brush = Brush.radialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)), radius = size.maxDimension * 0.7f))
    }
}
