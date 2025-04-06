package lab.jhrodriguezi.tictactoe.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import lab.jhrodriguezi.tictactoe.R
import java.util.Calendar

@Composable
fun HomeScreen(
    onGameBoardClick: (String, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var componentToBeRendered by rememberSaveable { mutableStateOf(HomeUiComponent.Choice) }
    val userId by rememberSaveable {
        mutableStateOf(
            Calendar.getInstance().timeInMillis.hashCode().toString()
        )
    }
    var userName by rememberSaveable { mutableStateOf("") }
    var roomSelected by remember { mutableStateOf("") }
    val homeState by viewModel.state.collectAsState()

    BackHandler(enabled = true) {
        when (componentToBeRendered) {
            HomeUiComponent.UserNameForm -> componentToBeRendered = HomeUiComponent.Choice
            HomeUiComponent.RoomsList -> componentToBeRendered = HomeUiComponent.UserNameForm
            HomeUiComponent.CreateRoomForm -> componentToBeRendered = HomeUiComponent.RoomsList
            else -> Unit
        }
    }

    LaunchedEffect(roomSelected) {
        if (roomSelected.isBlank()) return@LaunchedEffect
        componentToBeRendered = HomeUiComponent.GameBoard
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (componentToBeRendered != HomeUiComponent.Choice)
            IconButton(
                onClick = {
                    when (componentToBeRendered) {
                        HomeUiComponent.UserNameForm -> componentToBeRendered =
                            HomeUiComponent.Choice

                        HomeUiComponent.RoomsList -> componentToBeRendered =
                            HomeUiComponent.UserNameForm

                        HomeUiComponent.CreateRoomForm -> componentToBeRendered =
                            HomeUiComponent.RoomsList

                        else -> Unit
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .minimumInteractiveComponentSize()
                    .padding(start = 16.dp, top = 16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        when (componentToBeRendered) {
            HomeUiComponent.UserNameForm -> UserNameForm(
                onUserNameSubmit = {
                    userName = it
                    componentToBeRendered = HomeUiComponent.RoomsList
                }
            )

            HomeUiComponent.RoomsList -> {
                viewModel.getGameRooms()
                RoomsList(
                    rooms = homeState.gameRooms.map { Room(it.id, it.name) },
                    onCreateRoomClick = { componentToBeRendered = HomeUiComponent.CreateRoomForm },
                    onRoomClick = { roomId ->
                        if (roomId.isNullOrBlank()) return@RoomsList
                        roomSelected = roomId
                        viewModel.onJoinGameRoom(userId, roomId)
                        componentToBeRendered = HomeUiComponent.GameBoard
                    }
                )
            }

            HomeUiComponent.GameBoard -> onGameBoardClick(roomSelected, userId)
            HomeUiComponent.Choice -> GameModeChoice(
                onPlayOnlineClick = { componentToBeRendered = HomeUiComponent.UserNameForm },
                onPlayOfflineClick = { componentToBeRendered = HomeUiComponent.GameBoard },
            )

            HomeUiComponent.CreateRoomForm -> CreatingRoomForm(
                onCreateRoom = {
                    roomSelected = viewModel.onCreateGameRoom(userId, it)
                }
            )
        }
    }
}

@Composable
fun GameModeChoice(
    onPlayOnlineClick: () -> Unit,
    onPlayOfflineClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CardButton(
                text = "Play Offline",
                image = painterResource(R.drawable.offline),
                enabled = false,
                onClick = onPlayOfflineClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            CardButton(
                text = "Play Online",
                image = painterResource(R.drawable.online),
                onClick = onPlayOnlineClick
            )
        }
    }
}

@Composable
fun CardButton(
    text: String,
    image: Painter,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(270.dp)
            .clickable(enabled = enabled, onClick = onClick),
        elevation = CardDefaults.cardElevation(if (enabled) 10.dp else 0.dp),
        border = if (!enabled) BorderStroke(2.dp, Color.Gray.copy(alpha = 0.4f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (enabled) 1f else 0.5f)
        ) {
            Image(
                painter = image,
                contentDescription = "image",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp)
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun UserNameForm(
    onUserNameSubmit: (String) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var isUsernameValid by remember { mutableStateOf(true) }

    LaunchedEffect(username) { isUsernameValid = username.isNotBlank() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.wrapContentSize()) {
            Text("Enter your username")
            TextField(value = username, onValueChange = {
                username = it
            })
            if (!isUsernameValid)
                Text("Username is required", color = Color.Red)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (isUsernameValid)
                    onUserNameSubmit(username)
            }) {
                Text("Submit")
            }
        }
    }
}

data class Room(val id: String?, val name: String)

@Composable
fun RoomsList(
    rooms: List<Room> = emptyList(),
    onCreateRoomClick: () -> Unit,
    onRoomClick: (String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Text(
            "Rooms List",
            modifier = Modifier
                .padding(start = 16.dp, bottom = 16.dp, end = 24.dp)
                .align(Alignment.End),
            fontSize = 24.sp,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.padding(bottom = 50.dp)) {
                items(rooms) { room ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 8.dp
                            )
                            .clickable {
                                onRoomClick(room.id)
                            }
                    ) {
                        Text(
                            room.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                    }
                }
            }
            Button(
                onClick = onCreateRoomClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("Create Room")
            }
        }
    }
}

@Composable
fun CreatingRoomForm(
    onCreateRoom: (String) -> Unit,
) {
    var roomName by remember { mutableStateOf("") }
    var isRoomNameValid by remember { mutableStateOf(true) }

    LaunchedEffect(roomName) { isRoomNameValid = roomName.isNotBlank() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.wrapContentSize()) {
            Text("Enter room name")
            TextField(value = roomName, onValueChange = {
                roomName = it
            })
            if (!isRoomNameValid)
                Text("Room name is required", color = Color.Red)
            Button(onClick = {
                if (isRoomNameValid)
                    onCreateRoom(roomName)
            }) {
                Text("Create Room")
            }
        }
    }
}


private enum class HomeUiComponent {
    UserNameForm,
    RoomsList,
    GameBoard,
    Choice,
    CreateRoomForm,
}