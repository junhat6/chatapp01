<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container-app py-6">
      <!-- ヘッダー -->
      <div class="mb-6">
        <n-button 
          text 
          @click="$router.push('/matching')"
          class="mb-4 text-gray-600 hover:text-primary-600"
        >
          <template #icon>
            <div class="i-carbon-arrow-left text-lg"></div>
          </template>
          募集一覧に戻る
        </n-button>
        
        <div class="mb-4">
          <h1 class="text-2xl font-bold text-gray-900 mb-2">待機室</h1>
          <p class="text-gray-600">参加者が集まるのを待っています</p>
        </div>
      </div>

      <!-- ローディング状態 -->
      <div v-if="isLoading && !requestData" class="text-center py-12">
        <n-spin size="medium">
          <template #description>
            <div class="text-gray-600 mt-3">待機室情報を取得中...</div>
          </template>
        </n-spin>
      </div>

      <!-- エラーアラート -->
      <n-alert 
        v-if="error" 
        type="error" 
        :show-icon="true"
        class="mb-6"
        closable
        @close="error = null"
      >
        {{ error }}
      </n-alert>

      <!-- 成功メッセージ -->
      <n-alert 
        v-if="successMessage" 
        type="success" 
        :show-icon="true"
        class="mb-6"
        closable
        @close="successMessage = null"
      >
        {{ successMessage }}
      </n-alert>

      <!-- リトライ可能エラー -->
      <n-alert 
        v-if="retryableError" 
        type="warning" 
        :show-icon="true"
        class="mb-6"
        closable
        @close="retryableError = null"
      >
        {{ retryableError }}
        <template #action>
          <n-button size="small" @click="loadRoomData()">
            再試行
          </n-button>
        </template>
      </n-alert>

      <!-- 楽観的更新中の表示 -->
      <n-alert 
        v-if="isOptimisticUpdate" 
        type="info" 
        :show-icon="true"
        class="mb-6"
      >
        <template #icon>
          <n-spin size="small" />
        </template>
        リアルタイム更新中...
      </n-alert>

      <!-- メインコンテンツ -->
      <div v-else-if="requestData" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 募集詳細カード -->
        <div class="lg:col-span-2">
          <n-card class="card-feature mb-6" :bordered="true">
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-xl font-semibold text-gray-900">募集詳細</h2>
                <n-tag
                  :type="getStatusTagType(requestData.status)"
                  :bordered="false"
                  size="medium"
                >
                  {{ getStatusText(requestData.status) }}
                </n-tag>
              </div>
            </template>

            <div class="space-y-4">
              <!-- アトラクション -->
              <div class="flex items-center">
                <span class="text-2xl mr-3">🎢</span>
                <div>
                  <div class="text-sm text-gray-600">アトラクション</div>
                  <div class="font-medium text-gray-900">{{ requestData.attraction }}</div>
                </div>
              </div>

              <!-- 希望日時 -->
              <div class="flex items-center">
                <span class="text-2xl mr-3">🕒</span>
                <div>
                  <div class="text-sm text-gray-600">希望日時</div>
                  <div class="font-medium text-gray-900">{{ formatDateTime(requestData.preferredDateTime) }}</div>
                </div>
              </div>

              <!-- ホスト -->
              <div class="flex items-center">
                <span class="text-2xl mr-3">👤</span>
                <div>
                  <div class="text-sm text-gray-600">ホスト</div>
                  <div class="font-medium text-gray-900">{{ requestData.hostDisplayName }}</div>
                </div>
              </div>

              <!-- 募集人数 -->
              <div class="flex items-center">
                <span class="text-2xl mr-3">👥</span>
                <div class="flex-1">
                  <div class="text-sm text-gray-600 mb-2">募集人数</div>
                  <div class="flex items-center justify-between mb-2">
                    <span class="font-medium text-primary-600">
                      {{ currentParticipants }}/{{ requestData.maxParticipants }}人
                    </span>
                    <span class="text-sm text-gray-500">
                      {{ Math.round((currentParticipants / requestData.maxParticipants) * 100) }}%
                    </span>
                  </div>
                  <n-progress
                    type="line"
                    :percentage="(currentParticipants / requestData.maxParticipants) * 100"
                    :color="getProgressColor(currentParticipants, requestData.maxParticipants)"
                    :height="6"
                    :border-radius="3"
                    :fill-border-radius="3"
                  />
                </div>
              </div>

              <!-- 説明 -->
              <div v-if="requestData.description">
                <div class="text-sm text-gray-600 mb-2">メッセージ</div>
                <div class="bg-gray-50 rounded-lg p-3 text-gray-700">
                  {{ requestData.description }}
                </div>
              </div>
            </div>
          </n-card>

          <!-- アクションボタン -->
          <n-card class="card-feature" :bordered="true">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900">アクション</h3>
            </template>

            <div class="space-y-3">
              <!-- ホスト用ボタン -->
              <div v-if="isHost">
                <div class="text-sm text-gray-600 mb-3">ホスト操作</div>
                <n-space>
                  <n-button
                    v-if="canConfirmRoom"
                    type="primary"
                    @click="confirmRoom"
                    :loading="isConfirming"
                    class="btn-primary"
                  >
                    <template #icon>
                      <div class="i-carbon-checkmark"></div>
                    </template>
                    {{ isConfirming ? '確定中...' : '募集を確定する' }}
                  </n-button>

                  <n-button
                    v-if="canCancelRoom"
                    @click="cancelRoom"
                    :loading="isCancelling"
                    class="btn-secondary"
                  >
                    <template #icon>
                      <div class="i-carbon-close"></div>
                    </template>
                    {{ isCancelling ? 'キャンセル中...' : '募集をキャンセル' }}
                  </n-button>
                </n-space>
              </div>

              <!-- 参加者用ボタン -->
              <div v-else>
                <div class="text-sm text-gray-600 mb-3">参加者操作</div>
                <n-space>
                  <n-button
                    v-if="!userApplication && canApply"
                    type="primary"
                    @click="applyToRoom"
                    :loading="isApplying"
                    class="btn-primary"
                  >
                    <template #icon>
                      <div class="i-carbon-user-follow"></div>
                    </template>
                    {{ isApplying ? '応募中...' : '応募する' }}
                  </n-button>

                  <n-button
                    v-if="userApplication && userApplication.status === 'PENDING'"
                    @click="withdrawApplication"
                    :loading="isWithdrawing"
                    class="btn-secondary"
                  >
                    <template #icon>
                      <div class="i-carbon-user-unfollow"></div>
                    </template>
                    {{ isWithdrawing ? '取り消し中...' : '応募を取り消す' }}
                  </n-button>

                  <n-button
                    v-if="userApplication && userApplication.status === 'ACCEPTED'"
                    @click="leaveRoom"
                    :loading="isLeaving"
                    class="btn-secondary"
                  >
                    <template #icon>
                      <div class="i-carbon-logout"></div>
                    </template>
                    {{ isLeaving ? '退出中...' : '待機室を退出' }}
                  </n-button>
                </n-space>
              </div>

              <!-- ステータス表示 -->
              <div v-if="userApplication" class="mt-4 p-3 bg-gray-50 rounded-lg">
                <div class="text-sm font-medium text-gray-700">
                  あなたの応募状況: 
                  <n-tag
                    :type="getApplicationStatusTagType(userApplication.status)"
                    :bordered="false"
                    size="small"
                    class="ml-2"
                  >
                    {{ getApplicationStatusText(userApplication.status) }}
                  </n-tag>
                </div>
              </div>
            </div>
          </n-card>
        </div>

        <!-- 参加者一覧 -->
        <div class="lg:col-span-1">
          <n-card class="card-feature" :bordered="true">
            <template #header>
              <div class="flex items-center justify-between">
                <div>
                  <h3 class="text-lg font-semibold text-gray-900">参加者一覧</h3>
                  <div class="text-xs text-gray-500 mt-1">
                    最終更新: {{ formatUpdateTime(lastUpdateTime) }}
                  </div>
                </div>
                <div class="flex flex-col items-end space-y-1">
                  <div class="flex items-center space-x-2">
                    <div 
                      class="w-2 h-2 rounded-full"
                      :class="isWebSocketConnected ? 'bg-green-500 animate-pulse' : 'bg-yellow-500'"
                    ></div>
                    <span class="text-sm text-gray-500">
                      {{ isWebSocketConnected ? 'リアルタイム更新' : 'ポーリング更新' }}
                    </span>
                  </div>
                  <div v-if="pendingActions.size > 0" class="flex items-center space-x-1">
                    <n-spin size="tiny" />
                    <span class="text-xs text-gray-400">処理中...</span>
                  </div>
                </div>
              </div>
            </template>

            <div class="space-y-3">
              <!-- ホスト -->
              <div class="flex items-center p-3 bg-primary-50 rounded-lg">
                <div class="w-8 h-8 bg-primary-500 rounded-full flex items-center justify-center mr-3">
                  <span class="text-white text-sm font-bold">H</span>
                </div>
                <div class="flex-1">
                  <div class="font-medium text-gray-900">{{ requestData.hostDisplayName }}</div>
                  <div class="text-sm text-primary-600">ホスト</div>
                </div>
                <div class="w-2 h-2 bg-primary-500 rounded-full"></div>
              </div>

              <!-- 承認された参加者 -->
              <div v-for="application in acceptedApplications" :key="application.id" 
                   class="flex items-center p-3 bg-green-50 rounded-lg">
                <div class="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center mr-3">
                  <span class="text-white text-sm font-bold">{{ application.applicantDisplayName.charAt(0) }}</span>
                </div>
                <div class="flex-1">
                  <div class="font-medium text-gray-900">{{ application.applicantDisplayName }}</div>
                  <div class="text-sm text-green-600">参加中</div>
                </div>
                <div class="w-2 h-2 bg-green-500 rounded-full"></div>
              </div>

              <!-- 応募中の参加者（承認なしで参加） -->
              <div v-for="application in pendingApplications" :key="application.id" 
                   class="flex items-center p-3 bg-blue-50 rounded-lg">
                <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center mr-3">
                  <span class="text-white text-sm font-bold">{{ application.applicantDisplayName.charAt(0) }}</span>
                </div>
                <div class="flex-1">
                  <div class="font-medium text-gray-900">{{ application.applicantDisplayName }}</div>
                  <div class="text-sm text-blue-600">応募中</div>
                </div>
                <div v-if="isHost" class="flex space-x-1">
                  <n-button
                    size="tiny"
                    type="primary"
                    @click="approveApplication(application.id)"
                    class="btn-primary"
                  >
                    承認
                  </n-button>
                  <n-button
                    size="tiny"
                    @click="rejectApplication(application.id)"
                    class="btn-secondary"
                  >
                    拒否
                  </n-button>
                </div>
                <div v-else class="w-2 h-2 bg-blue-500 rounded-full"></div>
              </div>

              <!-- 空きスロット -->
              <div v-for="n in emptySlots" :key="`empty-${n}`" 
                   class="flex items-center p-3 bg-gray-50 rounded-lg">
                <div class="w-8 h-8 bg-gray-300 rounded-full flex items-center justify-center mr-3">
                  <span class="text-gray-500 text-sm">?</span>
                </div>
                <div class="flex-1">
                  <div class="text-gray-500">参加者を募集中...</div>
                </div>
              </div>
            </div>
          </n-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMatchingStore } from '@/stores/matching'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import type { MatchingRequestWithActions, MatchingApplicationDto } from '@/types'
