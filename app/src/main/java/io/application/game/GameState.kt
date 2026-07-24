package io.application.game

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
                text = "“我会找到她，无论门后是什么。”",
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

    is GameAction.MakeChoice -> when (action.choiceId) {
        "enter" -> copy(
            gate = gate.copy(isVisited = true),
            echoes = listOf(
                Echo(
                    id = "gate-enter",
                    text = "你穿过了门。光是冷的，像被清洗过的记忆。",
                    meta = "无名门 · 已穿越",
                    isUnread = true,
                )
            ) + echoes,
        )
        "wait" -> copy(
            mentalState = MentalState.HAUNTED,
            echoes = listOf(
                Echo(
                    id = "gate-wait",
                    text = "你停下来了。门没有关上——它只是继续等着你。",
                    meta = "无名门 · 观望",
                    isUnread = true,
                )
            ) + echoes,
        )
        else -> this
    }
}