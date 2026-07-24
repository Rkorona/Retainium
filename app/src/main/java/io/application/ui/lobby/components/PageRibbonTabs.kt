package io.application.ui.lobby.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.ui.theme.*

/**
 * 书签丝带导航 — 固定在页面右侧边缘
 *
 * 像真实古籍的彩色书签，每个丝带代表一个游戏章节。
 * 选中的书签向左凸出，颜色高亮。
 * 文字竖排旋转 90°，完全替代底部 Tab Bar。
 */

private data class RibbonTab(
    val label: String,
    val color: Color,
    val dimColor: Color
)

private val RIBBON_TABS = listOf(
    RibbonTab("副  本", Color(0xFF7B4FC8), Color(0xFF3A2060)),
    RibbonTab("大  厅", GoldAncient,       GoldDim),
    RibbonTab("装  备", Color(0xFF4A7FA5), Color(0xFF1A3050)),
    RibbonTab("日  志", Color(0xFF5A9B6A), Color(0xFF1A3020)),
)

@Composable
fun PageRibbonTabs(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 1,
    onSelect: (Int) -> Unit = {}
) {
    Column(
        modifier           = modifier
            .fillMaxHeight()
            .padding(vertical = 80.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End
    ) {
        RIBBON_TABS.forEachIndexed { index, tab ->
            val isSelected = index == selectedIndex

            val slideOffset by animateFloatAsState(
                targetValue   = if (isSelected) 0f else 10f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "ribbon_$index"
            )

            Box(
                modifier = Modifier
                    .offset(x = slideOffset.dp)
                    .width(if (isSelected) 32.dp else 26.dp)
                    .height(72.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart    = 6.dp,
                            bottomStart = 6.dp,
                            topEnd      = 0.dp,
                            bottomEnd   = 0.dp
                        )
                    )
                    .background(if (isSelected) tab.color else tab.dimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = tab.label,
                    fontSize = 10.sp,
                    color    = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.rotate(90f),
                    letterSpacing = 1.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