import { MatchingApplicationStatus } from '@/types'
import { matchingApi, matchingApplicationApi, matchingRoomApi } from '@/services'
import { stompWebSocketService } from '@/services/stompWebSocketService'
import type { 
  MatchingRequestDto,
  MatchingRoomDto,
  ParticipantDto,
  RoomStateUpdateMessage,
  JoinRoomMessage,
  LeaveRoomMessage,
  ConfirmRoomMessage,
  DisbandRoomMessage
} from '@/types'

interface Props {
  requestId: number
}

const props = defineProps<Props>()
const route = useRoute()
const router = useRouter()
const matchingStore = useMatchingStore()
const authStore = useAuthStore()
const profileStore = useProfileStore()

// State
const requestData = ref<MatchingRequestWithActions | null>(null)
const applications = ref<MatchingApplicationDto[]>([])
const error = ref<string | null>(null)
const isLoading = ref(false)

// Action states
const isConfirming = ref(false)
const isCancelling = ref(false)
const isApplying = ref(false)
const isWithdrawing = ref(false)
const isLeaving = ref(false)

// WebSocket event handlers
const websocketEventHandlers = ref<Array<{ event: string, handler: Function }>>([])

// WebSocketイベント状態
const isWebSocketConnected = ref(false)

// 最適化: リアルタイム状態管理
const realtimeParticipants = ref<ParticipantDto[]>([])
const lastUpdateTime = ref<Date>(new Date())
const isOptimisticUpdate = ref(false)
const pendingActions = ref<Set<string>>(new Set())

