package io.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.ui.theme.ApplicationTheme
import io.application.ui.theme.Amber
import io.application.ui.theme.Ink
import io.application.ui.theme.Mist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApplicationTheme {
                HallOfEchoes()
            }
        }
    }
}

@Composable
private fun HallOfEchoes() {
    var openPanel by remember { mutableStateOf<HallPanel?>(null) }
    var isEntering by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun enterStory() {
        if (!isEntering) {
            isEntering = true
            scope.launch {
                delay(1_400)
                toastMessage = "下一幕正在翻页……"
                delay(1_100)
                isEntering = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .drawBehind { drawRoomAtmosphere(size) }
    ) {
        FloatingDust()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            HallTopBar(
                onMenuClick = {
                    toastMessage = "书房的门暂时只向故事敞开"
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                RoomScene(isEntering = isEntering)

                Whisper(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 26.dp)
                )

                StoryGate(
                    modifier = Modifier.align(Alignment.TopEnd),
                    isEntering = isEntering,
                    onEnter = { enterStory() }
                )

                OrbitButton(
                    label = "余响",
                    detail = "3 条未读",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 92.dp),
                    tint = Mist,
                    onClick = { openPanel = HallPanel.Echoes }
                )

                OrbitButton(
                    label = "行囊",
                    detail = "7 件遗物",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = 50.dp),
                    tint = Amber,
                    onClick = { openPanel = HallPanel.Bag }
                )

                OrbitButton(
                    label = "誓言",
                    detail = "仍在燃烧",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-4).dp),
                    tint = Color(0xFFB88BD9),
                    onClick = { openPanel = HallPanel.Vow }
                )
            }

            BottomPrompt(onEnter = { enterStory() })
        }

        AnimatedVisibility(
            visible = openPanel != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(tween(180)) + slideInVertically(tween(420, easing = FastOutSlowInEasing)) { it },
            exit = fadeOut(tween(130)) + slideOutVertically(tween(300)) { it },
        ) {
            openPanel?.let { panel ->
                HallPanelSheet(
                    panel = panel,
                    onDismiss = { openPanel = null }
                )
            }
        }

        AnimatedVisibility(
            visible = toastMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 82.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
        ) {
            Surface(
                color = Color(0xFF2B2631).copy(alpha = .94f),
                contentColor = Mist,
                shape = RoundedCornerShape(100.dp),
                tonalElevation = 6.dp,
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2_400)
            toastMessage = null
        }
    }
}

