package com.codebattle.app

import com.codebattle.di.serverModule
import com.codebattle.di.sharedModule
import com.codebattle.model.GameEvent
import com.codebattle.server.game.RoomManager
import com.codebattle.server.game.GameRoom
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(serverModule, sharedModule)
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        
        anyHost()
        allowNonSimpleContentTypes = true
    }

    install(WebSockets) {
        pingPeriod = kotlin.time.Duration.parse("15s")
        timeout = kotlin.time.Duration.parse("15s")
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val roomManager by inject<RoomManager>()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        
        webSocket("/ws") {
            var currentRoom: GameRoom? = null
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            val event = Json.decodeFromString<GameEvent>(text)
                            when (event) {
                                is GameEvent.CreateRoom -> {
                                    currentRoom = roomManager.createRoom(this, event.nickname)
                                }
                                is GameEvent.JoinRoom -> {
                                    val room = roomManager.joinRoom(event.roomId, this, event.nickname)
                                    if (room == null) {
                                        send(Frame.Text(Json.encodeToString<GameEvent>(GameEvent.Error("Room not found"))))
                                    } else {
                                        currentRoom = room
                                    }
                                }
                                is GameEvent.UpdateCode -> {
                                    currentRoom?.updatePlayerCode(this, event.codeText)
                                }
                                is GameEvent.SetReady -> {
                                    currentRoom?.setPlayerReady(this, event.isReady)
                                }
                                is GameEvent.SubmitSolution -> {
                                    currentRoom?.submitSolution(this, event.codeText)
                                }
                                else -> {}
                            }
                        } catch (e: Exception) {
                            send(Frame.Text(Json.encodeToString<GameEvent>(GameEvent.Error("Invalid JSON or Event: ${e.message}"))))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                roomManager.removePlayer(this)
            }
        }
    }
}
