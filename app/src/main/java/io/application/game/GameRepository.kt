package io.application.game

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "game_save")
private val SAVE_KEY = stringPreferencesKey("saved_state")

private val json = Json { ignoreUnknownKeys = true }

/**
 * 负责游戏存档的读写与清除。
 * - 读取：[savedState] 作为 Flow，首次收到值即为启动时的存档（null 表示全新游戏）。
 * - 写入：[save] 将当前 [GameState] 序列化并持久化。
 * - 清除：[clear] 删除存档键，下次启动将使用初始状态。
 */
class GameRepository(private val context: Context) {

    val savedState: Flow<SavedState?> = context.dataStore.data.map { prefs ->
        prefs[SAVE_KEY]?.let { raw ->
            runCatching { json.decodeFromString<SavedState>(raw) }
                .onFailure { /* 解析失败：存档损坏，静默忽略，使用初始状态 */ }
                .getOrNull()
        }
    }

    suspend fun save(state: GameState) {
        val raw = json.encodeToString(SavedState.serializer(), state.toSaved())
        context.dataStore.edit { prefs -> prefs[SAVE_KEY] = raw }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.remove(SAVE_KEY) }
    }
}