@Composable
private fun HallTopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "第七夜 · 月蚀将至",
                color = Mist.copy(alpha = .62f),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 1.4.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "界隙书房",
                color = Mist,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-.4).sp,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = Color.White.copy(alpha = .06f),
                shape = RoundedCornerShape(100.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = .1f)),
            ) {
                Text(
                    text = "02:14:36",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    color = Amber.copy(alpha = .92f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            TextButton(onClick = onMenuClick) {
                Text(
                    text = "≡",
                    color = Mist.copy(alpha = .8f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
private fun RoomScene(isEntering: Boolean) {
    val infinite = rememberInfiniteTransition(label = "room-breath")
    val breath by infinite.animateFloat(
        initialValue = .96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(2_800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "room-breath-scale",
    )
    val glow by infinite.animateFloat(
        initialValue = .35f,
        targetValue = .72f,
        animationSpec = infiniteRepeatable(tween(1_900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
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
            Text(
                text = "“",
                color = Amber.copy(alpha = .65f),
                fontSize = 46.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "我还记得\n她的名字",
                color = Mist,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                lineHeight = 36.sp,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "记忆避难所 · 正在呼吸",
                color = Mist.copy(alpha = .45f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun Whisper(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(154.dp)
            .alpha(.78f),
    ) {
        Text(
            text = "余响 / 03",
            color = Amber.copy(alpha = .82f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.2.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "“潮汐已经退去，\n但门还没有关。”",
            color = Mist.copy(alpha = .7f),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun StoryGate(
    modifier: Modifier = Modifier,
    isEntering: Boolean,
    onEnter: () -> Unit,
) {
    val infinite = rememberInfiniteTransition(label = "gate")
    val drift by infinite.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(2_300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "gate-drift",
    )

    Column(
        modifier = modifier
            .offset(y = drift.dp)
            .width(166.dp)
            .clip(RoundedCornerShape(26.dp, 8.dp, 26.dp, 8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4C3349).copy(alpha = .82f),
                        Color(0xFF161923).copy(alpha = .94f),
                    )
                )
            )
            .border(1.dp, Amber.copy(alpha = .3f), RoundedCornerShape(26.dp, 8.dp, 26.dp, 8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onEnter() },
                    onLongPress = { onEnter() },
                )
            }
            .padding(16.dp),
    ) {
        Text(
            text = if (isEntering) "门正在回应" else "雾中的无名门",
            color = Mist,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isEntering) "不要回头" else "第三幕 · 未完成",
            color = Amber.copy(alpha = .82f),
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "有人在城墙外叫你的名字",
            color = Mist.copy(alpha = .55f),
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun OrbitButton(
    label: String,
    detail: String,
    modifier: Modifier,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .width(92.dp)
            .clip(RoundedCornerShape(24.dp, 10.dp, 24.dp, 10.dp))
            .background(Color.White.copy(alpha = .045f))
            .border(1.dp, tint.copy(alpha = .25f), RoundedCornerShape(24.dp, 10.dp, 24.dp, 10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = .13f))
                .border(1.dp, tint.copy(alpha = .65f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "·",
                color = tint,
                fontSize = 24.sp,
                lineHeight = 20.sp,
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            color = Mist,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = detail,
            color = Mist.copy(alpha = .47f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
        )
    }
}

@Composable
private fun BottomPrompt(onEnter: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "准备好了吗？",
                color = Mist.copy(alpha = .46f),
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "触碰无名门，继续前行",
                color = Mist.copy(alpha = .78f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OutlinedButton(
            onClick = onEnter,
            shape = RoundedCornerShape(100.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Mist),
            border = androidx.compose.foundation.BorderStroke(1.dp, Mist.copy(alpha = .22f)),
        ) {
            Text(text = "长按进入")
        }
    }
}

private enum class HallPanel(val title: String, val eyebrow: String) {
    Echoes("未熄之言", "记忆回声"),
    Bag("带入下一幕", "行囊"),
    Vow("仍在燃烧", "誓言"),
}

@Composable
private fun HallPanelSheet(
    panel: HallPanel,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        color = Color(0xFF211D27).copy(alpha = .98f),
        contentColor = Mist,
        shape = RoundedCornerShape(32.dp, 32.dp, 18.dp, 18.dp),
        tonalElevation = 10.dp,
        shadowElevation = 18.dp,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = panel.eyebrow,
                        color = Amber,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.2.sp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = panel.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Light,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("收起", color = Mist.copy(alpha = .65f))
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            when (panel) {
                HallPanel.Echoes -> EchoContent()
                HallPanel.Bag -> BagContent()
                HallPanel.Vow -> VowContent()
            }
        }
    }
}

@Composable
private fun EchoContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EchoLine("她把戒指放在窗台，没有带走。", "月蚀前 · 2 小时")
        EchoLine("你没有回头。", "灰塔 · 已改变")
        EchoLine("城墙外的声音，和你一模一样。", "未解读")
    }
}

@Composable
private fun EchoLine(text: String, meta: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = .035f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, color = Mist.copy(alpha = .82f), style = MaterialTheme.typography.bodyMedium)
        Text(text = meta, color = Mist.copy(alpha = .4f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BagContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "选择一件会被带入下一幕的遗物。",
            color = Mist.copy(alpha = .6f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Relic("旧银戒", "未冷却", Amber)
            Relic("空白页", "可书写", Mist)
            Relic("灰烬瓶", "一次性", Color(0xFFB88BD9))
        }
    }
}

@Composable
private fun Relic(name: String, state: String, tint: Color) {
    Surface(
        modifier = Modifier.weight(1f),
        color = tint.copy(alpha = .08f),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .25f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = name, color = Mist, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = state, color = tint, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun VowContent() {
    Column {
        Text(
            text = "“我会找到她，无论门后是什么。”",
            color = Mist,
            style = MaterialTheme.typography.titleLarge,
            fontStyle = FontStyle.Italic,
            lineHeight = 30.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "这句话仍然拥有重量。它会影响你在第三幕中的选择。",
            color = Mist.copy(alpha = .55f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber.copy(alpha = .88f),
                contentColor = Ink,
            ),
        ) {
            Text("守住这句誓言", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FloatingDust() {
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
                color = if (index % 2 == 0) Amber.copy(alpha = shimmer * .35f) else Mist.copy(alpha = shimmer * .22f),
                radius = (index % 3 + 1).dp.toPx(),
                center = point,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRoomAtmosphere(size: Size) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4C3349).copy(alpha = .36f), Color.Transparent),
            center = Offset(size.width * .72f, size.height * .26f),
            radius = size.maxDimension * .72f,
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF7C552A).copy(alpha = .2f), Color.Transparent),
            center = Offset(size.width * .36f, size.height * .72f),
            radius = size.maxDimension * .6f,
        )
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