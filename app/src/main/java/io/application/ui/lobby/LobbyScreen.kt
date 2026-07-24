package io.application.ui.lobby

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.model.SampleData
import io.application.ui.lobby.components.*
import io.application.ui.theme.*

/**
 * 大厅主屏幕 — 活页手稿
 *
 * 层级结构:
 *   Box (全屏)
 *     ├── ParchmentBackground       羊皮纸质感背景
 *     ├── RuneParticles             环境符文粒子
 *     └── Column (可滚动内容)
 *           ├── ManuscriptHeader    世界标题 + 章节（墨水书写动画）
 *           ├── PlayerStatusCard    玩家状态（弹簧进度条）
 *           ├── SectionDivider      分割墨线
 *           ├── LazyRow (副本卡片)  横向可滑动副本列表
 *           ├── ChapterProgress     章节进度卷轴
 *           └── BottomSpacer        为底部导航留空
 *     └── ManuscriptBottomBar       底部导航（自定义手稿风格）
 */
@Composable
fun LobbyScreen() {
    val player  = SampleData.player
    val dungeons = SampleData.dungeons

    Box(modifier = Modifier.fillMaxSize()) {

        // ── 背景层 ─────────────────────────────────────────────────
        ParchmentBackground()

        // ── 符文粒子层 ──────────────────────────────────────────────
        RuneParticles()

        // ── 主内容层 ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 世界大标题 ─────────────────────────────────────────
            ManuscriptHeader(
                worldName    = "虚空编年史",
                chapterTitle = player.currentChapter
            )

            Spacer(Modifier.height(24.dp))

            // ── 玩家状态 ────────────────────────────────────────────
            PlayerStatusCard(player = player)

            Spacer(Modifier.height(32.dp))

            // ── 副本列表标题 ────────────────────────────────────────
            SectionDivider(label = "可进入的副本")

            Spacer(Modifier.height(16.dp))

            // ── 副本横向卡片 ────────────────────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(dungeons, key = { it.id }) { dungeon ->
                    DungeonCard(dungeon = dungeon)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── 章节进度卷轴 ────────────────────────────────────────
            SectionDivider(label = "当前卷轴进度")

            Spacer(Modifier.height(16.dp))

            ChapterProgressSection(
                chapterTitle   = player.currentChapter,
                progress       = player.chapterProgress
            )

            Spacer(Modifier.height(32.dp))

            // ── 世界状态简报 ────────────────────────────────────────
            SectionDivider(label = "世界动态")

            Spacer(Modifier.height(16.dp))

            WorldNewsSection()

            // 为底部导航留出空间
            Spacer(Modifier.height(96.dp))
        }

        // ── 底部导航 ─────────────────────────────────────────────────
        ManuscriptBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── 世界大标题 + 章节书写动画 ─────────────────────────────────────────

@Composable
private fun ManuscriptHeader(
    worldName: String,
    chapterTitle: String
) {
    var worldDone by remember { mutableStateOf(false) }

    // 顶部装饰纹
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
            drawLine(
                brush       = Brush.horizontalGradient(
                    listOf(Color.Transparent, GoldAncient.copy(alpha = 0.6f), GoldBright, GoldAncient.copy(alpha = 0.6f), Color.Transparent)
                ),
                start       = androidx.compose.ui.geometry.Offset(0f, 0f),
                end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.5f
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // 世界名（大标题）
    InkWritingText(
        text       = worldName,
        style      = MaterialTheme.typography.displayMedium.copy(
            textAlign = TextAlign.Center
        ),
        color      = GoldBright,
        modifier   = Modifier.fillMaxWidth(),
        charDelayMs = 80L,
        onComplete = { worldDone = true }
    )

    Spacer(Modifier.height(6.dp))

    // 装饰分隔符
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text("─── ", color = GoldDim, fontSize = 12.sp)
        Text("✦", color = GoldAncient, fontSize = 14.sp)
        Text(" ───", color = GoldDim, fontSize = 12.sp)
    }

    Spacer(Modifier.height(6.dp))

    // 章节名（等世界名写完后再出现）
    if (worldDone) {
        InkWritingText(
            text      = chapterTitle,
            style     = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            ),
            color     = ScrollTextDim,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            delayMs   = 100L,
            charDelayMs = 40L
        )
    }

    Spacer(Modifier.height(16.dp))

    // 底部装饰纹
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            drawLine(
                brush       = Brush.horizontalGradient(
                    listOf(Color.Transparent, GoldDim.copy(alpha = 0.4f), Color.Transparent)
                ),
                start       = androidx.compose.ui.geometry.Offset(0f, 0f),
                end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1f
            )
        }
    }
}

