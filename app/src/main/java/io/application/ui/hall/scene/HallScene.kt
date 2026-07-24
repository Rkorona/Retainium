package io.application.ui.hall.scene

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.ui.theme.Amber
import io.application.ui.theme.Mist

@Composable
fun RoomScene(
    memoryPhrase: String,
    memoryCaption: String,
    isEntering: Boolean,
) {
    val infinite = rememberInfiniteTransition(label = "room-breath")
    val breath by infinite.animateFloat(
        initialValue = .96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            tween(2_800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "room-breath-scale",
    )
    val glow by infinite.animateFloat(
        initialValue = .35f,
        targetValue = .72f,
        animationSpec = infiniteRepeatable(
            tween(1_900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "fire-glow",
    )
    val enteringScale by animateFloatAsState(
        targetValue = if (isEntering) 1.18f else 1f,
        animationSpec = tween(1_200, easing = FastOutSlowInEasing),
        label = "gate-scale",
    )

    Box(
        modifier = Modifier
            .fillMaxHeight(.82f)
            .fillMaxWidth(.84f)
            .scale(enteringScale)
            .drawBehind {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Amber.copy(alpha = glow * .22f), Color.Transparent),
                        center = center,
                        radius = size.minDimension * .46f,
                    ),
                    radius = size.minDimension * .46f,
                    center = center,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * .26f
            drawCircle(
                color = Mist.copy(alpha = .035f),
                radius = radius * 1.55f,
                center = center,
            )
            drawCircle(
                color = Mist.copy(alpha = .08f),
                radius = radius * 1.12f,
                center = center,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawCircle(
                color = Amber.copy(alpha = .17f),
                radius = radius * breath,
                center = center,
                style = Stroke(width = 1.2.dp.toPx()),
            )
            drawArc(
                color = Amber.copy(alpha = .45f),
                startAngle = 205f,
                sweepAngle = 82f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 1.55f, center.y - radius * 1.55f),
                size = Size(radius * 3.1f, radius * 3.1f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = Mist.copy(alpha = .32f),
                startAngle = 25f,
                sweepAngle = 62f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 1.55f, center.y - radius * 1.55f),
                size = Size(radius * 3.1f, radius * 3.1f),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round),
            )
        }

        Column(
            modifier = Modifier.offset(y = (-6).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            androidx.compose.material3.Text(
                text = "“",
                color = Amber.copy(alpha = .65f),
                fontSize = 46.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            androidx.compose.material3.Text(
                text = memoryPhrase,
                color = Mist,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                lineHeight = 36.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.Text(
                text = memoryCaption,
                color = Mist.copy(alpha = .45f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
fun FloatingDust() {
    val infinite = rememberInfiniteTransition(label = "dust")
    val shimmer by infinite.animateFloat(
        initialValue = .25f,
        targetValue = .6f,
        animationSpec = infiniteRepeatable(tween(2_600), RepeatMode.Reverse),
        label = "dust-shimmer",
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dots = listOf(
            Offset(size.width * .12f, size.height * .22f),
            Offset(size.width * .82f, size.height * .18f),
            Offset(size.width * .68f, size.height * .52f),
            Offset(size.width * .18f, size.height * .7f),
            Offset(size.width * .9f, size.height * .78f),
        )
        dots.forEachIndexed { index, point ->
            drawCircle(
                color = if (index % 2 == 0) {
                    Amber.copy(alpha = shimmer * .35f)
                } else {
                    Mist.copy(alpha = shimmer * .22f)
                },
                radius = (index % 3 + 1).dp.toPx(),
                center = point,
            )
        }
    }
}

fun DrawScope.drawRoomAtmosphere(size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4C3349).copy(alpha = .36f), Color.Transparent),
            center = Offset(size.width * .72f, size.height * .26f),
            radius = size.maxDimension * .72f,
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7C552A).copy(alpha = .2f), Color.Transparent),
            center = Offset(size.width * .36f, size.height * .72f),
            radius = size.maxDimension * .6f,
        ),
    )
    val path = Path().apply {
        moveTo(0f, size.height * .82f)
        cubicTo(
            size.width * .2f, size.height * .76f,
            size.width * .34f, size.height * .88f,
            size.width * .56f, size.height * .8f,
        )
        cubicTo(
            size.width * .74f, size.height * .74f,
            size.width * .88f, size.height * .83f,
            size.width, size.height * .78f,
        )
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(path = path, color = Color(0xFF15131A).copy(alpha = .48f))
}