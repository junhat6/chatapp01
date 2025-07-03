package com.usjchatapp.infrastructure.config

import com.corundumstudio.socketio.Configuration as SocketIOConfiguration
import com.corundumstudio.socketio.SocketIOServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SocketIOConfig {

    @Value("\${server.port:8080}") private lateinit var serverPort: String

    @Bean
    fun socketIOServer(): SocketIOServer {
        val config = SocketIOConfiguration()
        config.hostname = "localhost"
        config.port = 9090 // Socket.IO専用ポート

        // タイムアウト設定を緩和
        config.setPingTimeout(60000) // 60秒（デフォルト: 60000）
        config.setPingInterval(25000) // 25秒（デフォルト: 25000）
        config.setUpgradeTimeout(10000) // 10秒（デフォルト: 10000）
        config.setAuthorizationListener { data ->
            // 認証は後で実装
            true
        }

        // CORS設定
        config.origin = "*"

        return SocketIOServer(config)
    }
}
