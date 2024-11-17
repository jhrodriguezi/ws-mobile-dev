package lab.jhrodriguezi.tictactoe.ui.tictactoe

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class TicTacToeViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

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

    fun makeMove(position: Int) {
        val currentState = _gameState.value
        if (position !in 0..8 ||
            currentState.board[position] != Cell.Empty ||
            currentState.isGameOver) {
            return
        }

        // Update board with player's move
        val newBoard = currentState.board.toMutableList()
        newBoard[position] = currentState.currentPlayer.toCell()

        // Check for winner or updates
        val updatedState = checkGameState(currentState.copy(
            board = newBoard,
            currentPlayer = currentState.currentPlayer.opposite()
        ))

        _gameState.value = updatedState

        // If game isn't over and it's computer's turn (O), make computer move
        if (!updatedState.isGameOver && updatedState.currentPlayer == Player.O) {
            makeComputerMove()
        }
    }

    private fun makeComputerMove() {
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

        // Make random move
        val emptyCells = _gameState.value.board.mapIndexedNotNull { index, cell ->
            if (cell == Cell.Empty) index else null
        }
        if (emptyCells.isNotEmpty()) {
            makeMove(emptyCells.random(Random))
        }
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
            return state.copy(
                winner = state.currentPlayer.opposite(),
                isGameOver = true,
                winningCombination = winner
            )
        }

        // Check for tie
        if (state.board.none { it == Cell.Empty }) {
            return state.copy(isGameOver = true)
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
        _gameState.value = GameState()
    }
}