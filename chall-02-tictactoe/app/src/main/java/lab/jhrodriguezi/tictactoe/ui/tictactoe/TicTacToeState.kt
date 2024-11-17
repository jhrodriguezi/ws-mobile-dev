package lab.jhrodriguezi.tictactoe.ui.tictactoe

data class GameState(
    val board: List<Cell> = List(9) { Cell.Empty },
    val currentPlayer: Player = Player.X,
    val winner: Player? = null,
    val isGameOver: Boolean = false,
    val winningCombination: List<Int> = emptyList()
)

enum class Cell {
    Empty, X, O
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