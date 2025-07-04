<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container-app py-6">
      <!-- ヘッダー -->
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900 mb-1">募集一覧</h1>
          <p class="text-gray-600">参加したい募集を見つけよう</p>
        </div>
        
        <n-button 
          type="primary"
          size="medium"
          @click="$router.push('/matching/create')"
          class="btn-primary"
        >
          新しい募集を作成
        </n-button>
      </div>

      <!-- 検索・フィルターカード -->
      <n-card class="mb-6 card-simple" :bordered="true">
        <n-grid :cols="4" :x-gap="12" :y-gap="12" responsive="screen">
          <n-grid-item :span="24" :md="6">
            <n-form-item label="アトラクション">
              <n-select
                v-model:value="searchForm.attraction"
                placeholder="すべて"
                :options="attractionFilterOptions"
                clearable
                @update:value="handleSearch"
                size="medium"
              />
            </n-form-item>
          </n-grid-item>
          
          <n-grid-item :span="24" :md="6">
            <n-form-item label="日付">
              <n-date-picker
                v-model:value="searchDateValue"
                type="date"
                placeholder="日付を選択"
                clearable
                :is-date-disabled="isDateDisabled"
                @update:value="handleDateSearch"
                size="medium"
                class="w-full"
              />
            </n-form-item>
          </n-grid-item>
          
          <n-grid-item :span="24" :md="6">
            <n-form-item label="募集人数">
              <n-select
                v-model:value="searchForm.maxParticipants"
                placeholder="すべて"
                :options="participantOptions"
                clearable
                @update:value="handleSearch"
                size="medium"
              />
            </n-form-item>
          </n-grid-item>
          
          <n-grid-item :span="24" :md="6" class="flex items-end">
            <n-button
              @click="resetSearch"
              size="medium"
              class="w-full btn-secondary"
              :disabled="isLoading"
            >
              リセット
            </n-button>
          </n-grid-item>
        </n-grid>
      </n-card>

      <!-- ローディング状態 -->
      <div v-if="isLoading" class="text-center py-12">
        <n-spin size="medium">
          <template #description>
            <div class="text-gray-600 mt-3">募集を検索中...</div>
          </template>
        </n-spin>
      </div>

      <!-- エラーアラート -->
      <n-alert 
        v-else-if="matchingStore.error" 
        type="error" 
        :show-icon="true"
        class="mb-6"
        closable
        @close="matchingStore.error = null"
      >
        {{ matchingStore.error }}
      </n-alert>

      <!-- 空状態 -->
      <div v-else-if="sortedFilteredRequests.length === 0" class="text-center py-12">
        <div class="text-6xl mb-4 opacity-30">🔍</div>
        <h3 class="text-xl font-semibold text-gray-700 mb-2">募集が見つかりませんでした</h3>
        <p class="text-gray-500 mb-4">条件を変更して再度検索してみてください</p>
        <n-button 
          @click="resetSearch"
          class="btn-secondary"
        >
          検索条件をリセット
        </n-button>
      </div>

      <!-- 募集カードグリッド -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <n-card 
          v-for="request in sortedFilteredRequests" 
          :key="request.id"
          class="card-simple h-full"
          :class="getCardClasses(request)"
          :bordered="true"
        >
          <!-- カードヘッダー -->
          <template #header>
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <h3 class="text-base font-semibold mb-2 line-clamp-2"
                    :class="getCardTextClasses(request)">
                  {{ request.attraction }}
                </h3>
                
                <div class="space-y-1">
                  <div class="flex items-center text-sm"
                       :class="getCardTextClasses(request, 'secondary')">
                    <span class="mr-1">👤</span>
                    {{ request.hostDisplayName }}
                  </div>
                  <div class="flex items-center text-sm"
                       :class="getCardTextClasses(request, 'secondary')">
                    <span class="mr-1">🕒</span>
                    {{ formatDateTime(request.preferredDateTime) }}
                    <span v-if="isRequestExpired(request)" class="ml-2 text-xs text-red-600">
                      ({{ getTimeUntilExpiration(request.preferredDateTime) }})
                    </span>
                  </div>
                </div>
              </div>
              
              <div class="ml-2 flex flex-col gap-1">
                <n-tag
                  :type="getStatusTagType(request.status, request)"
                  :bordered="false"
                  size="small"
                >
                  {{ getStatusText(request.status, request) }}
                </n-tag>
                
                <!-- 期限切れバッジ -->
                <n-tag
                  v-if="isRequestExpired(request)"
                  type="error"
                  :bordered="false"
                  size="small"
                  class="bg-red-100 text-red-800"
                >
                  期限切れ
                </n-tag>
              </div>
            </div>
          </template>

          <!-- 参加者状況 -->
          <div class="mb-4">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm" :class="getCardTextClasses(request, 'secondary')">参加者</span>
              <span class="text-sm font-medium text-primary-600"
                    :class="isRequestExpired(request) ? 'opacity-60' : ''">
                {{ request.currentApplications }}/{{ request.maxParticipants }}人
              </span>
            </div>
            
            <n-progress
              type="line"
              :percentage="(request.currentApplications / request.maxParticipants) * 100"
              :color="getProgressColor(request.currentApplications, request.maxParticipants, request)"
              :height="4"
              :border-radius="2"
              :fill-border-radius="2"
            />
          </div>

          <!-- 説明 -->
          <div v-if="request.description" class="mb-4">
            <p class="text-sm line-clamp-2" :class="getCardTextClasses(request, 'secondary')">
              {{ request.description }}
            </p>
          </div>

          <!-- カードアクション -->
          <template #action>
            <n-space>
              <n-button 
                @click="$router.push(`/matching/${request.id}`)"
                size="small"
                :type="getRoomButtonType(request)"
                class="flex-1"
                :class="getRoomButtonClass(request)"
                :disabled="isRequestExpired(request) && !hasUserParticipated(request)"
              >
                <template #icon>
                  <div :class="getRoomButtonIcon(request)"></div>
                </template>
                {{ getRoomButtonText(request) }}
              </n-button>
              
              <n-button
                v-if="canApplyToRequest(request) && !hasUserParticipated(request) && !hasExistingApplication"
                type="primary"
                @click="showApplicationModal(request)"
                size="small"
                class="btn-primary flex-1"
                :disabled="isRequestExpired(request)"
              >
                <template #icon>
                  <div class="i-carbon-user-follow"></div>
                </template>
                {{ isRequestExpired(request) ? '期限切れ' : '応募する' }}
              </n-button>
              
              <n-button
                v-else-if="hasExistingApplication && !hasUserParticipated(request)"
                disabled
                size="small"
                class="flex-1"
              >
                応募済み
              </n-button>
              
              <n-button
                v-else-if="!canApplyToRequest(request) || isRequestExpired(request)"
                disabled
                size="small"
                class="flex-1"
              >
                {{ isRequestExpired(request) ? '期限切れ' : '募集終了' }}
              </n-button>
            </n-space>
          </template>
        </n-card>
      </div>

      <!-- Application Modal -->
      <ApplicationModal 
        v-if="selectedRequest"
        :visible="showModal"
        :request="selectedRequest"
        @close="closeModal"
        @applied="handleApplicationSuccess"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMatchingStore } from '@/stores/matching'
