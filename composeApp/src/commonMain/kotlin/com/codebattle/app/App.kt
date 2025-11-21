package com.codebattle.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
        val runOutput by gameClient.runOutput.collectAsState()
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
                        runOutput = runOutput,
                        onReadyClick = { gameClient.setReady(it) },
                        onCodeUpdate = { gameClient.updateCode(it) },
                        onSubmitSolution = { gameClient.submitSolution(it) },
                        onRunCode = { gameClient.runCode(it) },
                        onDisconnect = { gameClient.leaveRoom() },
                        onPlayAgain = { gameClient.leaveRoom() }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    runOutput: String?,
    onReadyClick: (Boolean) -> Unit,
    onCodeUpdate: (String) -> Unit,
    onSubmitSolution: (String) -> Unit,
    onRunCode: (String) -> Unit,
    onDisconnect: () -> Unit,
    onPlayAgain: () -> Unit
) {
    val myPlayer = gameState.players.find { it.nickname == currentPlayerName }
    val opponent = gameState.players.firstOrNull { it.nickname != currentPlayerName }
    var codeValue by rememberSaveable(gameState.roomId, currentPlayerName, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(myPlayer?.codeText.orEmpty()))
    }

    LaunchedEffect(myPlayer?.codeText) {
        val incoming = myPlayer?.codeText.orEmpty()
        if (incoming != codeValue.text) {
            codeValue = TextFieldValue(incoming, TextRange(incoming.length))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ROOM ${gameState.roomId}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text("Status: ${gameState.status}", style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onDisconnect) {
                Text("Disconnect")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (gameState.status) {
            GameStatus.BATTLE -> BattleLayout(
                gameState = gameState,
                codeValue = codeValue,
                onCodeValueChange = { newValue ->
                    val processed = smartCodeInput(codeValue, newValue)
                    codeValue = processed
                    onCodeUpdate(processed.text)
                },
                runOutput = runOutput ?: myPlayer?.lastRunOutput,
                myPlayer = myPlayer,
                opponent = opponent,
                onRunCode = { onRunCode(codeValue.text) },
                onSubmitSolution = { onSubmitSolution(codeValue.text) }
            )
            GameStatus.RESULT -> ResultLayout(gameState = gameState, onPlayAgain = onPlayAgain)
            else -> WaitingLayout(
                gameState = gameState,
                myPlayer = myPlayer,
                onReadyClick = onReadyClick
            )
        }
    }
}

@Composable
private fun WaitingLayout(
    gameState: com.codebattle.model.GameState,
    myPlayer: com.codebattle.model.Player?,
    onReadyClick: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (gameState.status == GameStatus.COUNTDOWN) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${gameState.timeRemainingSeconds}",
                    style = MaterialTheme.typography.displayLarge,
                    color = if (gameState.timeRemainingSeconds < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        PlayersList(
            players = gameState.players,
            showCodeLength = false,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val ready = myPlayer?.isReady == true
        Button(
            onClick = { onReadyClick(!ready) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (ready) "Cancel Ready" else "Ready Up", fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun BattleLayout(
    gameState: com.codebattle.model.GameState,
    codeValue: TextFieldValue,
    onCodeValueChange: (TextFieldValue) -> Unit,
    runOutput: String?,
    myPlayer: com.codebattle.model.Player?,
    opponent: com.codebattle.model.Player?,
    onRunCode: () -> Unit,
    onSubmitSolution: () -> Unit
) {
    val editorBorderColor = if (myPlayer?.isFinished == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val opponentBorderColor = if (opponent?.isFinished == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.35f).fillMaxHeight()
        ) {
            TimerDisplay(gameState.timeRemainingSeconds)
            runOutput?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Last run: $it", color = MaterialTheme.colorScheme.secondary, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            PlayersList(
                players = gameState.players,
                showCodeLength = true,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier.weight(0.65f).fillMaxHeight()
        ) {
            TaskCard(gameState.taskText)
            Spacer(modifier = Modifier.height(16.dp))
            CodeEditor(
                value = codeValue,
                onValueChange = onCodeValueChange,
                borderColor = editorBorderColor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRunCode,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Run Code", fontFamily = FontFamily.Monospace)
                }
                Button(
                    onClick = onSubmitSolution,
                    enabled = myPlayer?.isFinished != true,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (myPlayer?.isFinished == true) "Submitted" else "Submit", fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            opponent?.let {
                OpponentCodeView(
                    code = it.codeText,
                    nickname = it.nickname,
                    borderColor = opponentBorderColor,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
private fun ResultLayout(
    gameState: com.codebattle.model.GameState,
    onPlayAgain: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Battle Complete", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                PlayersList(players = gameState.players, showCodeLength = false, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play Again")
                }
            }
        }
    }
}

@Composable
private fun TimerDisplay(time: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${time}s remaining",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TaskCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TASK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun PlayersList(
    players: List<com.codebattle.model.Player>,
    showCodeLength: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("PLAYERS:", style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            items(players) { player ->
                val borderColor = when {
                    player.isFinished -> MaterialTheme.colorScheme.secondary
                    player.isReady -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, borderColor, RoundedCornerShape(6.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(player.nickname, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text(
                                when {
                                    player.isFinished -> "Submitted"
                                    player.isReady -> "Ready"
                                    else -> "Waiting"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (player.isFinished) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        if (showCodeLength) {
                            Text("${player.codeText.length} chars", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeEditor(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val lineCount = maxOf(1, value.text.split('\n').size)
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
            value = value,
            onValueChange = onValueChange,
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
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("$nickname's code", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(1.dp)
        ) {
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
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

private fun smartCodeInput(
    previous: TextFieldValue,
    current: TextFieldValue
): TextFieldValue {
    var text = current.text
    var selection = current.selection.start
    val inserted = current.text.length - previous.text.length
    val wasSingleInsert = inserted == 1 && current.selection.start == current.selection.end
    if (wasSingleInsert && selection > 0) {
        val insertedChar = current.text[selection - 1]
        when (insertedChar) {
            '\n' -> {
                val prevCursor = previous.selection.start
                val prevLine = previous.text.substring(0, prevCursor).substringAfterLast('\n', "")
                val baseIndent = prevLine.takeWhile { it == ' ' || it == '\t' }
                val extraIndent = if (prevLine.trimEnd().endsWith("{")) "    " else ""
                val indent = baseIndent + extraIndent
                text = text.substring(0, selection) + indent + text.substring(selection)
                selection += indent.length
            }
            '(', '{', '[' -> {
                val closing = when (insertedChar) {
                    '(' -> ')'
                    '{' -> '}'
                    '[' -> ']'
                    else -> null
                }
                closing?.let {
                    text = text.substring(0, selection) + it + text.substring(selection)
                }
            }
            '"', '\'' -> {
                val prevChar = previous.text.getOrNull(previous.selection.start - 1)
                val nextChar = text.getOrNull(selection)
                if (prevChar != '\\' && nextChar != insertedChar) {
                    text = text.substring(0, selection) + insertedChar + text.substring(selection)
                }
            }
        }
    }
    return TextFieldValue(text, TextRange(selection))
}
