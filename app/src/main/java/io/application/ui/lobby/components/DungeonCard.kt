package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.model.DungeonEntry
import io.application.model.DungeonZone
import io.application.model.SceneType
import io.application.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * 副本卡片 — 活手稿插图风格
 *
 * 上半部分：Canvas 绘制的场景剪影（每个区域不同）
 * 下半部分：副本名称、副本描述、难度符文、推荐等级
 *
 * M3 Expressive 弹簧按压缩放 + 光晕呼吸动画
 */
@Composable
fun DungeonCard(
    dungeon: DungeonEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // M3 Expressive 弹簧缩放
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    // 场景动画时间轴
    val infiniteTransition = rememberInfiniteTransition(label = "scene_${dungeon.id}")
    val sceneTick by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sceneTick"
    )
    // 光晕脉冲
    val glowPulse by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2000 + dungeon.id.hashCode().and(0x7FF), easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF12101C))
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = dungeon.isUnlocked,
                onClick           = onClick
            )
    ) {
        Column {

            // ── 上半：场景插图 ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // 场景 Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSceneBackground(dungeon.zone, sceneTick)
                    when (dungeon.zone.sceneType) {
                        SceneType.FOREST  -> drawForestScene(sceneTick)
                        SceneType.ABYSS   -> drawAbyssScene(sceneTick)
                        SceneType.TEMPLE  -> drawTempleScene(sceneTick)
                        SceneType.RUINS   -> drawRuinsScene(sceneTick)
                        SceneType.GLACIER -> drawGlacierScene(sceneTick)
                    }
                    // 底部向卡片内容渐变
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF12101C)),
                            startY = size.height * 0.5f,
                            endY   = size.height
                        )
                    )
                }

                // 区域名标签（右上角）
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            dungeon.zone.seedColor.copy(alpha = 0.75f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text  = dungeon.zone.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // 锁定遮罩
                if (!dungeon.isUnlocked) {
                    Box(
                        modifier        = Modifier
                            .fillMaxSize()
                            .background(Color(0xAA000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 28.sp)
                    }
                }

                // 光晕（仅解锁副本）
                if (dungeon.isUnlocked) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush  = Brush.radialGradient(
                                listOf(
                                    dungeon.zone.glowColor.copy(alpha = glowPulse * 0.25f),
                                    Color.Transparent
                                ),
                                radius = size.minDimension * 0.6f
                            ),
                            radius = size.minDimension * 0.6f,
                            center = Offset(size.width * 0.5f, size.height * 0.4f)
                        )
                    }
                }
            }

            // ── 下半：文字信息 ────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text  = dungeon.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (dungeon.isUnlocked) ScrollText else ScrollTextDim,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text      = dungeon.subtitle,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = ScrollTextDim,
                    fontStyle = FontStyle.Italic,
                    maxLines  = 2
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // 难度符文点
                    DifficultRunes(
                        difficulty = dungeon.difficulty,
                        glowColor  = dungeon.zone.glowColor
                    )
                    // 推荐等级
                    Text(
                        text  = "Lv.${dungeon.recommendedLevel}+",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldDim,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))

                // 时间估算
                Text(
                    text  = "约 ${dungeon.estimatedMinutes} 分钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = ScrollTextFaint
                )
            }
        }

        // 卡片整体金边（解锁时）
        if (dungeon.isUnlocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color       = GoldDim.copy(alpha = 0.45f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                        style       = Stroke(width = 1f)
                    )
                }
            }
        }
    }
}

// ── 难度符文指示器 ──────────────────────────────────────────────────
@Composable
private fun DifficultRunes(difficulty: Int, glowColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { index ->
            val isActive = index < difficulty
            Text(
                text  = if (isActive) "◈" else "◇",
                fontSize = 10.sp,
                color = if (isActive) glowColor else ScrollTextFaint
            )
        }
    }
}

// ── Canvas 场景绘制函数 ────────────────────────────────────────────