// 成功・エラー通知状態
const successMessage = ref<string | null>(null)
const retryableError = ref<string | null>(null)

// Computed
const isHost = computed(() => 
  requestData.value && requestData.value.hostUserId === authStore.user?.id
)

const userApplication = computed(() => 
  requestData.value?.userApplication || null
)

const acceptedApplications = computed(() => 
  applications.value.filter(app => app.status === MatchingApplicationStatus.ACCEPTED)
)

const pendingApplications = computed(() => 
  applications.value.filter(app => app.status === MatchingApplicationStatus.PENDING)
)

const currentParticipants = computed(() => 
  1 + acceptedApplications.value.length + pendingApplications.value.length // ホスト + 承認された参加者 + 応募中の参加者
)

const emptySlots = computed(() => {
  if (!requestData.value) return 0
  return Math.max(0, requestData.value.maxParticipants - currentParticipants.value)
})

const canApply = computed(() => 
  requestData.value && 
  requestData.value.status === 'OPEN' && 
  currentParticipants.value < requestData.value.maxParticipants &&
  !isHost.value &&
  !userApplication.value
)

const canConfirmRoom = computed(() => 
  isHost.value && 
  requestData.value?.status === 'WAITING' &&
  currentParticipants.value >= 2
)

const canCancelRoom = computed(() => 
  isHost.value && 
  (requestData.value?.status === 'OPEN' || requestData.value?.status === 'WAITING')
)

