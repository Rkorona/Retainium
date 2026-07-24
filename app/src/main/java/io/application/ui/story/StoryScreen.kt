package io.application.ui.story

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.application.game.GameAction
import io.application.game.GameState
import io.application.game.story.ParagraphKind
import io.application.game.story.StoryChoice
import io.application.game.story.act3Choices
import io.application.game.story.act3Paragraphs
import io.application.ui.hall.scene.drawRoomAtmosphere
import io.application.ui.theme.Amber
import io.application.ui.theme.Ink
import io.application.ui.theme.Mist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class StoryPhase { Reading, Choosing, Result }

@Composable
fun StoryScreen(
    gameState: GameState,
    onAction: (GameAction) -> Unit,
    onExitStory: () -> Unit,
) {
    var revealedCount by remember { mutableStateOf(1) }
    var phase by remember { mutableStateOf(StoryPhase.Reading) }
    var choiceResult by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val isLastParagraph = revealedCount >= act3Paragraphs.size

    BackHandler { onExitStory() }

    LaunchedEffect(revealedCount) {
        delay(250)
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .drawBehind { drawRoomAtmosphere(size) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            StoryTopBar(
                gateTitle = gameState.gate.title,
                onBack = onExitStory,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
            ) {
                act3Paragraphs.forEachIndexed { index, paragraph ->
                    AnimatedVisibility(
                        visible = index < revealedCount,
                        enter = fadeIn(tween(700)) + slideInVertically(tween(600)) { it / 3 },
                        exit = fadeOut(tween(200)),
                    ) {
                        when (paragraph.kind) {
                            ParagraphKind.Scene -> SceneParagraph(text = paragraph.text)
                            ParagraphKind.Dialogue -> DialogueParagraph(
                                speaker = paragraph.speaker.orEmpty(),
                                text = paragraph.text,
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = phase == StoryPhase.Choosing,
                    enter = fadeIn(tween(800)),
                    exit = fadeOut(tween(300)),
                ) {
                    ChoiceSection(
                        choices = act3Choices,
                        onChoose = { choice ->
                            onAction(GameAction.MakeChoice(choice.id))
                            choiceResult = choice.resultText
                            phase = StoryPhase.Result
                            scope.launch {
                                delay(150)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        },
                    )
                }

                AnimatedVisibility(
                    visible = phase == StoryPhase.Result,
                    enter = fadeIn(tween(900)),
                    exit = fadeOut(tween(200)),
                ) {
                    ResultSection(
                        text = choiceResult,
                        onReturn = onExitStory,
                    )
                }
            }

            AnimatedVisibility(
                visible = phase == StoryPhase.Reading,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200)),
            ) {
                StoryBottomBar(
                    isLast = isLastParagraph,
                    onContinue = {
                        if (isLastParagraph) {
                            phase = StoryPhase.Choosing
                            scope.launch {
                                delay(150)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        } else {
                            revealedCount++
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun StoryTopBar(
    gateTitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "← 书房",
                color = Mist.copy(alpha = .55f),
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 0.6.sp,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "第三幕",
                color = Amber.copy(alpha = .65f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = gateTitle,
                color = Mist.copy(alpha = .82f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    HorizontalDivider(color = Mist.copy(alpha = .07f))
}

@Composable
private fun SceneParagraph(text: String) {
    Text(
        text = text,
        color = Mist.copy(alpha = .76f),
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = 30.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    )
}

@Composable
private fun DialogueParagraph(speaker: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .clip(RoundedCornerShape(4.dp, 18.dp, 18.dp, 4.dp))
            .background(Color.White.copy(alpha = .04f))
            .border(
                width = 1.dp,
                color = Amber.copy(alpha = .22f),
                shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 4.dp),
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = speaker,
            color = Amber.copy(alpha = .72f),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.2.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "\u201c$text\u201d",
            color = Mist.copy(alpha = .88f),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            lineHeight = 30.sp,
        )
    }
}

@Composable
private fun ChoiceSection(
    choices: List<StoryChoice>,
    onChoose: (StoryChoice) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "──  你将如何选择  ──",
            color = Mist.copy(alpha = .36f),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.4.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Center,
        )
        choices.forEach { choice ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChoose(choice) },
                color = Color.White.copy(alpha = .05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Mist.copy(alpha = .16f)),
            ) {
                Text(
                    text = choice.label,
                    color = Mist,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ResultSection(
    text: String,
    onReturn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        HorizontalDivider(color = Mist.copy(alpha = .1f))
        Spacer(Modifier.height(28.dp))
        Text(
            text = text,
            color = Mist.copy(alpha = .7f),
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            lineHeight = 30.sp,
        )
        Spacer(Modifier.height(40.dp))
        Text(
            text = "──  本幕结束  ──",
            color = Amber.copy(alpha = .45f),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.8.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReturn() },
            color = Amber.copy(alpha = .1f),
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(1.dp, Amber.copy(alpha = .32f)),
        ) {
            Text(
                text = "返回书房",
                color = Amber.copy(alpha = .88f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun StoryBottomBar(
    isLast: Boolean,
    onContinue: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Surface(
            modifier = Modifier.clickable(onClick = onContinue),
            color = if (isLast) Amber.copy(alpha = .12f) else Color.Transparent,
            shape = RoundedCornerShape(100.dp),
            border = BorderStroke(
                1.dp,
                if (isLast) Amber.copy(alpha = .4f) else Mist.copy(alpha = .18f),
            ),
        ) {
            Text(
                text = if (isLast) "做出选择" else "继续 →",
                color = if (isLast) Amber else Mist.copy(alpha = .55f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 13.dp),
            )
        }
    }
}