private fun DrawScope.drawSceneBackground(zone: DungeonZone, tick: Float) {
    // 每个区域专属天空渐变
    val (top, bottom) = when (zone) {
        DungeonZone.DARK_FOREST -> Pair(Color(0xFF060E06), Color(0xFF0C1A10))
        DungeonZone.ABYSS       -> Pair(Color(0xFF06020F), Color(0xFF120818))
        DungeonZone.TEMPLE      -> Pair(Color(0xFF080818), Color(0xFF141030))
        DungeonZone.RUINS       -> Pair(Color(0xFF0F0605), Color(0xFF1A0C08))
        DungeonZone.GLACIER     -> Pair(Color(0xFF040C14), Color(0xFF0A1828))
    }
    drawRect(
        brush = Brush.verticalGradient(listOf(top, bottom))
    )
}

private fun DrawScope.drawForestScene(tick: Float) {
    val w = size.width; val h = size.height
    // 星星
    val starSeed = listOf(0.15f to 0.12f, 0.45f to 0.08f, 0.72f to 0.18f, 0.88f to 0.06f, 0.32f to 0.22f)
    starSeed.forEach { (x, y) ->
        drawCircle(Color(0xFFDDCCA0).copy(alpha = 0.7f), radius = 1.2f, center = Offset(w * x, h * y))
    }
    // 月亮
    drawCircle(Color(0xFFE8DEBC), radius = w * 0.07f, center = Offset(w * 0.78f, h * 0.22f))
    drawCircle(Color(0xFF0C1A10), radius = w * 0.06f, center = Offset(w * 0.81f, h * 0.20f))

    // 树冠轮廓路径
    val treePath = Path().apply {
        // 左小树
        moveTo(w * 0.05f, h); lineTo(w * 0.15f, h * 0.55f); lineTo(w * 0.10f, h * 0.55f)
        lineTo(w * 0.18f, h * 0.35f); lineTo(w * 0.26f, h); close()
        // 中高树
        moveTo(w * 0.28f, h); lineTo(w * 0.38f, h * 0.38f); lineTo(w * 0.32f, h * 0.38f)
        lineTo(w * 0.42f, h * 0.18f); lineTo(w * 0.52f, h * 0.38f); lineTo(w * 0.46f, h * 0.38f)
        lineTo(w * 0.56f, h * 0.55f); lineTo(w * 0.56f, h); close()
        // 右树
        moveTo(w * 0.62f, h); lineTo(w * 0.70f, h * 0.45f); lineTo(w * 0.65f, h * 0.45f)
        lineTo(w * 0.73f, h * 0.28f); lineTo(w * 0.82f, h * 0.48f); lineTo(w * 0.76f, h * 0.48f)
        lineTo(w * 0.84f, h * 0.60f); lineTo(w * 0.90f, h); close()
    }
    drawPath(treePath, color = Color(0xFF040B04))

    // 地面草丛（波浪）
    val groundPath = Path().apply {
        moveTo(0f, h * 0.88f)
        cubicTo(w * 0.1f, h * 0.82f, w * 0.2f, h * 0.90f, w * 0.3f, h * 0.85f)
        cubicTo(w * 0.4f, h * 0.80f, w * 0.5f, h * 0.88f, w * 0.6f, h * 0.83f)
        cubicTo(w * 0.7f, h * 0.78f, w * 0.85f, h * 0.86f, w, h * 0.82f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(groundPath, color = Color(0xFF060E06))
    // 草丛顶部浅绿
    drawPath(groundPath, brush = Brush.verticalGradient(
        listOf(Color(0xFF1A3A15).copy(alpha = 0.6f), Color.Transparent),
        startY = h * 0.78f, endY = h * 0.92f
    ))
}

private fun DrawScope.drawAbyssScene(tick: Float) {
    val w = size.width; val h = size.height
    // 触手从底部伸出（随时间缓慢摆动）
    val tentacleColor = Color(0xFF1A0830)
    repeat(5) { i ->
        val baseX = w * (0.1f + i * 0.2f)
        val sway = sin((tick * 2 * Math.PI + i * 1.2).toDouble()).toFloat() * 12f
        val tentaclePath = Path().apply {
            moveTo(baseX, h)
            cubicTo(
                baseX + sway,     h * 0.7f,
                baseX - sway * 0.5f, h * 0.5f,
                baseX + sway * 0.8f, h * 0.25f + i * 8f
            )
        }
        drawPath(
            path   = tentaclePath,
            color  = tentacleColor,
            style  = Stroke(width = 8f - i * 0.5f, cap = StrokeCap.Round)
        )
    }
    // 深渊裂缝
    val crackPath = Path().apply {
        moveTo(w * 0.4f, h)
        lineTo(w * 0.42f, h * 0.7f)
        lineTo(w * 0.38f, h * 0.55f)
        lineTo(w * 0.44f, h * 0.4f)
        lineTo(w * 0.40f, h * 0.25f)
    }
    drawPath(crackPath, color = Color(0xFF7B4FC8).copy(alpha = 0.4f), style = Stroke(width = 2f))
    // 紫色涟漪
    val rippleAlpha = ((sin(tick * 2 * Math.PI * 2) + 1) / 2).toFloat() * 0.3f
    drawCircle(
        brush  = Brush.radialGradient(
            listOf(Color(0xFF7B4FC8).copy(alpha = rippleAlpha), Color.Transparent)
        ),
        radius = w * 0.4f,
        center = Offset(w * 0.5f, h * 0.6f)
    )
}

private fun DrawScope.drawTempleScene(tick: Float) {
    val w = size.width; val h = size.height
    // 星空
    listOf(0.2f to 0.1f, 0.5f to 0.06f, 0.75f to 0.15f, 0.1f to 0.2f, 0.88f to 0.09f).forEach { (x, y) ->
        val twinkle = ((sin((tick * 6.28 + x * 10).toDouble()) + 1) / 2).toFloat()
        drawCircle(Color.White.copy(alpha = 0.4f + twinkle * 0.4f), radius = 1.5f, center = Offset(w * x, h * y))
    }
    // 月亮满月
    drawCircle(Color(0xFFD8D0F0), radius = w * 0.09f, center = Offset(w * 0.5f, h * 0.25f))
    drawCircle(Color(0xFF8890D8).copy(alpha = 0.3f), radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.25f))

    // 神殿尖顶轮廓
    val templePath = Path().apply {
        // 左塔
        moveTo(w * 0.05f, h); lineTo(w * 0.05f, h * 0.55f)
        lineTo(w * 0.12f, h * 0.35f); lineTo(w * 0.19f, h * 0.55f); lineTo(w * 0.19f, h)
        // 中央大塔
        moveTo(w * 0.30f, h); lineTo(w * 0.30f, h * 0.5f)
        lineTo(w * 0.38f, h * 0.28f); lineTo(w * 0.50f, h * 0.12f)
        lineTo(w * 0.62f, h * 0.28f); lineTo(w * 0.70f, h * 0.5f); lineTo(w * 0.70f, h)
        // 右塔
        moveTo(w * 0.81f, h); lineTo(w * 0.81f, h * 0.55f)
        lineTo(w * 0.88f, h * 0.35f); lineTo(w * 0.95f, h * 0.55f); lineTo(w * 0.95f, h)
    }
    drawPath(templePath, color = Color(0xFF0C0C20))
    // 神殿窗口发光
    listOf(0.1f to 0.65f, 0.5f to 0.55f, 0.85f to 0.65f).forEach { (x, y) ->
        drawRect(
            color   = Color(0xFF8890D8).copy(alpha = 0.5f + ((sin((tick * 3.14 + x * 5).toDouble()) + 1) / 2).toFloat() * 0.3f),
            topLeft = Offset(w * x - 4f, h * y),
            size    = androidx.compose.ui.geometry.Size(8f, 12f)
        )
    }
}

private fun DrawScope.drawRuinsScene(tick: Float) {
    val w = size.width; val h = size.height
    // 废墟石拱门轮廓
    val archPath = Path().apply {
        // 左石柱
        moveTo(w * 0.1f, h); lineTo(w * 0.1f, h * 0.3f)
        lineTo(w * 0.22f, h * 0.3f); lineTo(w * 0.22f, h)
        // 拱顶（不完整，一侧碎裂）
        moveTo(w * 0.10f, h * 0.30f)
        cubicTo(w * 0.10f, h * 0.08f, w * 0.40f, h * 0.08f, w * 0.40f, h * 0.30f)
        // 右石柱（残缺）
        moveTo(w * 0.55f, h); lineTo(w * 0.55f, h * 0.45f)
        lineTo(w * 0.65f, h * 0.42f); lineTo(w * 0.68f, h)
        // 背景破墙
        moveTo(w * 0.72f, h); lineTo(w * 0.72f, h * 0.38f)
        lineTo(w * 0.78f, h * 0.32f); lineTo(w * 0.80f, h * 0.38f)
        lineTo(w * 0.88f, h * 0.35f); lineTo(w * 0.92f, h * 0.5f); lineTo(w * 0.95f, h)
    }
    drawPath(archPath, color = Color(0xFF1A0C08))
    drawPath(archPath, color = Color(0xFF4A2010).copy(alpha = 0.3f), style = Stroke(width = 1.5f))

    // 地面余烬（橙色粒子）
    repeat(8) { i ->
        val px = w * (0.08f + i * 0.12f) + sin((tick * 3 + i).toDouble()).toFloat() * 6f
        val py = h * 0.88f
        val emberAlpha = ((sin((tick * 4 + i * 1.5).toDouble()) + 1) / 2).toFloat() * 0.7f + 0.1f
        drawCircle(
            color  = Color(0xFFFF6020).copy(alpha = emberAlpha),
            radius = 2f + i % 3f,
            center = Offset(px, py - i * 3f)
        )
    }
    // 灰烟
    drawCircle(
        brush  = Brush.radialGradient(
            listOf(Color(0xFF3A2010).copy(alpha = 0.25f), Color.Transparent)
        ),
        radius = w * 0.5f,
        center = Offset(w * 0.35f, h * 0.7f)
    )
}

private fun DrawScope.drawGlacierScene(tick: Float) {
    val w = size.width; val h = size.height
    // 极光（水平渐变带，缓慢漂移）
    val auroraY = h * 0.15f + sin((tick * 1.5).toDouble()).toFloat() * 10f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color.Transparent, Color(0xFF60B8D8).copy(alpha = 0.15f), Color.Transparent),
            startY = auroraY - 30f, endY = auroraY + 30f
        )
    )
    // 冰峰轮廓
    val peakPath = Path().apply {
        moveTo(0f, h)
        lineTo(w * 0.12f, h * 0.42f)
        lineTo(w * 0.22f, h * 0.58f)
        lineTo(w * 0.35f, h * 0.28f)  // 主峰
        lineTo(w * 0.48f, h * 0.52f)
        lineTo(w * 0.60f, h * 0.35f)  // 次峰
        lineTo(w * 0.72f, h * 0.55f)
        lineTo(w * 0.82f, h * 0.40f)
        lineTo(w * 0.92f, h * 0.60f)
        lineTo(w, h); close()
    }
    drawPath(peakPath, color = Color(0xFF0A1828))
    // 冰雪高光
    drawPath(peakPath, brush = Brush.verticalGradient(
        listOf(Color(0xFFBBDDEE).copy(alpha = 0.25f), Color.Transparent),
        startY = h * 0.25f, endY = h * 0.65f
    ))
    // 飘雪粒子
    repeat(12) { i ->
        val snowX = w * ((i * 0.085f + tick * 0.3f) % 1.0f)
        val snowY = h * ((i * 0.12f + tick * 0.15f) % 0.9f)
        drawCircle(Color.White.copy(alpha = 0.4f), radius = 1.5f, center = Offset(snowX, snowY))
    }
}
