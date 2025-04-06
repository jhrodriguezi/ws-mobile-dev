package lab.jhrodriguezi.tictactoe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TicTacToeScreen(
    modifier: Modifier = Modifier,
    viewModel: TicTacToeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    var control by remember { mutableStateOf(true) }

    LaunchedEffect(gameState.currentPlayer) {
        if (gameState.currentPlayer == Player.O && control)
            viewModel.makeComputerMove()
        else
            control = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        GameStatus(gameState)
        Text("Victorias: " + gameState.victories)
        Text("Derrotas: " + gameState.defeats)
        Text("Empates: " + gameState.ties)
        GameBoard(gameState, onCellClick = { viewModel.makeMove(it) })
        NewGameButton(onClick = {
            viewModel.resetGame()
            control = true
        })
    }
}

@Composable
private fun GameStatus(gameState: GameState) {
    val status = when {
        gameState.winner == Player.X -> "¡Ganaste!"
        gameState.winner == Player.O -> "¡Ganó Android!"
        gameState.isGameOver -> "¡Empate!"
        gameState.currentPlayer == Player.X -> "Tu turno"
        else -> "Turno de Android"
    }

    Text(
        text = status,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
private fun GameBoard(
    gameState: GameState,
    onCellClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 15.dp)
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    GameCell(
                        cell = gameState.board[index],
                        isWinningCell = index in gameState.winningCombination,
                        onClick = { onCellClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCell(
    cell: Cell,
    isWinningCell: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isWinningCell -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = cell == Cell.Empty) { onClick() }
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (cell) {
            Cell.X -> PlayerSymbol(
                text = "X",
                color = MaterialTheme.colorScheme.primary
            )

            Cell.O -> PlayerSymbol(
                text = "O",
                color = MaterialTheme.colorScheme.error
            )

            Cell.Empty -> {}
        }
    }
}

@Composable
private fun PlayerSymbol(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "Nuevo Juego",
            style = MaterialTheme.typography.titleMedium
        )
    }
}