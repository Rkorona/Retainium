package io.application.game

import io.application.game.story.StoryEffect
import io.application.game.story.act3Scene

enum class MentalState {
    STEADY,
    WOUNDED,
    HAUNTED,
}

enum class RelicTone {
    AMBER,
    MIST,
    VIOLET,
}

data class Echo(
    val id: String,
    val text: String,
    val meta: String,
    val isUnread: Boolean = false,
)

data class Relic(
    val id: String,
    val name: String,
    val state: String,
    val tone: RelicTone,
    val defaultState: String = state,
)

data class Vow(
    val text: String,
    val description: String,
    val isKept: Boolean = false,
)

data class StoryGateState(
    val title: String,
    val subtitle: String,
    val description: String,
    val isVisited: Boolean = false,
    /** 记录玩家在此副本中选择了哪条路径，null 表示尚未进入 */
    val visitedChoiceId: String? = null,
)

data class GameState(
    val worldTitle: String,
    val countdown: String,
    val hallTitle: String,
    val memoryPhrase: String,
    val memoryCaption: String,
    val gate: StoryGateState,
    val echoes: List<Echo>,
    val relics: List<Relic>,
    val selectedRelicId: String?,
    val vow: Vow,
    val mentalState: MentalState,
) {
    val unreadEchoCount: Int
        get() = echoes.count { it.isUnread }

    // ── 阶段4：派生展示值，让大厅记住玩家做过什么 ───────────────────────

    /** 中央记忆核心文案：随关键选择变化 */
    val displayMemoryPhrase: String
        get() = when {
            mentalState == MentalState.HAUNTED -> "门后的声音\n和我一模一样"
            gate.visitedChoiceId == "enter" -> "我穿过了门\n光是冷的"
            else -> memoryPhrase
        }

    /** 记忆核心副标题：随关键选择变化 */
    val displayMemoryCaption: String
        get() = when {
            mentalState == MentalState.HAUNTED -> "记忆避难所 · 正在动摇"
            gate.isVisited -> "记忆避难所 · 已改变"
            else -> memoryCaption
        }

    /** 无名门展示状态：反映玩家的副本选择路径 */
    val displayGate: StoryGateState
        get() = when (gate.visitedChoiceId) {
            "enter" -> gate.copy(
                title = "你曾踏过的门",
                subtitle = "第三幕 · 已穿越",
                description = "门后的光是冷的。\n你已经知道了。",
            )
            "wait" -> gate.copy(
                title = "仍在等你的门",
                subtitle = "第三幕 · 你还在外面",
                description = "它没有关上。\n雾还在。",
            )
            else -> gate
        }

    companion object {
        fun initial(): GameState = GameState(
            worldTitle = "第七夜 · 月蚀将至",
            countdown = "02:14:36",
            hallTitle = "界隙书房",
            memoryPhrase = "我还记得\n她的名字",
            memoryCaption = "记忆避难所 · 正在呼吸",
            gate = StoryGateState(
                title = "雾中的无名门",
                subtitle = "第三幕 · 未完成",
                description = "有人在城墙外叫你的名字",
            ),
            echoes = listOf(
                Echo("ring", "她把戒指放在窗台，没有带走。", "月蚀前 · 2 小时", isUnread = true),
                Echo("turning", "你没有回头。", "灰塔 · 已改变", isUnread = true),
                Echo("voice", "城墙外的声音，和你一模一样。", "未解读", isUnread = true),
            ),
            relics = listOf(
                Relic("silver-ring", "旧银戒", "未冷却", RelicTone.AMBER),
                Relic("blank-page", "空白页", "可书写", RelicTone.MIST),
                Relic("ash-vial", "灰烬瓶", "一次性", RelicTone.VIOLET),
            ),
            selectedRelicId = null,
            vow = Vow(
                text = "\u201c我会找到她，无论门后是什么。\u201d",
                description = "这句话仍然拥有重量。它会影响你在第三幕中的选择。",
            ),
            mentalState = MentalState.STEADY,
        )
    }
}

sealed interface GameAction {
    data class SelectRelic(val relicId: String) : GameAction
    data object KeepVow : GameAction
    data class ReadEcho(val echoId: String) : GameAction
    data class MakeChoice(val choiceId: String) : GameAction
}

fun GameState.reduce(action: GameAction): GameState = when (action) {
    is GameAction.SelectRelic -> copy(
        selectedRelicId = action.relicId,
        relics = relics.map { relic ->
            if (relic.id == action.relicId) {
                relic.copy(state = "已带入")
            } else {
                relic.copy(state = relic.defaultState)
            }
        },
    )

    GameAction.KeepVow -> copy(vow = vow.copy(isKept = true))

    is GameAction.ReadEcho -> copy(
        echoes = echoes.map { echo ->
            if (echo.id == action.echoId) echo.copy(isUnread = false) else echo
        },
    )

    // 阶段3：通过 StoryEffect 列表处理副作用，unknown choiceId 静默 no-op
    is GameAction.MakeChoice ->
        act3Scene.choices
            .firstOrNull { it.id == action.choiceId }
            ?.let { choice ->
                choice.effects.fold(this) { state, effect ->
                    when (effect) {
                        is StoryEffect.AddEcho -> state.copy(
                            echoes = listOf(
                                Echo(
                                    id = effect.id,
                                    text = effect.text,
                                    meta = effect.meta,
                                    isUnread = true,
                                )
                            ) + state.echoes,
                        )
                        is StoryEffect.SetMentalState -> state.copy(mentalState = effect.state)
                        is StoryEffect.MarkGateVisited -> state.copy(
                            gate = state.gate.copy(
                                isVisited = true,
                                visitedChoiceId = effect.choiceId,
                            ),
                        )
                        StoryEffect.ConsumeSelectedRelic -> {
                            val relicId = state.selectedRelicId ?: return@fold state
                            state.copy(
                                relics = state.relics.map { r ->
                                    if (r.id == relicId) r.copy(state = "已消耗") else r
                                },
                            )
                        }
                    }
                }
            } ?: this
}
