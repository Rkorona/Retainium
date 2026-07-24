package io.application.model

import androidx.compose.ui.graphics.Color
import io.application.ui.theme.*

// ── 玩家状态 ──────────────────────────────────────────────────
data class PlayerStatus(
    val name: String,
    val title: String,           // 称号
    val level: Int,
    val hp: Int,
    val maxHp: Int,
    val mp: Int,
    val maxMp: Int,
    val currentChapter: String,
    val chapterProgress: Float,  // 0f..1f
    val sigil: String            // 代表符印的 Unicode 符文
)

// ── 副本区域类型 ───────────────────────────────────────────────
enum class DungeonZone(
    val displayName: String,
    val seedColor: Color,
    val glowColor: Color,
    val sceneType: SceneType
) {
    DARK_FOREST("幽暗之林", ZoneForest,  Color(0xFF4A8C3F), SceneType.FOREST),
    ABYSS      ("深渊裂缝", ZoneAbyss,   Color(0xFF7B4FC8), SceneType.ABYSS),
    TEMPLE     ("银月神殿", ZoneTemple,  Color(0xFF8890D8), SceneType.TEMPLE),
    RUINS      ("诅咒遗址", ZoneRuins,   Color(0xFFB06040), SceneType.RUINS),
    GLACIER    ("冰封绝峰", ZoneGlacier, Color(0xFF60B8D8), SceneType.GLACIER),
}

enum class SceneType { FOREST, ABYSS, TEMPLE, RUINS, GLACIER }

// ── 副本条目 ──────────────────────────────────────────────────
data class DungeonEntry(
    val id: String,
    val name: String,
    val subtitle: String,
    val difficulty: Int,         // 1-5
    val zone: DungeonZone,
    val isUnlocked: Boolean,
    val recommendedLevel: Int,
    val estimatedMinutes: Int
)

// ── 示例数据（后续替换为真实数据层）─────────────────────────────
object SampleData {

    val player = PlayerStatus(
        name           = "凌霄",
        title          = "虚空旅人",
        level          = 23,
        hp             = 847,
        maxHp          = 1000,
        mp             = 423,
        maxMp          = 600,
        currentChapter = "第三章：遗忘之地的低语",
        chapterProgress = 0.62f,
        sigil          = "᛭"
    )

    val dungeons = listOf(
        DungeonEntry(
            id                = "dark_forest_1",
            name              = "暮色密林",
            subtitle          = "古老的树语尚未沉默",
            difficulty        = 2,
            zone              = DungeonZone.DARK_FOREST,
            isUnlocked        = true,
            recommendedLevel  = 20,
            estimatedMinutes  = 15
        ),
        DungeonEntry(
            id                = "abyss_rift",
            name              = "裂渊之心",
            subtitle          = "深渊凝视着每一个来者",
            difficulty        = 4,
            zone              = DungeonZone.ABYSS,
            isUnlocked        = true,
            recommendedLevel  = 22,
            estimatedMinutes  = 25
        ),
        DungeonEntry(
            id                = "silver_temple",
            name              = "银月圣所",
            subtitle          = "月神的低语刻于廊柱之间",
            difficulty        = 3,
            zone              = DungeonZone.TEMPLE,
            isUnlocked        = true,
            recommendedLevel  = 21,
            estimatedMinutes  = 20
        ),
        DungeonEntry(
            id                = "cursed_ruins",
            name              = "焚痕废墟",
            subtitle          = "诅咒在灰烬中等待宿主",
            difficulty        = 5,
            zone              = DungeonZone.RUINS,
            isUnlocked        = false,
            recommendedLevel  = 25,
            estimatedMinutes  = 30
        ),
        DungeonEntry(
            id                = "frozen_peak",
            name              = "霜咬之巅",
            subtitle          = "寒风携来消失者的姓名",
            difficulty        = 3,
            zone              = DungeonZone.GLACIER,
            isUnlocked        = false,
            recommendedLevel  = 24,
            estimatedMinutes  = 20
        ),
    )
}
