package lab.jhrodriguezi.tictactoe.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lab.jhrodriguezi.tictactoe.data.network.FirebaseService
import javax.inject.Inject

@HiltViewModel
class TicTacToeOnlineViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameRoom())
    val gameState: StateFlow<GameRoom> = _gameState.asStateFlow()

    private var _userId: String? = null
    val user get() = _userId

    private val winningCombinations = listOf(
        // Rows
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        // Columns
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        // Diagonals
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun loadGameState(gameId: String) {
        viewModelScope.launch {
            firebaseService.getGameRoom(gameId).flowOn(Dispatchers.IO).collect { gameModel ->
                gameModel?.let {
                    _gameState.update { gameModel.toGameRoom() }
                }
            }
            firebaseService.observeGameRoomDeletion(gameId, {
                _gameState.update { GameRoom(goHome = true) }
            })
        }
    }

    fun setUserId(id: String) {
        _userId = id
    }

    fun createNewGame() {
        _gameState.update {
            it.resetGame()
        }

        firebaseService.updateGameRoom(_gameState.value.toGameModel())
    }

    fun closeGame() {
        viewModelScope.launch {
            if (_gameState.value.player2 == _userId) {

                _userId?.let {
                    _gameState.value.copy(player2 = null).toGameModel()
                }?.let { firebaseService.updateGameRoom(it) }
                return@launch
            }
            _gameState.value.id?.let { gameId ->
                firebaseService.deleteGameRoom(gameId)
            }
        }
    }

    fun makeMove(position: Int) {
        if (
            _gameState.value.currentPlayerId != _userId || // Not user's turn
            _gameState.value.gameState.currentPlayer == null ||  // Game not started
            _gameState.value.gameState.winner != null || // Game is over
            position !in 0..8 ||  // Invalid position
            _gameState.value.gameState.board[position] != Cell.Empty || // Position already taken
            _gameState.value.gameState.gameOver // Game is over
        ) {
            return
        }

        val currentState = _gameState.value

        // Update board with player's move
        val newBoard = currentState.gameState.board.toMutableList()
        newBoard[position] = currentState.gameState.currentPlayer!!.toCell()

        val updatedState = checkGameState(
            currentState.copy(
                gameState = currentState.gameState.copy(board = newBoard, firstMove = false),
                currentPlayerId = when (currentState.currentPlayerId) {
                    currentState.player1 -> currentState.player2
                    currentState.player2 -> currentState.player1
                    else -> null
                }
            )
        )

        _gameState.update {
            viewModelScope.launch {
                firebaseService.updateGameRoom(updatedState.toGameModel())
            }
            updatedState
        }
    }

    private fun checkGameState(state: GameRoom): GameRoom {
        val winner = checkWinner(state.gameState.board, state.gameState.currentPlayer!!.toCell())
        val gameState = state.gameState
        if (winner != null) {
            return when (state.gameState.currentPlayer) {
                Player.X -> state.copy(
                    gameState = gameState.copy(
                        winner = gameState.currentPlayer,
                        gameOver = true,
                        winningCombination = winner,
                        victories = gameState.victories + 1,
                        currentPlayer = gameState.currentPlayer!!.opposite()
                    )
                )

                Player.O -> state.copy(
                    gameState = gameState.copy(
                        winner = gameState.currentPlayer,
                        gameOver = true,
                        winningCombination = winner,
                        defeats = gameState.defeats + 1,
                        currentPlayer = gameState.currentPlayer!!.opposite()
                    )
                )
                Player.Empty -> TODO()
                null -> TODO()
            }
        }
        if (state.gameState.board.none { it == Cell.Empty }) {
            return state.copy(
                gameState = state.gameState.copy(gameOver = true, ties = state.gameState.ties + 1)
            )
        }

        return state
    }

    private fun checkWinner(board: List<Cell>, cell: Cell): List<Int>? {
        winningCombinations.forEach { combination ->
            if (combination.all { board[it] == cell }) {
                return combination
            }
        }
        return null
    }
}

