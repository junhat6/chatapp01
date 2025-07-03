package com.usjchatapp.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // クライアントがサブスクライブするプレフィックス
        registry.enableSimpleBroker("/topic")

        // クライアントがメッセージを送信するプレフィックス
        registry.setApplicationDestinationPrefixes("/app")

        // ユーザー宛のメッセージプレフィックス
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket接続エンドポイント
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 本番環境では適切に制限してください
                .withSockJS() // SockJSフォールバックを有効化
    }
}