import type { MatchingRequestDto, MatchingRequestWithActions } from '@/types'
import ApplicationModal from '@/components/matching/ApplicationModal.vue'
import { 
  isExpired, 
  getExpirationStatus, 
  getTimeUntilExpiration, 
  formatDateTime as utilFormatDateTime 
} from '@/utils/dateUtils'

const router = useRouter()
const matchingStore = useMatchingStore()

// Search form
const searchForm = ref({
  attraction: null as string | null,
  maxParticipants: null as number | null
})

const searchDateValue = ref<number | null>(null)

// Modal state
const showModal = ref(false)
const selectedRequest = ref<MatchingRequestDto | null>(null)

// Computed
const isLoading = computed(() => matchingStore.isLoading)
const filteredRequests = computed(() => matchingStore.activeRequests)
const hasExistingApplication = computed(() => {
  return matchingStore.myApplications.some(app => 
    app.status === 'PENDING' || app.status === 'ACCEPTED'
  )
})

// 期限切れを考慮したソート済み募集一覧
const sortedFilteredRequests = computed(() => {
  const requests = [...filteredRequests.value]
  
  return requests.sort((a, b) => {
    const aExpired = isExpired(a.preferredDateTime)
    const bExpired = isExpired(b.preferredDateTime)
    
    // 期限切れでない募集を上に表示
    if (aExpired !== bExpired) {
      return aExpired ? 1 : -1
    }
    
    // 同じ期限切れ状態の場合は日時順
    return new Date(a.preferredDateTime).getTime() - new Date(b.preferredDateTime).getTime()
  })
})

