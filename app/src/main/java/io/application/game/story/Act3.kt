package io.application.game.story

enum class ParagraphKind { Scene, Dialogue }

data class StoryParagraph(
    val kind: ParagraphKind,
    val text: String,
    val speaker: String? = null,
)

data class StoryChoice(
    val id: String,
    val label: String,
    val resultText: String,
)

val act3Paragraphs: List<StoryParagraph> = listOf(
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
)

val act3Choices: List<StoryChoice> = listOf(
    StoryChoice(
        id = "enter",
        label = "推开门，走进去",
        resultText = "你推开门。\n\n光是冷的，像被清洗过的记忆。门在你身后合上，没有声音。",
    ),
    StoryChoice(
        id = "wait",
        label = "退后一步，先观察",
        resultText = "你停下来了。\n\n门没有关上——它只是继续等着你，像它等了很久一样。雾渐渐没过了你的脚踝。",
    ),
)
