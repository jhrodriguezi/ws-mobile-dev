package lab.jhrodriguezi.tictactoe.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lab.jhrodriguezi.tictactoe.data.network.FirebaseService
import lab.jhrodriguezi.tictactoe.data.network.model.GameModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseService: FirebaseService
) : ViewModel() {
    private var _state = MutableStateFlow(HomeStateUi())
    val state: StateFlow<HomeStateUi> = _state.asStateFlow()

    fun onCreateGameRoom(userId: String, name: String): String {
        val game = GameModel(player1 = userId, name = name)
        val roomId = firebaseService.createGameRoom(game)
        return roomId
    }

    fun onJoinGameRoom(userId: String, roomId: String) {
        viewModelScope.launch {
            firebaseService.joinGameRoom(roomId).collect { game ->
                if (game != null && game.player2 == null) {
                    val updatedGame = game.copy(
                        player2 = userId, currentPlayer = selectCurrentPlayer(game.player1, userId),
                    )
                    firebaseService.updateGameRoom(updatedGame)
                } else {
                    _state.value = HomeStateUi(errorMessage = "Room not found")
                }
            }
        }
    }

    private fun selectCurrentPlayer(player1: String, player2: String): String {
        return if (Random.nextBoolean()) player1 else player2
    }

    fun getGameRooms() {
        viewModelScope.launch {
            firebaseService.getGameRooms().collect { gameRooms ->
                _state.value = HomeStateUi(gameRooms = gameRooms)
            }
        }
    }
}