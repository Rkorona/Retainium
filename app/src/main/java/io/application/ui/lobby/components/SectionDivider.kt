package io.application.ui.lobby.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.application.ui.theme.GoldAncient
import io.application.ui.theme.GoldDim
import io.application.ui.theme.ScrollTextDim

/**
 * 章节分割线 — 两侧渐变墨线 + 中央金色文字 + 菱形装饰
 */
@Composable
fun SectionDivider(
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧渐变线
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(
                brush       = Brush.horizontalGradient(
                    listOf(Color.Transparent, GoldDim.copy(alpha = 0.7f))
                ),
                start       = Offset(0f, 0f),
                end         = Offset(size.width, 0f),
                strokeWidth = 1.5f
            )
        }

        // 左菱形装饰
        Text("◇", color = GoldDim, style = MaterialTheme.typography.labelSmall)

        Spacer(Modifier.width(10.dp))

        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            color = ScrollTextDim,
            letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
        )

        Spacer(Modifier.width(10.dp))

        // 右菱形装饰
        Text("◇", color = GoldDim, style = MaterialTheme.typography.labelSmall)

        // 右侧渐变线
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
        ) {
            drawLine(
                brush       = Brush.horizontalGradient(
                    listOf(GoldDim.copy(alpha = 0.7f), Color.Transparent)
                ),
                start       = Offset(0f, 0f),
                end         = Offset(size.width, 0f),
                strokeWidth = 1.5f
            )
        }
    }
}
