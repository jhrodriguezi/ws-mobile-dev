package lab.jhrodriguezi.tictactoe.data.network

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lab.jhrodriguezi.tictactoe.data.network.model.GameModel
import javax.inject.Inject

class FirebaseService @Inject constructor(
    private val databaseReference: DatabaseReference
) {
    companion object {
        private const val GAME_ROOMS = "game_rooms"
    }

    fun createGameRoom(gameModel: GameModel): String {
        val gameReference = databaseReference.child(GAME_ROOMS).push()
        val gameKey = gameReference.key
        val gameToSave = gameModel.copy(id = gameKey.toString())
        gameReference.setValue(gameToSave)
        return gameKey.toString()
    }

    fun getGameRoom(gameRoomId: String): Flow<GameModel?> {
        return databaseReference.child(GAME_ROOMS).child(gameRoomId)
            .snapshots.map { it.getValue(GameModel::class.java) }
    }

    fun joinGameRoom(gameRoomId: String): Flow<GameModel?> {
        return databaseReference.database.reference.child(GAME_ROOMS).child(gameRoomId)
            .snapshots.map { it.getValue(GameModel::class.java) }
    }

    fun updateGameRoom(game: GameModel) {
        Log.i("FirebaseService", "updateGameRoom: $game")
        if (game.id != null) {
            databaseReference.child(GAME_ROOMS).child(game.id).setValue(game)
        }
    }

    fun deleteGameRoom(gameRoomId: String) {
        databaseReference.child(GAME_ROOMS).child(gameRoomId).removeValue()
    }

    fun observeGameRoomDeletion(gameRoomId: String, onDeleted: () -> Unit) {
        databaseReference.child(GAME_ROOMS).child(gameRoomId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Si el snapshot ya no existe, significa que se eliminó
                    if (!snapshot.exists()) {
                        onDeleted()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores en caso de ser necesario
                    Log.e("Firebase", "Error observing game room deletion: ${error.message}")
                }
            })
    }

    fun getGameRooms(): Flow<List<GameModel>> {
        return databaseReference.child(GAME_ROOMS).snapshots.map { dataSnapshot ->
            val gameRooms = mutableListOf<GameModel>()
            dataSnapshot.children.forEach { data ->
                val gameRoom = data.getValue(GameModel::class.java)
                gameRoom?.let { gameRooms.add(it) }
            }
            gameRooms
        }
    }
}