// ── 章节进度卷轴 ──────────────────────────────────────────────────────

@Composable
private fun ChapterProgressSection(
    chapterTitle: String,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessVeryLow
        ),
        label = "chapterProgress"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF13101E))
            .border(1.dp, GoldDim.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = chapterTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ScrollText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text  = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAncient,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // 卷轴式进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0A0818))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldDim, GoldAncient, GoldBright)
                            ),
                            RoundedCornerShape(4.dp)
                        )
                )
                // 刻度线（每 20%）
                repeat(4) { i ->
                    val fraction = (i + 1) / 5f
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .offset(x = (fraction * 1000).dp) // 相对宽度近似，在Canvas里会更精准
                            .background(Color(0xFF0A0818).copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "继续推进主线，解锁下一章",
                style = MaterialTheme.typography.bodySmall,
                color = ScrollTextFaint,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// ── 世界动态简报 ───────────────────────────────────────────────────────

private val worldNews = listOf(
    "᛭  深渊裂缝的涌动频率近日异常增加",
    "ᚱ  银月神殿守卫报告有不明旅者接近圣所",
    "ᚠ  幽暗之林深处，有人听见了久违的树语",
    "ᛞ  遗忘之地边境出现无名符文，尚未破译",
)

@Composable
private fun WorldNewsSection() {
    Column(
        modifier              = Modifier.padding(horizontal = 20.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        worldNews.forEach { news ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF100D1A))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(GoldDim.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text  = news,
                    style = MaterialTheme.typography.bodySmall,
                    color = ScrollTextDim,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

// ── 底部导航 ────────────────────────────────────────────────────────────

private data class NavItem(val icon: String, val label: String)
private val navItems = listOf(
    NavItem("⚔", "副本"),
    NavItem("᛭", "大厅"),
    NavItem("◈", "装备"),
    NavItem("✦", "日志"),
)

@Composable
private fun ManuscriptBottomBar(modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableIntStateOf(1) } // 默认选中"大厅"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, InkVoid.copy(alpha = 0.95f), InkVoid)
                )
            )
            .navigationBarsPadding()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                ManuscriptNavItem(
                    icon      = item.icon,
                    label     = item.label,
                    isSelected = index == selectedIndex,
                    onClick   = { selectedIndex = index }
                )
            }
        }

        // 顶边装饰线
        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
            drawLine(
                brush       = Brush.horizontalGradient(
                    listOf(Color.Transparent, GoldDim.copy(alpha = 0.5f), GoldAncient.copy(alpha = 0.6f), GoldDim.copy(alpha = 0.5f), Color.Transparent)
                ),
                start       = androidx.compose.ui.geometry.Offset(0f, 0f),
                end         = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
private fun ManuscriptNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconScale by animateFloatAsState(
        targetValue   = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "navScale"
    )
    val labelAlpha by animateFloatAsState(
        targetValue   = if (isSelected) 1f else 0.45f,
        animationSpec = tween(200),
        label = "navAlpha"
    )

    Column(
        modifier          = Modifier
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 选中时顶部小光点
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 2.dp)
                .background(
                    if (isSelected) GoldAncient.copy(alpha = 0.8f) else Color.Transparent,
                    RoundedCornerShape(1.dp)
                )
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text     = icon,
            fontSize = (20 * iconScale).sp,
            color    = if (isSelected) GoldAncient else ScrollTextDim.copy(alpha = labelAlpha)
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) GoldAncient.copy(alpha = 0.9f) else ScrollTextFaint
        )
    }
}
