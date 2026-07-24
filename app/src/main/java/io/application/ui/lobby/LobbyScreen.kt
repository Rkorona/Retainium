package io.application.ui.lobby

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.model.PlayerStatus
import io.application.model.SampleData
import io.application.ui.lobby.components.*
import io.application.ui.theme.*

/**
 * 大厅主屏幕 — 重构为「叙述之页」
 *
 * 彻底抛弃所有传统游戏 UI 范式：
 *  × 无卡片  × 无进度条  × 无底部 Tab  × 无汉堡菜单
 *
 *  ✓ 整个屏幕 = 一页正在被书写的古籍
 *  ✓ 玩家状态 = 书页中的手写散文 + 边注插图（烛火/月相）
 *  ✓ 副本入口 = 古籍词条，点击原地弹簧展开，显示微型场景
 *  ✓ 导航    = 右侧彩色书签丝带（真实书籍 page tab 样式）
 *  ✓ 菜单    = 右下角蜡印按钮
 *
 * 层级：
 *   Box (全屏)
 *     ├─ ParchmentBackground  羊皮纸背景
 *     ├─ RuneParticles         环境符文粒子
 *     ├─ Row
 *     │   ├─ 书页主体 (weight 1f, 可竖向滚动)
 *     │   └─ PageRibbonTabs   右侧书签丝带 (固定不滚动)
 *     └─ WaxSealButton         右下角蜡印
 */
@Composable
fun LobbyScreen() {
    val player   = SampleData.player
    val dungeons = SampleData.dungeons
    var ribbonSelected by remember { mutableIntStateOf(1) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── 背景 ────────────────────────────────────────────────────
        ParchmentBackground()
        RuneParticles()

        // ── 主体：书页 + 书签 ────────────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {

            // 书页内容（可滚动）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .systemBarsPadding()
                    .padding(start = 22.dp, end = 14.dp, bottom = 24.dp)
            ) {

                // 书眉（Running head）
                BookRunningHead(
                    worldName   = "虚空编年史",
                    chapterName = "第三章"
                )

                PageDivider(symbol = "❧")

                Spacer(Modifier.height(4.dp))

                // 玩家状态散文区
                PlayerProseStat(player = player)

                PageDivider(symbol = "⁂")

                Spacer(Modifier.height(8.dp))

                // 章节标题
                ChapterHeading(title = player.currentChapter)

                Spacer(Modifier.height(12.dp))

                // 副本序言散文
                PrologueProse()

                Spacer(Modifier.height(16.dp))

                // 副本词条列表
                dungeons.forEach { dungeon ->
                    DungeonEntryRow(dungeon = dungeon)
                }

                Spacer(Modifier.height(16.dp))

                PageDivider(symbol = "※")

                Spacer(Modifier.height(12.dp))

                // 章节进度（书页脚注风格）
                ChapterFooter(
                    chapterTitle = player.currentChapter,
                    progress     = player.chapterProgress
                )

                Spacer(Modifier.height(16.dp))

                // 世界动态（注脚）
                WorldAnnotations()

                Spacer(Modifier.height(80.dp))
            }

            // 书签丝带（固定，不随内容滚动）
            PageRibbonTabs(
                modifier      = Modifier.fillMaxHeight(),
                selectedIndex = ribbonSelected,
                onSelect      = { ribbonSelected = it }
            )
        }

        // 蜡印按钮（右下角）
        WaxSealButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 20.dp, end = 44.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// 子组件（全部书页排版风格）
// ─────────────────────────────────────────────────────────────────────────

/** 书眉：左侧世界名，右侧章节简称，中间点线 */
@Composable
private fun BookRunningHead(worldName: String, chapterName: String) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InkWritingText(
            text        = worldName,
            style       = MaterialTheme.typography.labelMedium,
            color       = GoldAncient,
            charDelayMs = 60L
        )
        // 点线
        Text(
            text     = "· · · · · · · · · · · ·",
            fontSize = 8.sp,
            color    = GoldDim.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
            textAlign = TextAlign.Center,
            maxLines  = 1
        )
        Text(
            text  = chapterName,
            style = MaterialTheme.typography.labelMedium,
            color = ScrollTextDim
        )
    }
}

/** 装饰分割线：两侧细线，中央符号 */
@Composable
private fun PageDivider(symbol: String = "✦") {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 0.5.dp,
            color     = GoldDim.copy(alpha = 0.35f)
        )
        Text(
            text     = "  $symbol  ",
            fontSize = 13.sp,
            color    = GoldAncient.copy(alpha = 0.7f)
        )
        HorizontalDivider(
            modifier  = Modifier.weight(1f),
            thickness = 0.5.dp,
            color     = GoldDim.copy(alpha = 0.35f)
        )
    }
}

/**
 * 玩家状态 — 书页散文 + 左侧边注插图（烛火 & 月相）
 *
 * 布局：
 *  Row:
 *    [烛火图] [月相图]  |  Column:
 *                           名字·称号·境界
 *                           生命之火：847/1000
 *                           法力盈亏：423/600
 */
