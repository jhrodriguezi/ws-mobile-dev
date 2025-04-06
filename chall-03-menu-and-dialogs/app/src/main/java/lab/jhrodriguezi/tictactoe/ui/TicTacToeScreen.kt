package lab.jhrodriguezi.tictactoe.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
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
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentDifficulty by viewModel.difficultyLevel.collectAsState()

    LaunchedEffect(gameState.currentPlayer) {
        if (gameState.currentPlayer == Player.O && gameState.isFirstMove)
            viewModel.makeComputerMove()
    }

    // Difficulty Dialog
    if (showDifficultyDialog) {
        AlertDialog(
            onDismissRequest = { showDifficultyDialog = false },
            title = { Text("Seleccionar Dificultad") },
            text = {
                Column {
                    DifficultyLevel.entries.forEach { difficulty ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentDifficulty == difficulty,
                                onClick = {
                                    viewModel.setDifficultyLevel(difficulty)
                                    showDifficultyDialog = false
                                }
                            )
                            Text(
                                text = difficulty.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDifficultyDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Quit Dialog
    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("¿Quieres salir del juego?") },
            text = { Text("Esta acción cerrará la aplicación.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Acerca de") },
            text = {
                Column {
                    Text("Tic Tac Toe", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Desarrollado por Jhonatan Rodríguez",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Versión 1.0.0", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = { showAboutDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Acerca de"
                )
            }
        }
        GameStatus(gameState)
        GameScore(
            participants = listOf(
                Participant("Tú", gameState.victories),
                Participant("Android", gameState.defeats),
                Participant("Empates", gameState.ties)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        GameBoard(gameState, onCellClick = { viewModel.makeMove(it) })
        Column(modifier = Modifier.fillMaxWidth()) {
            NewGameButton(onClick = {
                viewModel.resetGame()
                control = true
            })
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .weight(1f),
                    shape = RoundedCornerShape(16.dp), onClick = { showDifficultyDialog = true }) {
                    Text("Ajustar dificultad")
                }
                Spacer(modifier = Modifier.size(16.dp))
                Button(modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .weight(1f),
                    colors = ButtonColors(
                        contentColor = MaterialTheme.colorScheme.errorContainer,
                        containerColor = MaterialTheme.colorScheme.error,
                        disabledContentColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = RoundedCornerShape(16.dp), onClick = { showQuitDialog = true }) {
                    Text("Salir")
                }
            }
        }
    }
}

enum class DifficultyLevel {
    Easy, Harder, Expert
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

data class Participant(
    val name: String,
    val score: Int
)

// Componente de puntuación individual
@Composable
fun Score(participant: Participant, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = participant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = participant.score.toString(),
                fontSize = 20.sp
            )
        }
    }
}

// Componente de puntuaciones flexible
@Composable
fun GameScore(
    participants: List<Participant>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(participants) { participant ->
            Score(participant = participant)
        }
    }
}