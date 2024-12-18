package lab.jhrodriguezi.tictactoe.ui.game

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.jhrodriguezi.tictactoe.R
import lab.jhrodriguezi.tictactoe.ui.core.components.AdaptiveLayout


@Composable
fun ResponsiveTicTacToeScreen(
    viewModel: TicTacToeOnlineViewModel = hiltViewModel(),
    gameId: String,
    userId: String,
    onSalirClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.setUserId(userId)
        viewModel.loadGameState(gameId)
    }

    BackHandler(enabled = true) { }

    AdaptiveLayout { isLandscape ->
        if (isLandscape) {
            TicTacToeScreenLandscape(
                viewModel = viewModel,
                modifier = Modifier.safeDrawingPadding(),
                onSalirClick = onSalirClick
            )
        } else {
            TicTacToeScreenPortrait(
                viewModel = viewModel,
                modifier = Modifier.safeDrawingPadding(),
                onSalirClick = onSalirClick
            )
        }
    }
}

@Composable
fun TicTacToeScreenLandscape(
    modifier: Modifier = Modifier,
    viewModel: TicTacToeOnlineViewModel,
    onSalirClick: () -> Unit
) {
    val roomState by viewModel.gameState.collectAsState()
    var control by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(roomState) {
        if (roomState.id == null) {
            onSalirClick()
        }
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

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            GameBoard(roomState.gameState, onCellClick = { viewModel.makeMove(it) })
        }
        Spacer(modifier = Modifier.size(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Acerca de") },
                            onClick = { showAboutDialog = true; expanded = false },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) }
                        )
                    }
                }
                GameStatus(roomState.gameState)
                GameScore(
                    participants = listOf(
                        Participant("Tú", roomState.gameState.victories),
                        Participant("Contrincante", roomState.gameState.defeats),
                        Participant("Empates", roomState.gameState.ties)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        NewGameButton(onClick = {
                            viewModel.createNewGame()
                            control = true
                        })
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Acerca de"
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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
                            shape = RoundedCornerShape(16.dp),
                            onClick = {
                                viewModel.closeGame()
                            }) {
                            Text("Salir")
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun TicTacToeScreenPortrait(
    modifier: Modifier = Modifier,
    viewModel: TicTacToeOnlineViewModel,
    onSalirClick: () -> Unit
) {
    val roomState by viewModel.gameState.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(roomState) {
        if (roomState.goHome) {
            onSalirClick()
        }
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
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Acerca de") },
                    onClick = { showAboutDialog = true; expanded = false },
                    leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) }
                )
            }
        }
        GameStatus(roomState.gameState)
        GameScore(
            participants = listOf(
                Participant("X", roomState.gameState.victories),
                Participant("O", roomState.gameState.defeats),
                Participant("Empates", roomState.gameState.ties)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        GameBoard(roomState.gameState, onCellClick = {
            viewModel.makeMove(it)
        })
        Column(modifier = Modifier.fillMaxWidth()) {
            if (roomState.player1 == viewModel.user) {
                NewGameButton(onClick = {
                    viewModel.createNewGame()
                })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
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
                    shape = RoundedCornerShape(16.dp), onClick = {
                        viewModel.closeGame()
                        onSalirClick()
                    }) {
                    Text("Salir")
                }
            }
        }
    }
}

@Composable
private fun GameStatus(gameState: TicTacToeStateUi) {
    val status = when {
        gameState.winner == Player.X -> "¡Ganó X!"
        gameState.winner == Player.O -> "¡Ganó O!"
        gameState.gameOver -> "¡Empate!"
        gameState.currentPlayer == Player.X -> "Turno de X"
        gameState.currentPlayer == Player.O -> "Turno de O"
        else -> "Esperando..."
    }

    Text(
        text = status,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun GameBoard(
    gameState: TicTacToeStateUi,
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
    val xBitmap = ImageBitmap.imageResource(R.drawable.x)
    val oBitmap = ImageBitmap.imageResource(R.drawable.o)
    val context = LocalContext.current

    val x: MediaPlayer by remember { mutableStateOf(MediaPlayer.create(context, R.raw.tttcross))}
    val o: MediaPlayer by remember { mutableStateOf(MediaPlayer.create(context, R.raw.tttcircle))}
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
            .clickable(enabled = cell == Cell.Empty) {
                when (cell) {
                    Cell.X -> x.start()
                    Cell.O -> o.start()
                    Cell.Empty -> {}
                }
                onClick()
            }
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (cell) {
            Cell.X -> Image(xBitmap, contentDescription = "X")

            Cell.O -> Image(oBitmap, contentDescription = "O")

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
