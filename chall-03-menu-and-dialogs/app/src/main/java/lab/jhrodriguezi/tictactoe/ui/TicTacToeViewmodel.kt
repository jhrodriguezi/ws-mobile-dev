package lab.jhrodriguezi.tictactoe.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class TicTacToeViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _difficultyLevel = MutableStateFlow(DifficultyLevel.Expert)
    val difficultyLevel: StateFlow<DifficultyLevel> = _difficultyLevel.asStateFlow()

    private val winningCombinations = listOf(
        // Rows
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        // Columns
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        // Diagonals
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    fun setDifficultyLevel(level: DifficultyLevel) {
        _difficultyLevel.value = level
    }

    fun makeMove(position: Int) {
        _gameState.update { gameState ->
            gameState.copy(isFirstMove = false)
        }
        val currentState = _gameState.value
        if (position !in 0..8 ||
            currentState.board[position] != Cell.Empty ||
            currentState.isGameOver
        ) {
            return
        }

        // Update board with player's move
        val newBoard = currentState.board.toMutableList()
        newBoard[position] = currentState.currentPlayer.toCell()

        // Check for winner or updates
        val updatedState = checkGameState(
            currentState.copy(
                board = newBoard,
                currentPlayer = currentState.currentPlayer.opposite()
            )
        )

        _gameState.value = updatedState

        // If game isn't over and it's computer's turn (O), make computer move
        if (!updatedState.isGameOver && updatedState.currentPlayer == Player.O) {
            makeComputerMove()
        }
    }

    fun makeComputerMove() {
        _gameState.update { gameState ->
            gameState.copy(isFirstMove = false)
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

    private fun checkGameState(state: GameState): GameState {
        val winner = checkWinner(state.board, state.currentPlayer.opposite().toCell())
        if (winner != null) {
            return when (state.currentPlayer) {
                Player.O -> state.copy(
                    winner = state.currentPlayer.opposite(),
                    isGameOver = true,
                    winningCombination = winner,
                    victories = state.victories + 1
                )
                Player.X -> state.copy(
                    winner = state.currentPlayer.opposite(),
                    isGameOver = true,
                    winningCombination = winner,
                    defeats = state.defeats + 1
                )
            }
        }

        // Check for tie
        if (state.board.none { it == Cell.Empty }) {
            return state.copy(isGameOver = true, ties = state.ties + 1)
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
        _gameState.value = GameState(
            victories = _gameState.value.victories,
            defeats = _gameState.value.defeats,
            ties = _gameState.value.ties
        )
    }
}