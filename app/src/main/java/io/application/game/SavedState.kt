package io.application.game

import kotlinx.serialization.Serializable

/** 存档版本号：字段结构变化时递增，用于后续迁移判断 */
private const val SAVE_VERSION = 1

@Serializable
data class SavedEcho(
    val id: String,
    val text: String,
    val meta: String,
    val isUnread: Boolean,
)

@Serializable
data class SavedGate(
    val isVisited: Boolean,
    val visitedChoiceId: String? = null,
)

@Serializable
data class SavedState(
    val version: Int = SAVE_VERSION,
    val mentalState: String,
    val gate: SavedGate,
    val echoes: List<SavedEcho>,
    val selectedRelicId: String? = null,
    /** 遗物 id → 当前状态文字（如"已消耗"、"已带入"） */
    val relicStates: Map<String, String> = emptyMap(),
    val vowIsKept: Boolean,
)

// ── 序列化：GameState → SavedState ──────────────────────────────────────────

fun GameState.toSaved(): SavedState = SavedState(
    mentalState = mentalState.name,
    gate = SavedGate(isVisited = gate.isVisited, visitedChoiceId = gate.visitedChoiceId),
    echoes = echoes.map { SavedEcho(it.id, it.text, it.meta, it.isUnread) },
    selectedRelicId = selectedRelicId,
    relicStates = relics.associate { it.id to it.state },
    vowIsKept = vow.isKept,
)

// ── 反序列化：SavedState 覆盖 GameState.initial() 中的可变字段 ─────────────

/**
 * 从存档恢复游戏状态。
 * 始终以 [GameState.initial()] 为基础，只覆盖玩家可改变的字段，
 * 固定文案（worldTitle、hallTitle 等）保持初始值，确保后续内容更新无需迁移。
 * 未知版本号时静默回退到初始状态，不崩溃。
 */
fun GameState.restoreFrom(saved: SavedState): GameState {
    if (saved.version != SAVE_VERSION) return this

    // 已保存的余响列表：可能包含初始余响（isUnread 已更新）和剧情动态添加的余响
    val restoredEchoes = saved.echoes.map { Echo(it.id, it.text, it.meta, it.isUnread) }

    return copy(
        mentalState = MentalState.entries.firstOrNull { it.name == saved.mentalState }
            ?: mentalState,
        gate = gate.copy(
            isVisited = saved.gate.isVisited,
            visitedChoiceId = saved.gate.visitedChoiceId,
        ),
        echoes = restoredEchoes,
        selectedRelicId = saved.selectedRelicId,
        relics = relics.map { relic ->
            val savedRelicState = saved.relicStates[relic.id]
            if (savedRelicState != null) relic.copy(state = savedRelicState) else relic
        },
        vow = vow.copy(isKept = saved.vowIsKept),
    )
}
