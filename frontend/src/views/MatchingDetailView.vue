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
          <h1 class="text-2xl font-bold text-gray-900 mb-2">募集詳細</h1>
          <p class="text-gray-600">募集の詳細情報を確認できます</p>
        </div>
      </div>

      <!-- ローディング状態 -->
      <div v-if="isLoading && !requestData" class="text-center py-12">
        <n-spin size="medium">
          <template #description>
            <div class="text-gray-600 mt-3">募集情報を取得中...</div>
          </template>
        </n-spin>
      </div>

      <!-- エラーアラート -->
      <n-alert 
        v-else-if="error" 
        type="error" 
        :show-icon="true"
        class="mb-6"
        closable
        @close="error = null"
      >
        {{ error }}
      </n-alert>

      <!-- メインコンテンツ -->
      <div v-else-if="requestData" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 募集詳細カード -->
        <div class="lg:col-span-2">
          <n-card class="card-feature mb-6" :bordered="true">
            <template #header>
              <div class="flex items-center justify-between">
                <h2 class="text-xl font-semibold text-gray-900">募集情報</h2>
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
                <span class="text-2xl mr-3">{{ getAttractionEmoji(requestData.attraction) }}</span>
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
                    type="primary"
                    @click="goToRoom"
                    class="btn-primary"
                  >
                    <template #icon>
                      <div class="i-carbon-home"></div>
                    </template>
                    待機室へ
                  </n-button>

                  <n-button
                    @click="editRequest"
                    class="btn-secondary"
                  >
                    <template #icon>
                      <div class="i-carbon-edit"></div>
                    </template>
                    募集を編集
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
                    @click="showApplicationModal = true"
                    class="btn-primary"
                  >
                    <template #icon>
                      <div class="i-carbon-user-follow"></div>
                    </template>
                    応募する
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
                    type="primary"
                    @click="goToRoom"
                    class="btn-primary"
                  >
                    <template #icon>
                      <div class="i-carbon-home"></div>
                    </template>
                    待機室へ
                  </n-button>

                  <n-button
                    v-if="userApplication"
                    @click="goToRoom"
                    class="btn-secondary"
                  >
                    <template #icon>
                      <div class="i-carbon-view"></div>
                    </template>
                    待機室を見る
                  </n-button>
                </n-space>
              </div>
            </div>
          </n-card>
        </div>

        <!-- 参加者一覧カード -->
        <div class="lg:col-span-1">
          <n-card class="card-feature" :bordered="true">
            <template #header>
              <h3 class="text-lg font-semibold text-gray-900">参加者一覧</h3>
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

              <!-- 応募中の参加者 -->
              <div v-for="application in pendingApplications" :key="application.id" 
                   class="flex items-center p-3 bg-blue-50 rounded-lg">
                <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center mr-3">
                  <span class="text-white text-sm font-bold">{{ application.applicantDisplayName.charAt(0) }}</span>
                </div>
                <div class="flex-1">
                  <div class="font-medium text-gray-900">{{ application.applicantDisplayName }}</div>
                  <div class="text-sm text-blue-600">応募中</div>
                </div>
                <div class="w-2 h-2 bg-blue-500 rounded-full"></div>
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

      <!-- Application Modal -->
      <ApplicationModal 
        v-if="showApplicationModal"
        :request="requestData"
        @close="showApplicationModal = false"
        @applied="handleApplicationSuccess"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMatchingStore } from '@/stores/matching'
import { useAuthStore } from '@/stores/auth'
import type { MatchingRequestWithActions, MatchingApplicationDto } from '@/types'
import { MatchingApplicationStatus } from '@/types'
import { matchingApi, matchingApplicationApi } from '@/services'
import ApplicationModal from '@/components/matching/ApplicationModal.vue'

const route = useRoute()
const router = useRouter()
const matchingStore = useMatchingStore()
const authStore = useAuthStore()

// State
const requestData = ref<MatchingRequestWithActions | null>(null)
const applications = ref<MatchingApplicationDto[]>([])
const error = ref<string | null>(null)
const isLoading = ref(false)
const isWithdrawing = ref(false)
const showApplicationModal = ref(false)

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
  1 + acceptedApplications.value.length + pendingApplications.value.length
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
  return emojiMap[attraction] || '🎢'
}

// Data loading
const loadData = async () => {
  try {
    isLoading.value = true
    error.value = null

    const requestId = parseInt(route.params.id as string)
    
    // 募集詳細と応募一覧を並行取得
    const [requestResponse, applicationsResponse] = await Promise.all([
      matchingApi.getMatchingRequestWithActions(requestId),
      matchingApplicationApi.getApplicationsForRequest(requestId)
    ])

    requestData.value = requestResponse
    applications.value = applicationsResponse.data.data || []

  } catch (err: any) {
    console.error('募集詳細データ取得エラー:', err)
    error.value = err.response?.data?.message || '募集詳細の取得に失敗しました'
  } finally {
    isLoading.value = false
  }
}

// Actions
const goToRoom = () => {
  const requestId = parseInt(route.params.id as string)
  router.push(`/matching/${requestId}/room`)
}

const editRequest = () => {
  // TODO: 募集編集画面への遷移
  console.log('募集編集機能は未実装です')
}

const withdrawApplication = async () => {
  if (!userApplication.value) return
  
  try {
    isWithdrawing.value = true
    await matchingApplicationApi.withdrawApplication(parseInt(route.params.id as string))
    await loadData()
  } catch (err) {
    console.error('応募取り消しエラー:', err)
  } finally {
    isWithdrawing.value = false
  }
}

const handleApplicationSuccess = () => {
  showApplicationModal.value = false
  loadData() // データ再取得
}

// Initialize
onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* レスポンシブデザイン */
@media (max-width: 768px) {
  .container-app {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style> 