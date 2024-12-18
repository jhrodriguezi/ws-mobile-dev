package lab.jhrodriguezi.tictactoe.ui.core

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import lab.jhrodriguezi.tictactoe.ui.game.ResponsiveTicTacToeScreen
import lab.jhrodriguezi.tictactoe.ui.home.HomeScreen

@Composable
fun ContentWrapper(navigationController: NavHostController) {
    NavHost(
        navController = navigationController,
        startDestination = Routes.Home
    ) {
        composable<Routes.Home> {
            HomeScreen(
                onGameBoardClick = { gameId, userId ->
                    navigationController.navigate(Routes.GameBoard(gameId, userId))
                },
            )
        }
        composable<Routes.GameBoard> {
            val args = it.toRoute<Routes.GameBoard>()
            ResponsiveTicTacToeScreen(gameId = args.gameId, userId = args.userId, onSalirClick = {
                navigationController.navigate(Routes.Home)
            })
        }
    }
}

sealed class Routes {
    @Serializable
    data object Home : Routes()

    @Serializable
    data class GameBoard(val gameId: String, val userId: String) : Routes()
}
