package com.codebattle.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.blur
import com.codebattle.app.theme.CodeBattleTheme
import com.codebattle.app.ui.SyntaxHighlightTransformation
import com.codebattle.client.GameClient
import com.codebattle.model.GameStatus
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    CodeBattleTheme {
        val gameClient = koinInject<GameClient>()
        val connectionStatus by gameClient.connectionStatus.collectAsState()
        val gameState by gameClient.gameState.collectAsState()
        var nickname by rememberSaveable { mutableStateOf("Player") }
        
        LaunchedEffect(Unit) {
            gameClient.connect()
        }

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                     Text(
                         text = "STATUS: ${connectionStatus.uppercase()}",
                         style = MaterialTheme.typography.labelSmall,
                         color = if (connectionStatus == "Connected") Color.Green else Color.Red,
                         fontFamily = FontFamily.Monospace
                     )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                if (gameState.roomId.isEmpty()) {
                    LobbyScreen(
                        nickname = nickname,
                        onNicknameChange = { nickname = it },
                        onCreateRoom = { name -> gameClient.createRoom(name) },
                        onJoinRoom = { roomId, name -> gameClient.joinRoom(roomId, name) }
                    )
                } else {
                    GameRoomScreen(
                        currentPlayerName = nickname,
                        gameState = gameState,
                        onReadyClick = { gameClient.setReady(it) },
                        onCodeUpdate = { gameClient.updateCode(it) },
                        onSubmitSolution = { gameClient.submitSolution(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun LobbyScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onCreateRoom: (String) -> Unit,
    onJoinRoom: (String, String) -> Unit
) {
    var roomIdInput by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
            Text(
                "CODE BATTLE",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { onNicknameChange(it) },
                label = { Text("NICKNAME") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onCreateRoom(nickname) },
                enabled = nickname.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("CREATE ROOM", fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("- OR JOIN EXISTING -", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = roomIdInput,
                onValueChange = { roomIdInput = it.uppercase() },
                label = { Text("ROOM CODE") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                 colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
                Button(
                    onClick = { onJoinRoom(roomIdInput, nickname) },
                    enabled = nickname.isNotBlank() && roomIdInput.isNotBlank(),
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text("JOIN", fontFamily = FontFamily.Monospace)
                }
            }
    }
}

@Composable
fun GameRoomScreen(
    currentPlayerName: String,
    gameState: com.codebattle.model.GameState,
    onReadyClick: (Boolean) -> Unit,
    onCodeUpdate: (String) -> Unit,
    onSubmitSolution: (String) -> Unit
) {
    var codeText by rememberSaveable(gameState.roomId) { mutableStateOf("") }
    val myPlayer = gameState.players.find { it.nickname == currentPlayerName }
    val opponent = gameState.players.firstOrNull { it.nickname != currentPlayerName }

    LaunchedEffect(myPlayer?.codeText) {
        myPlayer?.codeText?.let { codeText = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ROOM ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(gameState.roomId, style = MaterialTheme.typography.headlineMedium, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(gameState.status.toString(), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Monospace)
                }
            }
        }

        if (gameState.status == GameStatus.COUNTDOWN || gameState.status == GameStatus.BATTLE) {
             Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                 Text(
                     "${gameState.timeRemainingSeconds}", 
                     style = MaterialTheme.typography.displayLarge, 
                     color = if(gameState.timeRemainingSeconds < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                     fontFamily = FontFamily.Monospace
                 )
             }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Players List
        Text("PLAYERS:", style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onBackground)
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            items(gameState.players) { player ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, if(player.isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(player.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(if (player.isReady) "READY" else "NOT READY", style = MaterialTheme.typography.bodySmall, color = if(player.isReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        if (gameState.status == GameStatus.BATTLE) {
                             Text("${player.codeText.length} chars", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        if (gameState.status == GameStatus.WAITING) {
            Button(
                onClick = { onReadyClick(true) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp),
                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Text("I AM READY", fontFamily = FontFamily.Monospace)
            }
        }
        
        if (gameState.status == GameStatus.BATTLE) {
            // Task Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                 colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("TASK:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(gameState.taskText, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            CodeEditor(
                code = codeText,
                onCodeChange = {
                    codeText = it
                    onCodeUpdate(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            opponent?.let {
                OpponentCodeView(
                    code = it.codeText,
                    nickname = it.nickname,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { onSubmitSolution(codeText) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SUBMIT SOLUTION", fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val lineCount = maxOf(1, code.split('\n').size)
        Column(
            modifier = Modifier.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(lineCount) { index ->
                Text(
                    text = (index + 1).toString().padStart(2, ' '),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp
            ),
            minLines = 12,
            maxLines = 30,
            visualTransformation = SyntaxHighlightTransformation(MaterialTheme.colorScheme.secondary),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

@Composable
fun OpponentCodeView(
    code: String,
    nickname: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$nickname's code",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = code,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .blur(15.dp),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 14.sp
            ),
            minLines = 6,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                disabledIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        )
    }
}