// Utility functions
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'OPEN': return 'success'
    case 'WAITING': return 'info'
    case 'CONFIRMED': return 'warning'
    case 'CLOSED': return 'default'
    case 'EXPIRED': return 'error'
    default: return 'default'
  }
}

const getStatusText = (status: string): string => {
  switch (status) {
    case 'OPEN': return '募集中'
    case 'WAITING': return '待機中'
    case 'CONFIRMED': return '確定済み'
    case 'CLOSED': return '終了'
    case 'EXPIRED': return '期限切れ'
    default: return '不明'
  }
}

const getApplicationStatusTagType = (status: string) => {
  switch (status) {
    case MatchingApplicationStatus.PENDING: return 'warning'
    case MatchingApplicationStatus.ACCEPTED: return 'success'
    case MatchingApplicationStatus.REJECTED: return 'error'
    default: return 'default'
  }
}

const getApplicationStatusText = (status: string): string => {
  switch (status) {
    case MatchingApplicationStatus.PENDING: return '承認待ち'
    case MatchingApplicationStatus.ACCEPTED: return '承認済み'
    case MatchingApplicationStatus.REJECTED: return '拒否済み'
    default: return '不明'
  }
}

const getProgressColor = (current: number, max: number): string => {
  const percentage = (current / max) * 100
  if (percentage >= 100) return '#22c55e'
  if (percentage >= 75) return '#f59e0b'
  if (percentage >= 50) return '#3b82f6'
  return '#8b5cf6'
}

