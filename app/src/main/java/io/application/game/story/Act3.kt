package io.application.game.story

import io.application.game.MentalState

val act3Scene = StoryScene(
    id = "act3",
    actLabel = "第三幕",
    title = "无名门",
    paragraphs = listOf(
        StoryParagraph(
            ParagraphKind.Scene,
            "雾比你记忆中的厚。城墙在身后合拢，来时的路已经消失不见。前方只有一扇门——没有匾额，没有门神，黑色的边框像一道没有底的裂口，从缝隙里渗出琥珀色的微光。",
        ),
        StoryParagraph(
            ParagraphKind.Scene,
            "然后你听见了那个声音。",
        ),
        StoryParagraph(
            ParagraphKind.Dialogue,
            "你来了。",
            "门后",
        ),
        StoryParagraph(
            ParagraphKind.Scene,
            "声音和你的一模一样。",
        ),
        StoryParagraph(
            ParagraphKind.Scene,
            "你握紧了带来的东西。它的重量提醒你：你仍然是你自己。",
        ),
        StoryParagraph(
            ParagraphKind.Dialogue,
            "我一直在等。你知道等待是什么感觉——比你想象的要长。",
            "门后",
        ),
        StoryParagraph(
            ParagraphKind.Scene,
            "门缝里的光开始变化，从琥珀渐渐变成苍白，像一只深呼吸之前睁开的眼睛。",
        ),
        StoryParagraph(
            ParagraphKind.Dialogue,
            "进来。\n……还是，你害怕了？",
            "门后",
        ),
        StoryParagraph(
            ParagraphKind.Scene,
            "雾向你靠拢。你还有时间做出选择。",
        ),
    ),
    choices = listOf(
        StoryChoice(
            id = "enter",
            label = "推开门，走进去",
            resultParagraphs = listOf(
                StoryParagraph(
                    ParagraphKind.Scene,
                    "你推开门。",
                ),
                StoryParagraph(
                    ParagraphKind.Dialogue,
                    "光是冷的。\n比你记忆中的任何一盏灯都要冷得多。",
                    "门后的声音",
                ),
                StoryParagraph(
                    ParagraphKind.Scene,
                    "门在你身后合上，没有声音。你回头——只有一面和书房一模一样的墙壁。",
                ),
                StoryParagraph(
                    ParagraphKind.Scene,
                    "你带来的东西，不在了。",
                ),
            ),
            effects = listOf(
                StoryEffect.MarkGateVisited("enter"),
                StoryEffect.AddEcho(
                    id = "gate-enter",
                    text = "你穿过了门。光是冷的，像被清洗过的记忆。",
                    meta = "无名门 · 已穿越",
                ),
                StoryEffect.ConsumeSelectedRelic,
            ),
        ),
        StoryChoice(
            id = "wait",
            label = "退后一步，先观察",
            resultParagraphs = listOf(
                StoryParagraph(
                    ParagraphKind.Scene,
                    "你停下来了。",
                ),
                StoryParagraph(
                    ParagraphKind.Dialogue,
                    "……我会等。",
                    "门后的声音",
                ),
                StoryParagraph(
                    ParagraphKind.Scene,
                    "雾没过了你的脚踝，又没过了膝盖。门没有关上——它只是继续等着你，像它等了很久一样。",
                ),
                StoryParagraph(
                    ParagraphKind.Scene,
                    "回到书房的路，已经模糊。",
                ),
            ),
            effects = listOf(
                StoryEffect.MarkGateVisited("wait"),
                StoryEffect.SetMentalState(MentalState.HAUNTED),
                StoryEffect.AddEcho(
                    id = "gate-wait",
                    text = "你停下来了。门没有关上——它只是继续等着你。",
                    meta = "无名门 · 观望",
                ),
            ),
        ),
    ),
)
