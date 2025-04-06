package lab.jhrodriguezi.tictactoe.data.network.model

data class GameModel(
    val id: String? = null,
    val name: String = "",
    val player1: String = "",
    val player2: String? = null,
    val currentPlayer: String? = null,
    val board: List<Int> = List(9) { 0 },
    val winningCombination: List<Int> = emptyList(),
    val winner: String? = null,
    val newGameCount: Int = 0,
    val gameOver: Boolean = false,
    val firstMove: Boolean = true,
    val player1Victories: Int = 0,
    val player2Victories: Int = 0,
    val draws: Int = 0,
    val errorMessage: String? = null
)