const formatDateTime = (dateTime: string): string => {
  const date = new Date(dateTime)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  const weekdays = ['日', '月', '火', '水', '木', '金', '土']
  const weekday = weekdays[date.getDay()]
  return `${month}/${day}(${weekday}) ${hours}:${minutes}`
}

const formatUpdateTime = (time: Date): string => {
  const now = new Date()
  const diffMs = now.getTime() - time.getTime()
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  
  if (diffSeconds < 30) {
    return 'たった今'
  } else if (diffSeconds < 60) {
    return `${diffSeconds}秒前`
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分前`
  } else {
    const hours = time.getHours().toString().padStart(2, '0')
    const minutes = time.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}`
  }
}

// Data loading
const loadRoomData = async () => {
  try {
    isLoading.value = true
    error.value = null

    // 募集詳細と応募一覧を並行取得
    const [requestResponse, applicationsResponse] = await Promise.all([
      matchingApi.getMatchingRequestWithActions(props.requestId),
      matchingApplicationApi.getApplicationsForRequest(props.requestId)
    ])

    requestData.value = requestResponse
    applications.value = applicationsResponse.data.data || []

  } catch (err: any) {
    console.error('待機室データ取得エラー:', err)
    error.value = err.response?.data?.message || '待機室データの取得に失敗しました'
  } finally {
    isLoading.value = false
  }
}

// Actions
const applyToRoom = async () => {
  try {
    isApplying.value = true
    await matchingStore.applyToRequest(props.requestId, {
      message: '参加させてください！'
    })
    await loadRoomData() // データ再取得
  } catch (err) {
    console.error('応募エラー:', err)
  } finally {
    isApplying.value = false
  }
}

const withdrawApplication = async () => {
  if (!userApplication.value) return
  
  try {
    isWithdrawing.value = true
    await matchingApplicationApi.updateApplicationStatus(
      userApplication.value.id, 
      { status: MatchingApplicationStatus.REJECTED }
    )
    await loadRoomData()
  } catch (err) {
    console.error('応募取り消しエラー:', err)
  } finally {
    isWithdrawing.value = false
  }
}

const leaveRoom = async () => {
  try {
    isLeaving.value = true
    await matchingRoomApi.leaveMatchingRoom(props.requestId)
    await loadRoomData()
  } catch (err) {
    console.error('退出エラー:', err)
  } finally {
    isLeaving.value = false
  }
}

const confirmRoom = async () => {
  if (!requestData.value || !authStore.user) return

  const actionId = 'confirm-room'
  
  const confirmAction = async () => {
    isConfirming.value = true
    
    // STOMPでの確定リクエスト送信
    if (stompWebSocketService.isConnected()) {
      stompWebSocketService.confirmRoom(props.requestId, authStore.user!.id)
      showSuccessMessage('募集確定リクエストを送信中...')
    } else {
      throw new Error('WebSocket接続が確立されていません。ページを再読み込みしてください。')
    }
  }

  try {
    await trackPendingAction(actionId, confirmAction())
    console.log('募集確定リクエストを送信しました')
  } catch (err) {
    console.error('募集確定エラー:', err)
    const message = err instanceof Error ? err.message : '募集確定に失敗しました'
    showRetryableError(message, confirmRoom)
  } finally {
    isConfirming.value = false
  }
}

