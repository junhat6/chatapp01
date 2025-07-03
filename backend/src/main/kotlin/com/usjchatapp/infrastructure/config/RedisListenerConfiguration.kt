package com.usjchatapp.infrastructure.config

import com.usjchatapp.infrastructure.listener.RedisMessageListener
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RedisListenerConfiguration(
        private val redisMessageListenerContainer: RedisMessageListenerContainer,
        private val redisMessageListener: RedisMessageListener
) {

    @PostConstruct
    fun setupRedisListeners() {
        // 動的にチャンネルを追加するためのリスナーアダプター
        val messageListenerAdapter = MessageListenerAdapter(redisMessageListener, "onMessage")
        redisMessageListenerContainer.addMessageListener(
                messageListenerAdapter,
                ChannelTopic("waiting-room-*")
        )
    }
}