/**

class TicTacToeOfflineViewModel(context: Context) : ViewModel() {
private val preferencesManager = PreferencesManager(context)
private val currentPreferences = preferencesManager.observeAppPreferences()
.onStart {
loadGameState()
}
.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), AppPreferences())
private val DELAY_COMPUTER_MOVE = 1500L

private val _gameState = MutableStateFlow(GameRoom())
val gameState: StateFlow<GameRoom> = _gameState.asStateFlow()

private val _difficultyLevel = MutableStateFlow(DifficultyLevel.Expert)
val difficultyLevel: StateFlow<DifficultyLevel> = _difficultyLevel.asStateFlow()

private val x: MediaPlayer = MediaPlayer.create(context, R.raw.tttcross)
private val o: MediaPlayer = MediaPlayer.create(context, R.raw.tttcircle)

private val winningCombinations = listOf(
// Rows
listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
// Columns
listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
// Diagonals
listOf(0, 4, 8), listOf(2, 4, 6)
)

fun loadGameState() {
viewModelScope.launch {
currentPreferences.collect { preferences ->
_gameState.value = GameStateOffline(
board = preferences.board,
currentPlayer = preferences.currentPlayer,
winner = preferences.winner,
gameOver = preferences.gameOver,
winningCombination = preferences.winningCombination,
firstMove = preferences.firstMove,
victories = preferences.victories,
defeats = preferences.defeats,
ties = preferences.ties
)
_difficultyLevel.value = when (preferences.difficultyLevel) {
0 -> DifficultyLevel.Easy
1 -> DifficultyLevel.Harder
else -> DifficultyLevel.Expert
}
}
}
}

override fun onCleared() {
this.savePreferences()
super.onCleared()
}

fun setDifficultyLevel(level: DifficultyLevel) {
_difficultyLevel.value = level
savePreferences()
}

fun makeMove(position: Int) {
if (_gameState.value.currentPlayer == Player.X) x.start()
_gameState.update { gameState ->
gameState.copy(firstMove = false)
}
val currentState = _gameState.value
if (position !in 0..8 || currentState.board[position] != Cell.Empty || currentState.gameOver) {
return
}

// Update board with player's move
val newBoard = currentState.board.toMutableList()
newBoard[position] = currentState.currentPlayer.toCell()

val updatedState = checkGameState(
currentState.copy(
board = newBoard
)
)

_gameState.update {
updatedState
}
}

fun makeComputerMove() {
_gameState.update { gameState ->
gameState.copy(firstMove = false)
}
when (_difficultyLevel.value) {
DifficultyLevel.Easy -> {
makeRandomMove()
}

DifficultyLevel.Harder -> {
// Try to win first
val winningMove = findWinningMove(Player.O)
if (winningMove != null) {
makeMove(winningMove)
return
}
// If can't win, make random move
makeRandomMove()
}

DifficultyLevel.Expert -> {
// Try to win
val winningMove = findWinningMove(Player.O)
if (winningMove != null) {
makeMove(winningMove)
return
}

// Try to block player
val blockingMove = findWinningMove(Player.X)
if (blockingMove != null) {
makeMove(blockingMove)
return
}

// If no winning or blocking move, make strategic move
makeStrategicMove()
}
}
}

private fun makeRandomMove() {
val emptyCells = _gameState.value.board.mapIndexedNotNull { index, cell ->
if (cell == Cell.Empty) index else null
}
if (emptyCells.isNotEmpty()) {
makeMove(emptyCells.random(Random))
}
}

private fun makeStrategicMove() {
val currentBoard = _gameState.value.board

// Priority 1: Take center if available
if (currentBoard[4] == Cell.Empty) {
makeMove(4)
return
}

// Priority 2: Take corners if available
val corners = listOf(0, 2, 6, 8)
val emptyCorners = corners.filter { currentBoard[it] == Cell.Empty }
if (emptyCorners.isNotEmpty()) {
makeMove(emptyCorners.random())
return
}

// Priority 3: Take any available side
val sides = listOf(1, 3, 5, 7)
val emptySides = sides.filter { currentBoard[it] == Cell.Empty }
if (emptySides.isNotEmpty()) {
makeMove(emptySides.random())
return
}

// If somehow no move was made (shouldn't happen), make random move
makeRandomMove()
}

private fun findWinningMove(player: Player): Int? {
val currentBoard = _gameState.value.board

// Try each empty cell
currentBoard.indices.forEach { pos ->
if (currentBoard[pos] == Cell.Empty) {
val testBoard = currentBoard.toMutableList()
testBoard[pos] = player.toCell()

// Check if this move would win
if (checkWinner(testBoard, player.toCell()) != null) {
return pos
}
}
}
return null
}

private fun checkGameState(state: GameStateOffline): GameStateOffline {
val winner = checkWinner(state.board, state.currentPlayer.toCell())
if (winner != null) {
return when (state.currentPlayer) {
Player.O -> state.copy(
winner = state.currentPlayer,
gameOver = true,
winningCombination = winner,
victories = state.victories + 1,
currentPlayer = state.currentPlayer.opposite()
)

Player.X -> state.copy(
winner = state.currentPlayer,
gameOver = true,
winningCombination = winner,
defeats = state.defeats + 1,
currentPlayer = state.currentPlayer.opposite()
)
}
}
if (state.board.none { it == Cell.Empty }) {
return state.copy(gameOver = true, ties = state.ties + 1)
}

return state
}

private fun checkWinner(board: List<Cell>, cell: Cell): List<Int>? {
winningCombinations.forEach { combination ->
if (combination.all { board[it] == cell }) {
return combination
}
}
return null
}

fun resetGame() {
_gameState.value = GameStateOffline(
victories = _gameState.value.victories,
defeats = _gameState.value.defeats,
ties = _gameState.value.ties
)
}

fun resetGameAndScores() {
_gameState.value = GameStateOffline()
}

fun savePreferences() {
val preferencesToSave = currentPreferences.value.copy(
board = _gameState.value.board,
currentPlayer = _gameState.value.currentPlayer,
winner = _gameState.value.winner,
gameOver = _gameState.value.gameOver,
winningCombination = _gameState.value.winningCombination,
firstMove = _gameState.value.firstMove,
victories = _gameState.value.victories,
defeats = _gameState.value.defeats,
ties = _gameState.value.ties,
difficultyLevel = _difficultyLevel.value.ordinal
)
viewModelScope.launch {
preferencesManager.saveGameState(preferencesToSave)
}
}

companion object {
fun factory(context: Context) = object : ViewModelProvider.Factory {
@Suppress("UNCHECKED_CAST")
override fun <T : ViewModel> create(
modelClass: Class<T>, extras: CreationExtras
): T {
return TicTacToeViewModel(context) as T
}
}
}
}*/