// 期限切れ判定と視覚的スタイリング
const isRequestExpired = (request: MatchingRequestDto): boolean => {
  return isExpired(request.preferredDateTime)
}

const getCardClasses = (request: MatchingRequestDto): string => {
  if (isRequestExpired(request)) {
    return 'opacity-70 bg-gray-50 border-gray-200'
  }
  return ''
}

const getCardTextClasses = (request: MatchingRequestDto, type: 'primary' | 'secondary' = 'primary'): string => {
  const baseClasses = type === 'primary' ? 'text-gray-900' : 'text-gray-600'
  
  if (isRequestExpired(request)) {
    return type === 'primary' ? 'text-gray-500' : 'text-gray-400'
  }
  
  return baseClasses
}

// Filter options  
const attractionFilterOptions = [
  {
    type: 'group',
    label: '🎢 ライド系',
    key: 'rides',
    children: [
      { label: 'ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー', value: 'ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー' },
      { label: 'フライト・オブ・ザ・ヒッポグリフ', value: 'フライト・オブ・ザ・ヒッポグリフ' },
      { label: 'ミニオン・ハチャメチャ・ライド', value: 'ミニオン・ハチャメチャ・ライド' },
      { label: 'ジュラシック・パーク・ザ・ライド', value: 'ジュラシック・パーク・ザ・ライド' },
      { label: 'ザ・フライング・ダイナソー', value: 'ザ・フライング・ダイナソー' },
      { label: 'ハリウッド・ドリーム・ザ・ライド', value: 'ハリウッド・ドリーム・ザ・ライド' },
      { label: 'ハリウッド・ドリーム・ザ・ライド〜バックドロップ〜', value: 'ハリウッド・ドリーム・ザ・ライド〜バックドロップ〜' },
      { label: 'スペース・ファンタジー・ザ・ライド', value: 'スペース・ファンタジー・ザ・ライド' }
    ]
  },
  {
    type: 'group',
    label: '🎭 ショー・体験系',
    key: 'shows',
    children: [
      { label: 'シング・オン・ツアー', value: 'シング・オン・ツアー' },
      { label: 'ターミネーター 2:3-D', value: 'ターミネーター 2:3-D' },
      { label: 'シュレック 4-D アドベンチャー', value: 'シュレック 4-D アドベンチャー' },
      { label: 'ミニオン・ハチャメチャ・アイス', value: 'ミニオン・ハチャメチャ・アイス' }
    ]
  },
  {
    type: 'group',
    label: '🌟 エリア・その他',
    key: 'areas',
    children: [
      { label: 'ハリー・ポッター エリア散策', value: 'ハリー・ポッター エリア散策' },
      { label: 'ミニオン・パーク エリア散策', value: 'ミニオン・パーク エリア散策' },
      { label: 'ジュラシック・パーク エリア散策', value: 'ジュラシック・パーク エリア散策' },
      { label: 'フード・コート', value: 'フード・コート' },
      { label: 'お土産ショッピング', value: 'お土産ショッピング' }
    ]
  }
]

const participantOptions = [
  { label: '2人', value: 2 },
  { label: '3人', value: 3 },
  { label: '4人', value: 4 },
  { label: '5人', value: 5 },
  { label: '6人', value: 6 },
  { label: '7人', value: 7 },
  { label: '8人', value: 8 }
]

// Utility functions
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

const getStatusTagType = (status: string, request: MatchingRequestDto) => {
  if (isRequestExpired(request)) {
    return 'error'
  }
  switch (status) {
    case 'OPEN': return 'success'
    case 'WAITING': return 'info'
    case 'CONFIRMED': return 'warning'
    case 'CLOSED': return 'default'
    case 'EXPIRED': return 'error'
    default: return 'default'
  }
}