const cancelRoom = async () => {
  if (!requestData.value || !authStore.user) return

  const actionId = 'cancel-room'
  
  const cancelAction = async () => {
    isCancelling.value = true
    
    // STOMPでの解散リクエスト送信
    if (stompWebSocketService.isConnected()) {
      stompWebSocketService.disbandRoom(props.requestId, authStore.user!.id)
      showSuccessMessage('募集解散リクエストを送信中...')
    } else {
      throw new Error('WebSocket接続が確立されていません。ページを再読み込みしてください。')
    }
  }

  try {
    await trackPendingAction(actionId, cancelAction())
    console.log('募集解散リクエストを送信しました')
  } catch (err) {
    console.error('募集解散エラー:', err)
    const message = err instanceof Error ? err.message : '募集解散に失敗しました'
    showRetryableError(message, cancelRoom)
  } finally {
    isCancelling.value = false
  }
}

const approveApplication = async (applicationId: number) => {
  try {
    await matchingApplicationApi.updateApplicationStatus(applicationId, {
      status: MatchingApplicationStatus.ACCEPTED
    })
    await loadRoomData()
  } catch (err) {
    console.error('承認エラー:', err)
  }
}

const rejectApplication = async (applicationId: number) => {
  try {
    await matchingApplicationApi.updateApplicationStatus(applicationId, {
      status: MatchingApplicationStatus.REJECTED
    })
    await loadRoomData()
  } catch (err) {
    console.error('拒否エラー:', err)
  }
}

// WebSocket setup
const setupWebSocketListeners = () => {
  // STOMP WebSocket接続
  stompWebSocketService.connect().then(() => {
    isWebSocketConnected.value = true
    console.log('🔌 STOMP WebSocket connected for matching room')
    
    // 待機室にサブスクライブ
    stompWebSocketService.subscribeToRoom(props.requestId)
    
    // ユーザー情報を取得してから参加処理
    const currentUser = authStore.user
    const currentProfile = profileStore.profile
    if (currentUser && currentProfile) {
      stompWebSocketService.joinRoom(props.requestId, currentUser.id, currentProfile.displayName)
    }
  }).catch((error) => {
    console.error('🚨 STOMP WebSocket connection failed:', error)
    isWebSocketConnected.value = false
    // フォールバック：ポーリングを開始
    startFallbackPolling()
  })

  // STOMPイベントコールバック設定（最適化版）
  stompWebSocketService.on('onRoomStateUpdate', (data: RoomStateUpdateMessage) => {
    if (data.requestId === props.requestId) {
      console.log('🏠 Room state updated:', data)
      // リアルタイムデータを直接更新（API呼び出し削減）
      updateRealtimeState(data)
      lastUpdateTime.value = new Date()
      showSuccessMessage('参加者状況が更新されました')
    }
  })

  stompWebSocketService.on('onJoinRoom', (data: JoinRoomMessage) => {
    if (data.requestId === props.requestId) {
      console.log('👤 User joined room:', data)
      // 楽観的更新：即座にUIを更新
      addParticipantOptimistically(data)
      showSuccessMessage(`${data.userDisplayName}さんが参加しました`)
    }
  })

  stompWebSocketService.on('onLeaveRoom', (data: LeaveRoomMessage) => {
    if (data.requestId === props.requestId) {
      console.log('👤 User left room:', data)
      // 楽観的更新：即座にUIを更新
      removeParticipantOptimistically(data)
      showSuccessMessage(`${data.userDisplayName}さんが退出しました`)
    }
  })

  stompWebSocketService.on('onConfirmRoom', (data: ConfirmRoomMessage) => {
    if (data.requestId === props.requestId) {
      console.log('✅ Room confirmed, redirecting to chat')
      showSuccessMessage('募集が確定されました！チャットルームに移動します...')
      // スムーズな遷移のため少し遅延
      setTimeout(() => {
        router.push(`/chat/${props.requestId}`)
      }, 1500)
    }
  })

  stompWebSocketService.on('onDisbandRoom', (data: DisbandRoomMessage) => {
    if (data.requestId === props.requestId) {
      console.log('❌ Room disbanded')
      showSuccessMessage('募集が解散されました。募集一覧に戻ります...')
      // スムーズな遷移のため少し遅延
      setTimeout(() => {
        router.push('/matching')
      }, 1500)
    }
  })

  stompWebSocketService.on('onError', (errorMessage: string) => {
    console.error('🚨 STOMP WebSocket error:', errorMessage)
    error.value = errorMessage
  })
}

