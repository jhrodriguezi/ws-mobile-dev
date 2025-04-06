package lab.jhrodriguezi.tictactoe.util

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import lab.jhrodriguezi.tictactoe.ui.game.Cell
import lab.jhrodriguezi.tictactoe.ui.game.Player
import lab.jhrodriguezi.tictactoe.ui.game.getRandomPlayer

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class PreferencesManager(private val context: Context) {
    companion object {
        val BOARD_KEY = stringPreferencesKey("board")
        val CURRENT_PLAYER_KEY = stringPreferencesKey("current_player")
        val WINNER_KEY = stringPreferencesKey("winner")
        val IS_GAME_OVER_KEY = booleanPreferencesKey("is_game_over")
        val WINNING_COMBINATION_KEY = stringPreferencesKey("winning_combination")
        val IS_FIRST_MOVE_KEY = booleanPreferencesKey("is_first_move")
        val VICTORIES_KEY = floatPreferencesKey("victories")
        val DEFEATS_KEY = floatPreferencesKey("defeats")
        val TIES_KEY = floatPreferencesKey("ties")
        val DIFFICULTY_LEVEL_KEY = floatPreferencesKey("difficulty_level")
    }

    // Save a boolean preference
    suspend fun saveBooleanPreference(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // Save a string preference
    suspend fun saveStringPreference(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // Save a float preference
    suspend fun saveFloatPreference(key: Preferences.Key<Float>, value: Float) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // Get a boolean preference
    fun getBooleanPreference(key: Preferences.Key<Boolean>, defaultValue: Boolean): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    // Get a string preference
    fun getStringPreference(key: Preferences.Key<String>, defaultValue: String): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    // Get a float preference
    fun getFloatPreference(key: Preferences.Key<Float>, defaultValue: Float): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    suspend fun saveGameState(gameState: AppPreferences) {
        context.dataStore.edit { preferences ->
            preferences[BOARD_KEY] = gameState.board.joinToString(",") { it.name }
            preferences[CURRENT_PLAYER_KEY] = gameState.currentPlayer.name
            preferences[WINNER_KEY] = gameState.winner?.name ?: ""
            preferences[IS_GAME_OVER_KEY] = gameState.isGameOver
            preferences[WINNING_COMBINATION_KEY] = gameState.winningCombination.joinToString(",")
            preferences[IS_FIRST_MOVE_KEY] = gameState.isFirstMove
            preferences[VICTORIES_KEY] = gameState.victories.toFloat()
            preferences[DEFEATS_KEY] = gameState.defeats.toFloat()
            preferences[TIES_KEY] = gameState.ties.toFloat()
            preferences[DIFFICULTY_LEVEL_KEY] = gameState.difficultyLevel.toFloat()
            Log.i("PreferencesManager", "Saving game state: $gameState")
        }
    }

    fun observeAppPreferences(): Flow<AppPreferences> {
        return combine(
            getStringPreference(BOARD_KEY, ""),
            getStringPreference(CURRENT_PLAYER_KEY, Player.X.name),
            getStringPreference(WINNER_KEY, ""),
            getBooleanPreference(IS_GAME_OVER_KEY, false),
            getStringPreference(WINNING_COMBINATION_KEY, ""),
            getBooleanPreference(IS_FIRST_MOVE_KEY, true),
            getFloatPreference(VICTORIES_KEY, 0f),
            getFloatPreference(DEFEATS_KEY, 0f),
            getFloatPreference(TIES_KEY, 0f),
            getFloatPreference(DIFFICULTY_LEVEL_KEY, 2f)
        ) { params ->
            val board = params[0] as String
            val currentPlayer = params[1] as String
            val winner = params[2] as String
            val isGameOver = params[3] as Boolean
            val winningCombination = params[4] as String
            val isFirstMove = params[5] as Boolean
            val victories = params[6] as Float
            val defeats = params[7] as Float
            val ties = params[8] as Float
            val difficultyLevel = params[9] as Float
            AppPreferences(
                board = board.takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.map { Cell.valueOf(it) }
                    ?: List(9) { Cell.Empty },
                currentPlayer = Player.valueOf(currentPlayer),
                winner = winner.takeIf { it.isNotEmpty() }?.let { Player.valueOf(it) },
                isGameOver = isGameOver,
                winningCombination = winningCombination.takeIf { it.isNotEmpty() }
                    ?.split(",")
                    ?.map { it.toInt() }
                    ?: emptyList(),
                isFirstMove = isFirstMove,
                victories = victories.toInt(),
                defeats = defeats.toInt(),
                ties = ties.toInt(),
                difficultyLevel = difficultyLevel.toInt()
            )
        }
    }
}


data class AppPreferences(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player = getRandomPlayer(),
    val winner: Player? = null,
    val isGameOver: Boolean = false,
    val winningCombination: List<Int> = emptyList(),
    val isFirstMove: Boolean = true,
    val victories: Int = 0,
    val defeats: Int = 0,
    val ties: Int = 0,
    val difficultyLevel: Int = 0
)