const getStatusText = (status: string, request: MatchingRequestDto): string => {
  if (isRequestExpired(request)) {
    return '期限切れ'
  }
  switch (status) {
    case 'OPEN': return '募集中'
    case 'WAITING': return '待機中'
    case 'CONFIRMED': return '確定済み'
    case 'CLOSED': return '終了'
    case 'EXPIRED': return '期限切れ'
    default: return '不明'
  }
}

const getProgressColor = (current: number, max: number, request: MatchingRequestDto): string => {
  const percentage = (current / max) * 100
  // 期限切れの場合は色を薄くする
  if (isRequestExpired(request)) {
    return '#94a3b8' // グレー色
  }
  if (percentage >= 100) return '#22c55e' // adventure green
  if (percentage >= 75) return '#facc15' // sunshine yellow
  if (percentage >= 50) return '#f97316' // orange
  return '#d946ef' // magic
}

const formatDateTime = (dateTime: string): string => {
  const date = new Date(dateTime)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const isDateDisabled = (ts: number) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return ts < today.getTime()
}

const canApplyToRequest = (request: MatchingRequestDto): boolean => {
  return request.status === 'OPEN' && request.currentApplications < request.maxParticipants && !isRequestExpired(request)
}

// Room button functions
const getRoomButtonType = (request: MatchingRequestDto | MatchingRequestWithActions): string => {
  if (hasUserParticipated(request)) {
    return 'primary'
  }
  return 'default'
}

const getRoomButtonClass = (request: MatchingRequestDto | MatchingRequestWithActions): string => {
  if (hasUserParticipated(request)) {
    return 'btn-primary'
  }
  return 'btn-secondary'
}

const getRoomButtonIcon = (request: MatchingRequestDto | MatchingRequestWithActions): string => {
  switch (request.status) {
    case 'OPEN':
      return hasUserParticipated(request) ? 'i-carbon-home' : 'i-carbon-view'
    case 'WAITING':
      return 'i-carbon-time'
    case 'CONFIRMED':
      return 'i-carbon-chat'
    default:
      return 'i-carbon-view'
  }
}

const getRoomButtonText = (request: MatchingRequestDto | MatchingRequestWithActions): string => {
  if (hasUserParticipated(request)) {
    switch (request.status) {
      case 'OPEN':
      case 'WAITING':
        return '待機室へ'
      case 'CONFIRMED':
        return 'チャットへ'
      default:
        return '詳細'
    }
  }
  
  switch (request.status) {
    case 'OPEN':
      return '詳細を見る'
    case 'WAITING':
      return '待機室を見る'
    case 'CONFIRMED':
      return '確定済み'
    default:
      return '詳細'
  }
}

const hasUserParticipated = (request: MatchingRequestDto | MatchingRequestWithActions): boolean => {
  // MatchingRequestWithActionsの場合、userApplicationをチェック
  if ('userApplication' in request && request.userApplication) {
    return request.userApplication.status === 'ACCEPTED' || request.userApplication.status === 'PENDING'
  }
  return false
}

// Search handlers
const handleSearch = () => {
  matchingStore.fetchActiveRequests({
    attraction: searchForm.value.attraction || undefined,
    maxParticipants: searchForm.value.maxParticipants || undefined,
    dateFrom: searchDateValue.value ? new Date(searchDateValue.value).toISOString().split('T')[0] : undefined
  })
}

const handleDateSearch = () => {
  handleSearch()
}

const resetSearch = () => {
  searchForm.value = {
    attraction: null,
    maxParticipants: null
  }
  searchDateValue.value = null
  matchingStore.fetchActiveRequests()
}

// Modal handlers
const showApplicationModal = (request: MatchingRequestDto) => {
  selectedRequest.value = request
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedRequest.value = null
}

const handleApplicationSuccess = () => {
  closeModal()
  matchingStore.fetchActiveRequests() // Refresh list
  matchingStore.fetchMyApplications() // Refresh applications
  
  // 応募した募集の待機室画面に遷移
  if (selectedRequest.value) {
    router.push(`/matching/${selectedRequest.value.id}/room`)
  }
}

// Initialize
onMounted(() => {
  matchingStore.fetchActiveRequests()
  matchingStore.fetchMyApplications()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* レスポンシブデザイン */
@media (max-width: 768px) {
  .container-app {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style> 