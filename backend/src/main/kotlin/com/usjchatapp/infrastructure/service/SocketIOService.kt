package com.usjchatapp.infrastructure.service

import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.ConnectListener
import com.corundumstudio.socketio.listener.DisconnectListener
import com.usjchatapp.infrastructure.security.JwtUtils
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SocketIOService
@Autowired
constructor(private val socketIOServer: SocketIOServer, private val jwtUtils: JwtUtils) {

    private val logger = LoggerFactory.getLogger(SocketIOService::class.java)

    @PostConstruct
    fun start() {
        setupEventListeners()
        socketIOServer.start()
        logger.info("Socket.IO server started on port ${socketIOServer.configuration.port}")
    }

    @PreDestroy
    fun stop() {
        socketIOServer.stop()
        logger.info("Socket.IO server stopped")
    }

    private fun setupEventListeners() {
        // 接続イベント
        socketIOServer.addConnectListener(
                ConnectListener { client ->
                    logger.info("Client connected: ${client.sessionId}")
                    client.sendEvent("connected")
                }
        )

        // 切断イベント
        socketIOServer.addDisconnectListener(
                DisconnectListener { client ->
                    logger.info("Client disconnected: ${client.sessionId}")
                }
        )

        // 認証イベント
        socketIOServer.addEventListener("authenticate", String::class.java) {
                client,
                data,
                ackRequest ->
            try {
                // JWTトークンを検証
                val username = jwtUtils.extractUsername(data)
                logger.info("Client authenticated: $username (${client.sessionId})")

                // 認証成功をクライアントに通知
                client.sendEvent("authenticated")

                // 認証情報をクライアントに保存
                client.set("authenticated", true)
                client.set("username", username)
            } catch (e: Exception) {
                logger.error("Authentication failed for client ${client.sessionId}: ${e.message}")
                client.sendEvent("error", mapOf("message" to "認証に失敗しました"))
            }
        }

        // 待機室参加イベント
        socketIOServer.addEventListener("join_matching_room", Map::class.java) {
                client,
                data,
                ackRequest ->
            val requestId = data["requestId"] as? Long
            if (requestId != null) {
                val roomName = "matching_room_$requestId"
                client.joinRoom(roomName)
                logger.info("Client ${client.sessionId} joined room: $roomName")

                // 他の参加者に通知
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "participant_joined",
                                mapOf(
                                        "requestId" to requestId,
                                        "participant" to
                                                mapOf(
                                                        "userId" to client.get<String>("username"),
                                                        "sessionId" to client.sessionId
                                                )
                                )
                        )
            }
        }

        // 待機室退出イベント
        socketIOServer.addEventListener("leave_matching_room", Map::class.java) {
                client,
                data,
                ackRequest ->
            val requestId = data["requestId"] as? Long
            if (requestId != null) {
                val roomName = "matching_room_$requestId"
                client.leaveRoom(roomName)
                logger.info("Client ${client.sessionId} left room: $roomName")

                // 他の参加者に通知
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "participant_left",
                                mapOf(
                                        "requestId" to requestId,
                                        "userId" to client.get<String>("username")
                                )
                        )
            }
        }

        // チャットルーム参加イベント
        socketIOServer.addEventListener("join_chat_room", Map::class.java) {
                client,
                data,
                ackRequest ->
            val chatRoomId = data["chatRoomId"] as? Long
            if (chatRoomId != null) {
                val roomName = "chat_room_$chatRoomId"
                client.joinRoom(roomName)
                logger.info("Client ${client.sessionId} joined chat room: $roomName")

                // 他の参加者に通知
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "user_joined_chat",
                                mapOf(
                                        "chatRoomId" to chatRoomId,
                                        "participant" to
                                                mapOf(
                                                        "userId" to client.get<String>("username"),
                                                        "sessionId" to client.sessionId
                                                )
                                )
                        )
            }
        }

        // チャットルーム退出イベント
        socketIOServer.addEventListener("leave_chat_room", Map::class.java) {
                client,
                data,
                ackRequest ->
            val chatRoomId = data["chatRoomId"] as? Long
            if (chatRoomId != null) {
                val roomName = "chat_room_$chatRoomId"
                client.leaveRoom(roomName)
                logger.info("Client ${client.sessionId} left chat room: $roomName")

                // 他の参加者に通知
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "user_left_chat",
                                mapOf(
                                        "chatRoomId" to chatRoomId,
                                        "userId" to client.get<String>("username")
                                )
                        )
            }
        }

        // メッセージ送信イベント
        socketIOServer.addEventListener("send_message", Map::class.java) { client, data, ackRequest
            ->
            val chatRoomId = data["chatRoomId"] as? Long
            val content = data["content"] as? String
            val messageType = data["messageType"] as? String

            if (chatRoomId != null && content != null) {
                val roomName = "chat_room_$chatRoomId"
                val message =
                        mapOf(
                                "chatRoomId" to chatRoomId,
                                "senderUserId" to client.get<String>("username"),
                                "content" to content,
                                "messageType" to (messageType ?: "TEXT"),
                                "sentAt" to System.currentTimeMillis()
                        )

                // チャットルーム内の全員にメッセージを送信
                socketIOServer.getRoomOperations(roomName).sendEvent("new_message", message)
                logger.info("Message sent to room $roomName by ${client.get<String>("username")}")
            }
        }

        // タイピング開始イベント
        socketIOServer.addEventListener("start_typing", Map::class.java) { client, data, ackRequest
            ->
            val chatRoomId = data["chatRoomId"] as? Long
            if (chatRoomId != null) {
                val roomName = "chat_room_$chatRoomId"
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "typing_start",
                                mapOf(
                                        "chatRoomId" to chatRoomId,
                                        "userId" to client.get<String>("username"),
                                        "displayName" to client.get<String>("username")
                                )
                        )
            }
        }

        // タイピング停止イベント
        socketIOServer.addEventListener("stop_typing", Map::class.java) { client, data, ackRequest
            ->
            val chatRoomId = data["chatRoomId"] as? Long
            if (chatRoomId != null) {
                val roomName = "chat_room_$chatRoomId"
                socketIOServer
                        .getRoomOperations(roomName)
                        .sendEvent(
                                "typing_stop",
                                mapOf(
                                        "chatRoomId" to chatRoomId,
                                        "userId" to client.get<String>("username")
                                )
                        )
            }
        }
    }

    // 外部からイベントを送信するためのメソッド
    fun sendToMatchingRoom(requestId: Long, event: String, data: Any) {
        val roomName = "matching_room_$requestId"
        socketIOServer.getRoomOperations(roomName).sendEvent(event, data)
    }

    fun sendToChatRoom(chatRoomId: Long, event: String, data: Any) {
        val roomName = "chat_room_$chatRoomId"
        socketIOServer.getRoomOperations(roomName).sendEvent(event, data)
    }

    fun sendToClient(sessionId: UUID, event: String, data: Any) {
        val client = socketIOServer.getClient(sessionId)
        client?.sendEvent(event, data)
    }
}
