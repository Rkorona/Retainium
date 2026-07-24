package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.model.PlayerStatus
import io.application.ui.theme.*

/**
 * 玩家状态卡 — 撕裂羊皮纸美学
 *
 * 顶部留空给装饰符印，中间是姓名/等级/称号，
 * 底部是 HP/MP 手写风格进度条。
 * M3 Expressive: 弹簧动画进度条、悬浮光晕。
 */
@Composable
fun PlayerStatusCard(
    player: PlayerStatus,
    modifier: Modifier = Modifier
) {
    // 进度条弹簧动画
    val hpFraction = player.hp.toFloat() / player.maxHp
    val mpFraction = player.mp.toFloat() / player.maxMp

    val animatedHp by animateFloatAsState(
        targetValue   = hpFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "hp"
    )
    val animatedMp by animateFloatAsState(
        targetValue   = mpFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "mp"
    )

    // 符印呼吸光晕
    val infiniteTransition = rememberInfiniteTransition(label = "sigilGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.70f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier = modifier.padding(horizontal = 20.dp)) {
        // 底层：卡片主体（深色羊皮纸表面）
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(
                topStart     = 4.dp,
                topEnd       = 4.dp,
                bottomStart  = 14.dp,
                bottomEnd    = 14.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16121E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // 卡片顶边：黄金分割线
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GoldAncient, GoldBright, GoldAncient, Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {

                // ── 顶行：符印 + 名字/称号 ────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // 符印圆形区域（呼吸光晕）
                    Box(contentAlignment = Alignment.Center) {
                        // 外层光晕
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            GoldAncient.copy(alpha = glowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        // 内圆边框
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF0E0B18), RoundedCornerShape(50))
                                .clip(RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text     = player.sigil,
                                fontSize = 20.sp,
                                color    = GoldAncient
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text  = player.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = ScrollText,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 等级徽章
                            Box(
                                modifier = Modifier
                                    .background(GoldDim.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text  = "Lv.${player.level}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text      = player.title,
                                style     = MaterialTheme.typography.bodySmall,
                                color     = ScrollTextDim,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 分割墨线
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, GoldDim.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                Spacer(Modifier.height(16.dp))

                // ── HP 进度条 ─────────────────────────────────────────
                StatusBar(
                    label    = "❤  生命",
                    current  = player.hp,
                    max      = player.maxHp,
                    fraction = animatedHp,
                    barColor = BloodHP,
                    textColor= ScrollTextDim
                )

                Spacer(Modifier.height(10.dp))

                // ── MP 进度条 ─────────────────────────────────────────
                StatusBar(
                    label    = "◆  法力",
                    current  = player.mp,
                    max      = player.maxMp,
                    fraction = animatedMp,
                    barColor = SapphireMP,
                    textColor= ScrollTextDim
                )

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StatusBar(
    label: String,
    current: Int,
    max: Int,
    fraction: Float,
    barColor: Color,
    textColor: Color
) {
    Column {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
            Text(
                text  = "$current / $max",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
        }
        Spacer(Modifier.height(5.dp))
        // 轨道背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color(0xFF0A0810), RoundedCornerShape(3.dp))
        ) {
            // 填充部分
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.7f), barColor)
                        ),
                        RoundedCornerShape(3.dp)
                    )
            )
            // 高光
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(alpha = 0.0f), Color.White.copy(alpha = 0.25f))
                        ),
                        RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                    )
            )
        }
    }
}