@Composable
private fun PlayerProseStat(player: PlayerStatus) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧：边注插图（竖排两个图标）
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(end = 14.dp, top = 4.dp)
        ) {
            FlameStatIcon(
                fraction = player.hp.toFloat() / player.maxHp,
                modifier = Modifier.size(width = 20.dp, height = 30.dp)
            )
            MoonStatIcon(
                fraction = player.mp.toFloat() / player.maxMp,
                modifier = Modifier.size(20.dp)
            )
        }

        // 右侧：散文描述
        Column(modifier = Modifier.weight(1f)) {
            // 名字行
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = player.name,
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = GoldBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text     = "，${player.title}，境界${numberToChinese(player.level)}。",
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = ScrollTextDim,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // HP 行
            StatProseRow(
                label    = "生命之火",
                current  = player.hp,
                max      = player.maxHp,
                barColor = BloodHP
            )

            Spacer(Modifier.height(4.dp))

            // MP 行
            StatProseRow(
                label    = "法力盈亏",
                current  = player.mp,
                max      = player.maxMp,
                barColor = SapphireMP
            )
        }
    }
}

/** 单行状态：标签 + 细长手绘风进度槽 + 数值 */
@Composable
private fun StatProseRow(
    label: String,
    current: Int,
    max: Int,
    barColor: Color
) {
    val fraction = current.toFloat() / max

    val animFraction by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "stat_$label"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth()
    ) {
        // 手写风格标签
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = ScrollTextDim,
            modifier = Modifier.width(52.dp),
            fontStyle = FontStyle.Italic
        )

        Text(
            text  = "：",
            style = MaterialTheme.typography.labelSmall,
            color = ScrollTextFaint
        )

        // 羊毛纸条式进度槽（极细，像手绘刻度尺）
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF0A0818))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFraction)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(barColor.copy(alpha = 0.6f), barColor)
                        ),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        // 数值
        Text(
            text     = "  $current",
            style    = MaterialTheme.typography.labelSmall,
            color    = ScrollTextDim,
            modifier = Modifier.width(36.dp)
        )
    }
}

/** 章节大标题（居中，仿古籍章首排版） */
@Composable
private fun ChapterHeading(title: String) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text      = "※ ※ ※",
            fontSize  = 10.sp,
            color     = GoldDim.copy(alpha = 0.6f),
            letterSpacing = 6.sp
        )
        Spacer(Modifier.height(8.dp))
        InkWritingText(
            text      = title,
            style     = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center
            ),
            color     = ScrollText,
            delayMs   = 200L,
            charDelayMs = 45L,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

/** 副本序言：两三句描述当前章节氛围的散文 */
@Composable
private fun PrologueProse() {
    Text(
        text = "旅人凌霄立于迷雾边缘，脚下的大地低声诉说着被遗忘者的名字。" +
               "三道裂缝横亘于夜色深处，等待着有人踏入——或永不归来。",
        style     = MaterialTheme.typography.bodyMedium,
        color     = ScrollTextDim,
        fontStyle = FontStyle.Italic,
        lineHeight = 22.sp,
        modifier  = Modifier.fillMaxWidth()
    )
}

/** 章节进度脚注 */
@Composable
private fun ChapterFooter(chapterTitle: String, progress: Float) {
    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
        label         = "chapterProgress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "卷轴进度",
                style = MaterialTheme.typography.labelSmall,
                color = ScrollTextFaint,
                fontStyle = FontStyle.Italic
            )
            Text(
                text  = "${(animProgress * 100).toInt()}%  完成",
                style = MaterialTheme.typography.labelSmall,
                color = GoldAncient,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(6.dp))

        // 极细进度轨（仿古籍刻度）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(Color(0xFF0A0818))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(GoldDim, GoldAncient, GoldBright)),
                        RoundedCornerShape(1.5.dp)
                    )
            )
        }
    }
}

/** 世界动态注脚（古籍边注/批注风格） */
private val WORLD_NOTES = listOf(
    "᛭" to "深渊裂缝的涌动近日异常，守境者已发出预警。",
    "ᚱ" to "银月神殿守卫报告有不明旅者在圣所外徘徊。",
    "ᚠ" to "幽暗之林深处，久违的树语再度响起。",
    "ᛞ" to "遗忘之地边境现无名符文，学者尚未破译。",
)

@Composable
private fun WorldAnnotations() {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text  = "— 世界动态 —",
            style = MaterialTheme.typography.labelSmall,
            color = ScrollTextFaint,
            fontStyle   = FontStyle.Italic,
            letterSpacing = 2.sp,
            modifier    = Modifier.fillMaxWidth(),
            textAlign   = TextAlign.Center
        )

        WORLD_NOTES.forEach { (rune, text) ->
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text     = rune,
                    fontSize = 11.sp,
                    color    = GoldDim.copy(alpha = 0.7f),
                    modifier = Modifier
                        .padding(end = 8.dp, top = 1.dp)
                        .width(14.dp)
                )
                Text(
                    text      = text,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = ScrollTextFaint,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/** 将阿拉伯数字转换为中文数字（简版，1-99） */
private fun numberToChinese(n: Int): String {
    val units = listOf("", "十", "二十", "三十", "四十", "五十", "六十", "七十", "八十", "九十")
    val digits = listOf("", "一", "二", "三", "四", "五", "六", "七", "八", "九")
    return if (n < 10) digits[n]
    else units[n / 10] + digits[n % 10]
}
