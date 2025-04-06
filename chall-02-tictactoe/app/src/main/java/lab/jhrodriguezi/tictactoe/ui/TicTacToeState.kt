package lab.jhrodriguezi.tictactoe.ui

import kotlin.random.Random

data class GameState(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player = getRandomPlayer(),
    val winner: Player? = null,
    val isGameOver: Boolean = false,
    val winningCombination: List<Int> = emptyList(),
    val victories: Int = 0,
    val defeats: Int = 0,
    val ties: Int = 0,
)

enum class Cell {
    Empty, X, O
}

fun getRandomPlayer(): Player {
    return if (Random.nextBoolean()) Player.X else Player.O
}

enum class Player {
    X, O;

    fun toCell(): Cell = when (this) {
        X -> Cell.X
        O -> Cell.O
    }

    fun opposite(): Player = when (this) {
        X -> O
        O -> X
    }
}