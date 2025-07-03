<template>
  <div class="flex flex-col h-screen bg-gray-50">
    <!-- ヘッダー -->
    <div class="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between shadow-sm">
      <div class="flex items-center">
        <n-button 
          text 
          @click="goBack"
          class="mr-3 text-gray-600 hover:text-primary-600"
        >
          <template #icon>
            <div class="i-carbon-arrow-left text-lg"></div>
          </template>
        </n-button>
        
        <div class="flex items-center">
          <div class="w-10 h-10 bg-primary-500 rounded-full flex items-center justify-center mr-3">
            <span class="text-white text-sm font-bold">{{ getAttractionEmoji(matchingRequest?.attraction || '') }}</span>
          </div>
          <div>
            <h1 class="text-lg font-semibold text-gray-900">
              {{ chatRoom?.name || 'チャットルーム' }}
            </h1>
            <p class="text-sm text-gray-600">
              {{ chatRoom?.participants?.length || 0 }}人が参加中
            </p>
          </div>
        </div>
      </div>

      <!-- 参加者表示ボタン -->
      <n-button 
        @click="showParticipants = !showParticipants"
        text
        class="text-gray-600 hover:text-primary-600"
      >
        <template #icon>
          <div class="i-carbon-user-multiple text-lg"></div>
        </template>
        <span class="ml-1 hidden sm:inline">参加者</span>
      </n-button>
    </div>

    <!-- メインコンテンツ -->
    <div class="flex flex-1 overflow-hidden">
      <!-- チャットエリア -->
      <div class="flex-1 flex flex-col">
        <!-- ローディング状態 -->
        <div v-if="isLoading && messages.length === 0" class="flex-1 flex items-center justify-center">
          <n-spin size="medium">
            <template #description>
              <div class="text-gray-600 mt-3">チャットルームを読み込み中...</div>
            </template>
          </n-spin>
        </div>

        <!-- エラーアラート -->
        <n-alert 
          v-else-if="error" 
          type="error" 
          :show-icon="true"
          class="m-4"
          closable
          @close="error = null"
        >
          {{ error }}
        </n-alert>

        <!-- メッセージリスト -->
        <div 
          v-else
          ref="messageContainer"
          class="flex-1 overflow-y-auto px-4 py-3 space-y-3"
          @scroll="handleScroll"
        >
          <!-- 日付セパレーター -->
          <div 
            v-for="(group, date) in groupedMessages" 
            :key="date"
          >
            <!-- 日付ヘッダー -->
            <div class="flex justify-center my-4">
              <div class="bg-gray-100 rounded-full px-3 py-1">
                <span class="text-sm text-gray-600">{{ formatDate(date) }}</span>
              </div>
            </div>

            <!-- その日のメッセージ -->
            <div 
              v-for="message in group" 
              :key="message.id"
              class="flex"
              :class="isOwnMessage(message) ? 'justify-end' : 'justify-start'"
            >
              <!-- 他人のメッセージ -->
              <div 
                v-if="!isOwnMessage(message)"
                class="flex items-start max-w-xs lg:max-w-md"
              >
                <!-- アバター -->
                <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center mr-2 flex-shrink-0">
                  <span class="text-white text-xs font-bold">
                    {{ message.senderDisplayName.charAt(0) }}
                  </span>
                </div>
                
                <div class="flex flex-col">
                  <!-- 送信者名 -->
                  <div class="text-xs text-gray-600 mb-1">{{ message.senderDisplayName }}</div>
                  
                  <!-- メッセージバブル -->
                  <div 
                    class="rounded-lg px-3 py-2"
                    :class="getMessageBubbleClass(message.messageType, false)"
                  >
                    <div class="text-sm" :class="getMessageTextClass(message.messageType)">
                      {{ message.content }}
                    </div>
                    <div class="text-xs mt-1 opacity-75" :class="getTimeTextClass(message.messageType)">
                      {{ formatTime(message.sentAt) }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 自分のメッセージ -->
              <div 
                v-else
                class="flex items-end max-w-xs lg:max-w-md"
              >
                <div class="flex flex-col items-end">
                  <!-- メッセージバブル -->
                  <div 
                    class="rounded-lg px-3 py-2"
                    :class="getMessageBubbleClass(message.messageType, true)"
                  >
                    <div class="text-sm" :class="getMessageTextClass(message.messageType)">
                      {{ message.content }}
                    </div>
                    <div class="text-xs mt-1 opacity-75" :class="getTimeTextClass(message.messageType)">
                      {{ formatTime(message.sentAt) }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- システムメッセージ -->
              <div 
                v-if="message.messageType === 'SYSTEM'"
                class="w-full flex justify-center"
              >
                <div class="bg-gray-100 rounded-full px-4 py-2 max-w-md">
                  <div class="text-sm text-gray-600 text-center">
                    {{ message.content }}
                  </div>
                  <div class="text-xs text-gray-500 text-center mt-1">
                    {{ formatTime(message.sentAt) }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 新しいメッセージ表示ボタン -->
          <div 
            v-if="hasNewMessages"
            class="fixed bottom-20 left-1/2 transform -translate-x-1/2 z-10"
          >
            <n-button 
              @click="scrollToBottom"
              type="primary"
              round
              class="btn-primary shadow-lg"
            >
              <template #icon>
                <div class="i-carbon-arrow-down"></div>
              </template>
              新しいメッセージ
            </n-button>
          </div>
        </div>

        <!-- メッセージ入力エリア -->
        <div class="bg-white border-t border-gray-200 p-4">
          <div class="flex items-end space-x-3">
            <div class="flex-1">
              <n-input
                v-model:value="newMessage"
                type="textarea"
                placeholder="メッセージを入力..."
                :autosize="{
                  minRows: 1,
                  maxRows: 4
                }"
                :maxlength="500"
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.enter.shift.exact="newMessage += '\n'"
                class="resize-none"
              />
            </div>
            <n-button
              @click="sendMessage"
              type="primary"
              :disabled="!canSendMessage"
              :loading="isSendingMessage"
              circle
              class="btn-primary"
            >
              <template #icon>
                <div class="i-carbon-send-alt text-lg"></div>
              </template>
            </n-button>
          </div>
          
          <!-- 文字数制限表示 -->
          <div class="text-right mt-1">
            <span class="text-xs text-gray-500">
              {{ newMessage.length }}/500
            </span>
          </div>
        </div>
      </div>

      <!-- 参加者サイドバー（デスクトップ時） -->
      <div 
        v-if="showParticipants"
        class="w-80 bg-white border-l border-gray-200 hidden lg:block"
      >
        <div class="p-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-900">参加者一覧</h3>
          <p class="text-sm text-gray-600">{{ chatRoom?.participants?.length || 0 }}人</p>
        </div>
        
        <div class="p-4 space-y-3 overflow-y-auto" style="max-height: calc(100vh - 120px);">
          <div 
            v-for="participant in chatRoom?.participants" 
            :key="participant.userId"
            class="flex items-center p-3 rounded-lg hover:bg-gray-50"
          >
            <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center mr-3">
              <span class="text-white text-sm font-bold">
                {{ participant.displayName.charAt(0) }}
              </span>
            </div>
            <div class="flex-1">
              <div class="font-medium text-gray-900">{{ participant.displayName }}</div>
              <div class="text-sm text-gray-600">
                {{ participant.isHost ? 'ホスト' : 'メンバー' }}
              </div>
            </div>
            <div class="w-2 h-2 bg-green-500 rounded-full"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 参加者モーダル（モバイル時） -->
    <n-modal 
      v-model:show="showParticipantsModal"
      preset="card"
      title="参加者一覧"
      class="w-full max-w-md mx-4 lg:hidden"
    >
      <div class="space-y-3">
        <div 
          v-for="participant in chatRoom?.participants" 
          :key="participant.userId"
          class="flex items-center p-3 rounded-lg bg-gray-50"
        >
          <div class="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center mr-3">
            <span class="text-white text-sm font-bold">
              {{ participant.displayName.charAt(0) }}
            </span>
          </div>
          <div class="flex-1">
            <div class="font-medium text-gray-900">{{ participant.displayName }}</div>
            <div class="text-sm text-gray-600">
              {{ participant.isHost ? 'ホスト' : 'メンバー' }}
            </div>
          </div>
          <div class="w-2 h-2 bg-green-500 rounded-full"></div>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import type { ChatRoomDto, ChatMessageDto, MatchingRequestDto } from '@/types'
import { ChatMessageType } from '@/types'
import { chatRoomApi, matchingRequestApi, stompWebSocketService } from '@/services'

interface Props {
  requestId: number
}

const props = defineProps<Props>()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const profileStore = useProfileStore()

// State
const chatRoom = ref<ChatRoomDto | null>(null)
const matchingRequest = ref<MatchingRequestDto | null>(null)
const messages = ref<ChatMessageDto[]>([])
const newMessage = ref('')
const error = ref<string | null>(null)
const isLoading = ref(false)
const isSendingMessage = ref(false)
const showParticipants = ref(false)
const showParticipantsModal = ref(false)
const hasNewMessages = ref(false)
const messageContainer = ref<HTMLElement | null>(null)

// Polling
let pollingInterval: NodeJS.Timeout | null = null
let lastMessageCount = 0

// WebSocket event handlers
const websocketEventHandlers = ref<Array<{ event: string, handler: Function }>>([])
const isWebSocketConnected = ref(false)
const typingUsers = ref<Set<string>>(new Set())

// Computed
const canSendMessage = computed(() => 
  newMessage.value.trim().length > 0 && !isSendingMessage.value
)

const groupedMessages = computed(() => {
  const grouped: Record<string, ChatMessageDto[]> = {}
  
  messages.value.forEach(message => {
    const date = new Date(message.sentAt).toDateString()
    if (!grouped[date]) {
      grouped[date] = []
    }
    grouped[date].push(message)
  })
  
  return grouped
})

// Utility functions
const isOwnMessage = (message: ChatMessageDto): boolean => {
  return message.senderUserId === authStore.user?.id
}

const getMessageBubbleClass = (messageType: ChatMessageType, isOwn: boolean): string => {
  if (messageType === 'SYSTEM') {
    return 'bg-gray-100'
  }
  
  return isOwn 
    ? 'bg-primary-500 text-white' 
    : 'bg-white border border-gray-200'
}

const getMessageTextClass = (messageType: ChatMessageType): string => {
  if (messageType === 'SYSTEM') {
    return 'text-gray-600'
  }
  return ''
}

const getTimeTextClass = (messageType: ChatMessageType): string => {
  if (messageType === 'SYSTEM') {
    return 'text-gray-500'
  }
  return ''
}

const getAttractionEmoji = (attraction: string): string => {
  const emojiMap: Record<string, string> = {
    'ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー': '⚡',
    'フライト・オブ・ザ・ヒッポグリフ': '🦅',
    'ミニオン・ハチャメチャ・ライド': '💛',
    'ジュラシック・パーク・ザ・ライド': '🦕',
    'ザ・フライング・ダイナソー': '🦖',
    'ハリウッド・ドリーム・ザ・ライド': '🌟',
    'ハリウッド・ドリーム・ザ・ライド〜バックドロップ〜': '⭐',
    'スペース・ファンタジー・ザ・ライド': '🚀',
    'シング・オン・ツアー': '🎤',
    'ターミネーター 2:3-D': '🤖',
    'シュレック 4-D アドベンチャー': '🏰',
    'ミニオン・ハチャメチャ・アイス': '🍦',
    'ハリー・ポッター エリア散策': '🏛️',
    'ミニオン・パーク エリア散策': '🎨',
    'ジュラシック・パーク エリア散策': '🌿',
    'フード・コート': '🍔',
    'お土産ショッピング': '🛍️'
  }
  return emojiMap[attraction] || '💬'
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (date.toDateString() === today.toDateString()) {
    return '今日'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨日'
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
  }
}

const formatTime = (dateTime: string): string => {
  const date = new Date(dateTime)
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

// Data loading
const loadChatRoom = async () => {
  try {
    isLoading.value = true
    error.value = null

    const chatRoomResponse = await chatRoomApi.getChatRoomByRequestId(props.requestId)
    const chatRoomData = chatRoomResponse.data.data
    
    if (chatRoomData) {
      chatRoom.value = chatRoomData
      
      // マッチングリクエスト情報も取得
      try {
        const matchingResponse = await matchingRequestApi.getMatchingRequest(chatRoomData.matchingRequestId)
        if (matchingResponse.data.data) {
          matchingRequest.value = matchingResponse.data.data
        }
      } catch (matchingError) {
        console.warn('マッチングリクエスト情報の取得に失敗:', matchingError)
      }
    }

  } catch (err: any) {
    console.error('チャットルーム取得エラー:', err)
    error.value = err.response?.data?.message || 'チャットルームの取得に失敗しました'
  } finally {
    isLoading.value = false
  }
}

const loadMessages = async () => {
  if (!chatRoom.value) return

  try {
    const messagesResponse = await chatRoomApi.getMessages(chatRoom.value.id)
    const newMessages = messagesResponse.data.data || []
    
    // 新しいメッセージがある場合の検知
    if (newMessages.length > lastMessageCount && lastMessageCount > 0) {
      hasNewMessages.value = true
    }
    
    messages.value = newMessages
    lastMessageCount = newMessages.length

  } catch (err: any) {
    console.error('メッセージ取得エラー:', err)
  }
}

// Actions
const sendMessage = async () => {
  if (!canSendMessage.value || !chatRoom.value) return

  try {
    isSendingMessage.value = true
    
    // WebSocket経由でメッセージ送信
    if (stompWebSocketService.isConnected()) {
      stompWebSocketService.sendMessage(
        chatRoom.value.id, 
        newMessage.value.trim(), 
        ChatMessageType.TEXT
      )
      newMessage.value = ''
      // メッセージはWebSocketイベントで更新されるため、ここでは再取得しない
    } else {
      // フォールバック: 直接API呼び出し
      await chatRoomApi.sendMessage(chatRoom.value.id, {
        content: newMessage.value.trim(),
        messageType: ChatMessageType.TEXT
      })
      
      newMessage.value = ''
      await loadMessages()
    }
    
    await nextTick()
    scrollToBottom()
    
  } catch (err: any) {
    console.error('メッセージ送信エラー:', err)
    error.value = err.response?.data?.message || 'メッセージの送信に失敗しました'
  } finally {
    isSendingMessage.value = false
  }
}

const scrollToBottom = () => {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    hasNewMessages.value = false
  }
}

const handleScroll = () => {
  if (!messageContainer.value) return
  
  const { scrollTop, scrollHeight, clientHeight } = messageContainer.value
  const isAtBottom = scrollHeight - scrollTop - clientHeight < 100
  
  if (isAtBottom) {
    hasNewMessages.value = false
  }
}

const goBack = () => {
  router.push(`/matching/${props.requestId}`)
}

// Polling setup
const startPolling = () => {
  pollingInterval = setInterval(async () => {
    if (!isLoading.value) {
      await loadMessages()
    }
  }, 2000) // 2秒間隔
}

const stopPolling = () => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

// WebSocket setup
const setupWebSocketListeners = () => {
  // WebSocket接続
  stompWebSocketService.connect().then(() => {
    isWebSocketConnected.value = true
    console.log('🔌 WebSocket connected for chat room')
    
    // チャットルームに参加
    if (chatRoom.value) {
      const currentUser = authStore.user
      const currentProfile = profileStore.profile
      if (currentUser && currentProfile) {
        stompWebSocketService.subscribeToChatRoom(chatRoom.value.id)
        stompWebSocketService.joinChatRoom(chatRoom.value.id, currentUser.id, currentProfile.displayName)
      }
    }
  }).catch((error: any) => {
    console.error('🚨 WebSocket connection failed:', error)
    isWebSocketConnected.value = false
    // フォールバック：ポーリングを開始
    startPolling()
  })

  // 新しいメッセージイベント
  const newMessageHandler = (data: any) => {
    if (chatRoom.value && data.chatRoomId === chatRoom.value.id) {
      console.log('💬 New message received:', data)
      
      // メッセージを追加
      messages.value.push(data)
      hasNewMessages.value = true
      
      // 自動スクロール（画面下部にいる場合のみ）
      nextTick(() => {
        if (messageContainer.value) {
          const { scrollTop, scrollHeight, clientHeight } = messageContainer.value
          const isAtBottom = scrollHeight - scrollTop - clientHeight < 100
          if (isAtBottom) {
            scrollToBottom()
          }
        }
      })
    }
  }

  // ユーザー入室イベント
  const userJoinedChatHandler = (data: any) => {
    if (chatRoom.value && data.chatRoomId === chatRoom.value.id) {
      console.log('💬 User joined chat:', data.participant)
      // チャットルーム情報を再取得（参加者リスト更新のため）
      loadChatRoom()
    }
  }

  // ユーザー退室イベント
  const userLeftChatHandler = (data: any) => {
    if (chatRoom.value && data.chatRoomId === chatRoom.value.id) {
      console.log('💬 User left chat:', data.userId)
      // チャットルーム情報を再取得（参加者リスト更新のため）
      loadChatRoom()
    }
  }

  // タイピング開始イベント
  const typingStartHandler = (data: any) => {
    if (chatRoom.value && data.chatRoomId === chatRoom.value.id) {
      typingUsers.value.add(data.displayName)
    }
  }

  // タイピング終了イベント
  const typingStopHandler = (data: any) => {
    if (chatRoom.value && data.chatRoomId === chatRoom.value.id) {
      typingUsers.value.delete(data.displayName)
    }
  }

  // エラーイベント
  const errorHandler = (data: any) => {
    console.error('🚨 WebSocket error:', data)
    error.value = data.message
  }

  // STOMP イベントリスナー登録
  stompWebSocketService.on('onNewMessage', newMessageHandler)
  stompWebSocketService.on('onUserJoinedChat', userJoinedChatHandler)
  stompWebSocketService.on('onUserLeftChat', userLeftChatHandler)
  stompWebSocketService.on('onTypingStart', typingStartHandler)
  stompWebSocketService.on('onTypingStop', typingStopHandler)
  stompWebSocketService.on('onError', errorHandler)
}

const cleanupWebSocketListeners = () => {
  // チャットルームから退出
  if (stompWebSocketService.isConnected() && chatRoom.value) {
    const currentUser = authStore.user
    const currentProfile = profileStore.profile
    if (currentUser && currentProfile) {
      stompWebSocketService.leaveChatRoom(chatRoom.value.id, currentUser.id, currentProfile.displayName)
    }
    stompWebSocketService.unsubscribeFromChatRoom(chatRoom.value.id)
  }

  // WebSocket切断
  stompWebSocketService.disconnect()
  isWebSocketConnected.value = false
}

// Watchers
watch(showParticipants, (newValue) => {
  if (newValue && window.innerWidth < 1024) {
    showParticipantsModal.value = true
    showParticipants.value = false
  }
})

// Lifecycle
onMounted(async () => {
  await loadChatRoom()
  if (chatRoom.value) {
    await loadMessages()
    await nextTick()
    scrollToBottom()
    setupWebSocketListeners()
  }
})

onUnmounted(() => {
  stopPolling()
  cleanupWebSocketListeners()
})
</script>

<style scoped>
/* カスタムスクロールバー */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* アニメーション */
.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* レスポンシブデザイン */
@media (max-width: 640px) {
  .max-w-xs {
    max-width: 280px;
  }
}
</style> 