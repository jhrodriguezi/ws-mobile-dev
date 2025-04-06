package lab.jhrodriguezi.tictactoe.ui.game

import lab.jhrodriguezi.tictactoe.data.network.model.GameModel
import kotlin.random.Random

data class TicTacToeStateUi(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player? = null,
    val winner: Player? = null,
    val gameOver: Boolean = false,
    val firstMove: Boolean = true,
    val winningCombination: List<Int> = emptyList(),
    val victories: Int = 0,
    val defeats: Int = 0,
    val ties: Int = 0,
)

data class GameRoom(
    val id: String? = null,
    val name: String = "",
    val player1: String = "",
    val player2: String? = null,
    val currentPlayerId: String? = null,
    val gameState: TicTacToeStateUi = TicTacToeStateUi(),
    val goHome: Boolean = false,
)

fun GameRoom.resetGame(): GameRoom {
    return copy(
        currentPlayerId = if(getRandomPlayer() == Player.X) player1 else player2!!,
        gameState = TicTacToeStateUi(
            board = List(9) { Cell.Empty },
            currentPlayer = if(currentPlayerId == player1) Player.X else Player.O,
            firstMove = true,
            victories = gameState.victories,
            defeats = gameState.defeats,
            ties = gameState.ties,
        )
    )
}

fun GameRoom.toGameModel(): GameModel {
    return GameModel(
        id = this.id,
        name = this.name,
        player1 = this.player1,
        player2 = this.player2,
        board = this.gameState.board.map { it.ordinal },
        currentPlayer = currentPlayerId,
        winner = when(this.gameState.winner) {
            null -> null
            Player.X -> player1
            Player.O -> player2
            else -> null
        },
        gameOver = this.gameState.gameOver,
        firstMove = this.gameState.firstMove,
        winningCombination = this.gameState.winningCombination,
        player1Victories = this.gameState.victories,
        player2Victories = this.gameState.defeats,
        draws = this.gameState.ties,
    )
}

fun GameModel.toGameRoom(): GameRoom {
    return GameRoom(
        id = this.id,
        name = this.name,
        player1 = this.player1,
        player2 = this.player2,
        currentPlayerId = this.currentPlayer,
        gameState = TicTacToeStateUi(
            board = this.board.map { Cell.entries[it] },
            currentPlayer = when (this.currentPlayer) {
                null -> null
                this.player1 -> Player.X
                this.player2 -> Player.O
                else -> null
            },
            winner = when(this.winner) {
                null -> null
                this.player1 -> Player.X
                this.player2 -> Player.O
                else -> null
            },
            gameOver = this.gameOver,
            firstMove = this.firstMove,
            winningCombination = this.winningCombination,
            victories = this.player1Victories,
            defeats = this.player2Victories,
            ties = this.draws,
        )
    )
}

enum class Cell {
    Empty, X, O
}

fun getRandomPlayer(): Player {
    return if (Random.nextBoolean()) Player.X else Player.O
}

enum class Player {
    X, O, Empty;

    fun toCell(): Cell = when (this) {
        X -> Cell.X
        O -> Cell.O
        Empty -> Cell.Empty
    }

    fun opposite(): Player = when (this) {
        X -> O
        O -> X
        Empty -> Empty
    }
}