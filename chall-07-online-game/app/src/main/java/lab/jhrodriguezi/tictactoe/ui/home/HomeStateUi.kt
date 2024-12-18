package lab.jhrodriguezi.tictactoe.ui.home

import lab.jhrodriguezi.tictactoe.data.network.model.GameModel


data class HomeStateUi(
    val isLoading: Boolean = false,
    val gameRooms: List<GameModel> = emptyList(),
    val errorMessage: String = "",
)