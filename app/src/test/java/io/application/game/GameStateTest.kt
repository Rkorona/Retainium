package io.application.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {
    @Test
    fun initialState_containsHallPreparationData() {
        val state = GameState.initial()

        assertEquals("界隙书房", state.hallTitle)
        assertEquals(3, state.echoes.size)
        assertEquals(3, state.relics.size)
        assertEquals(3, state.unreadEchoCount)
        assertFalse(state.vow.isKept)
    }

    @Test
    fun selectingRelic_updatesOnlySelectedRelic() {
        val state = GameState.initial()

        val updated = state.reduce(GameAction.SelectRelic("blank-page"))

        assertEquals("blank-page", updated.selectedRelicId)
        assertEquals("已带入", updated.relics.first { it.id == "blank-page" }.state)
        assertEquals("未冷却", updated.relics.first { it.id == "silver-ring" }.state)
    }

    @Test
    fun selectingAnotherRelic_resetsPreviousSelection() {
        val state = GameState.initial()
            .reduce(GameAction.SelectRelic("blank-page"))

        val updated = state.reduce(GameAction.SelectRelic("ash-vial"))

        assertEquals("一次性", updated.relics.first { it.id == "ash-vial" }.defaultState)
        assertEquals("已带入", updated.relics.first { it.id == "ash-vial" }.state)
        assertEquals("可书写", updated.relics.first { it.id == "blank-page" }.state)
    }

    @Test
    fun readingEcho_marksOnlyThatEchoAsRead() {
        val state = GameState.initial()

        val updated = state.reduce(GameAction.ReadEcho("voice"))

        assertEquals(2, updated.unreadEchoCount)
        assertFalse(updated.echoes.first { it.id == "voice" }.isUnread)
        assertTrue(updated.echoes.first { it.id == "ring" }.isUnread)
    }

    @Test
    fun keepingVow_changesVowState() {
        val state = GameState.initial()

        val updated = state.reduce(GameAction.KeepVow)

        assertTrue(updated.vow.isKept)
    }
}