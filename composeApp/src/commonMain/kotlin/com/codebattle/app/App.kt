package com.codebattle.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.codebattle.client.GameClient
import com.codebattle.model.GameStatus
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val gameClient = koinInject<GameClient>()
        val connectionStatus by gameClient.connectionStatus.collectAsState()
        val gameState by gameClient.gameState.collectAsState()
        
        LaunchedEffect(Unit) {
            gameClient.connect()
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Status: $connectionStatus", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))

            if (gameState.roomId.isEmpty()) {
                // Lobby UI
                LobbyScreen(
                    onCreateRoom = { nickname -> gameClient.createRoom(nickname) },
                    onJoinRoom = { roomId, nickname -> gameClient.joinRoom(roomId, nickname) }
                )
            } else {
                // Game Room UI
                GameRoomScreen(
                    gameState = gameState,
                    onReadyClick = { gameClient.setReady(it) },
                    onCodeUpdate = { gameClient.updateCode(it) } // Mock for now
                )
            }
        }
    }
}

@Composable
fun LobbyScreen(
    onCreateRoom: (String) -> Unit,
    onJoinRoom: (String, String) -> Unit
) {
    var nickname by remember { mutableStateOf("Player") }
    var roomIdInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("CodeBattle Lobby", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCreateRoom(nickname) },
            enabled = nickname.isNotBlank()
        ) {
            Text("Create New Room")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("OR")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = roomIdInput,
            onValueChange = { roomIdInput = it },
            label = { Text("Room ID") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onJoinRoom(roomIdInput, nickname) },
            enabled = nickname.isNotBlank() && roomIdInput.isNotBlank()
        ) {
            Text("Join Room")
        }
    }
}

@Composable
fun GameRoomScreen(
    gameState: com.codebattle.model.GameState,
    onReadyClick: (Boolean) -> Unit,
    onCodeUpdate: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Room: ${gameState.roomId}", style = MaterialTheme.typography.headlineSmall)
        Text("Status: ${gameState.status}", style = MaterialTheme.typography.titleMedium)
        
        if (gameState.status == GameStatus.COUNTDOWN || gameState.status == GameStatus.BATTLE) {
             Text("Time: ${gameState.timeRemainingSeconds}s", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Players:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(gameState.players) { player ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(player.nickname, style = MaterialTheme.typography.bodyLarge)
                            Text("Status: ${if (player.isReady) "Ready" else "Not Ready"}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (gameState.status == GameStatus.BATTLE) {
                            Text("Code length: ${player.codeText.length} chars")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (gameState.status == GameStatus.WAITING) {
            val myPlayer = gameState.players.find { true } // In real app identify self
            // Simplification: Just a toggle button for testing
            Button(onClick = { onReadyClick(true) }) {
                Text("I am Ready!")
            }
        }
        
        if (gameState.status == GameStatus.BATTLE) {
            Text("Task: ${gameState.taskText}")
            // Here will be the code editor later
            Button(onClick = { onCodeUpdate("fun solve() = 42") }) {
                Text("Simulate Typing Code")
            }
        }
    }
}