const cleanupWebSocketListeners = () => {
  // 待機室のサブスクリプション解除
  stompWebSocketService.unsubscribeFromRoom(props.requestId)
  
  // ユーザー情報を取得してから退出処理
  const currentUser = authStore.user
  const currentProfile = profileStore.profile
  if (currentUser && currentProfile && stompWebSocketService.isConnected()) {
    stompWebSocketService.leaveRoom(props.requestId, currentUser.id, currentProfile.displayName)
  }

  // WebSocket切断
  stompWebSocketService.disconnect()
  isWebSocketConnected.value = false
}

// フォールバック：ポーリング（WebSocket接続失敗時）
let fallbackPollingInterval: NodeJS.Timeout | null = null

const startFallbackPolling = () => {
  console.log('🔄 Starting fallback polling (WebSocket failed)')
  fallbackPollingInterval = setInterval(async () => {
    if (!isLoading.value) {
      await loadRoomData()
    }
  }, 3000) // 3秒間隔
}

const stopFallbackPolling = () => {
  if (fallbackPollingInterval) {
    clearInterval(fallbackPollingInterval)
    fallbackPollingInterval = null
  }
}

// === 最適化ヘルパー関数 ===

// リアルタイム状態更新
const updateRealtimeState = (data: RoomStateUpdateMessage) => {
  if (data.participants) {
    realtimeParticipants.value = data.participants
  }
  
  // 状態が変更された場合、必要最小限の更新のみ実行
  if (requestData.value && data.currentCount !== undefined) {
    const updatedData = { ...requestData.value }
    // 参加者数の更新など最小限のデータのみ
    requestData.value = updatedData
  }
}

// 楽観的参加者追加
const addParticipantOptimistically = (data: JoinRoomMessage) => {
  isOptimisticUpdate.value = true
  
  // 既存参加者に新規参加者を追加
  const newParticipant: ParticipantDto = {
    userId: data.userId,
    displayName: data.userDisplayName,
    isHost: false
  }
  
  realtimeParticipants.value = [...realtimeParticipants.value, newParticipant]
  
  // 楽観的更新の状態をリセット
  setTimeout(() => {
    isOptimisticUpdate.value = false
  }, 1000)
}

// 楽観的参加者削除
const removeParticipantOptimistically = (data: LeaveRoomMessage) => {
  isOptimisticUpdate.value = true
  
  // 該当参加者を削除
  realtimeParticipants.value = realtimeParticipants.value.filter(
    participant => participant.userId !== data.userId
  )
  
  // 楽観的更新の状態をリセット
  setTimeout(() => {
    isOptimisticUpdate.value = false
  }, 1000)
}

// 成功メッセージ表示
const showSuccessMessage = (message: string) => {
  successMessage.value = message
  retryableError.value = null // エラーをクリア
  
  // 3秒後に自動非表示
  setTimeout(() => {
    successMessage.value = null
  }, 3000)
}

// エラーメッセージ表示（リトライ可能）
const showRetryableError = (message: string, retryAction?: () => void) => {
  retryableError.value = message
  successMessage.value = null // 成功メッセージをクリア
  
  // リトライアクションを保存（必要に応じて）
  if (retryAction) {
    // リトライ機能の実装は今後追加
  }
}

// アクション実行中の状態管理
const trackPendingAction = (actionId: string, promise: Promise<any>) => {
  pendingActions.value.add(actionId)
  
  return promise.finally(() => {
    pendingActions.value.delete(actionId)
  })
}

// Lifecycle
onMounted(async () => {
  await loadRoomData()
  setupWebSocketListeners()
})

onUnmounted(() => {
  cleanupWebSocketListeners()
  stopFallbackPolling()
})
</script>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}

/* レスポンシブデザイン */
@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style> 