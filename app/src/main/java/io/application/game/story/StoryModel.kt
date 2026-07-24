package io.application.game.story

import io.application.game.MentalState

enum class ParagraphKind { Scene, Dialogue }

data class StoryParagraph(
    val kind: ParagraphKind,
    val text: String,
    val speaker: String? = null,
)

/** 玩家在选择节点可执行的操作 */
data class StoryChoice(
    val id: String,
    val label: String,
    /** 选择后呈现的叙事段落（替代纯字符串结果文本） */
    val resultParagraphs: List<StoryParagraph>,
    /** 选择会对 GameState 产生的副作用列表 */
    val effects: List<StoryEffect>,
)

/** 一个完整的剧情副本：线性段落 → 选择节点 */
data class StoryScene(
    val id: String,
    val actLabel: String,
    val title: String,
    val paragraphs: List<StoryParagraph>,
    val choices: List<StoryChoice>,
)

/** 选择施加给 GameState 的原子状态变更 */
sealed interface StoryEffect {
    /** 在余响列表前端插入一条新余响 */
    data class AddEcho(val id: String, val text: String, val meta: String) : StoryEffect
    /** 改变玩家的精神状态 */
    data class SetMentalState(val state: MentalState) : StoryEffect
    /** 标记无名门为已穿越，并记录具体选择 */
    data class MarkGateVisited(val choiceId: String) : StoryEffect
    /** 消耗当前选中的遗物（若无选中则静默跳过） */
    data object ConsumeSelectedRelic : StoryEffect
}
