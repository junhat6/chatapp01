<template>
  <!-- モーダルオーバーレイ -->
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
    <div class="bg-white rounded-xl max-w-md w-full max-h-screen overflow-y-auto shadow-2xl">
      <!-- モーダルヘッダー -->
      <div class="px-6 py-4 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900">募集に応募</h3>
          <button 
            @click="$emit('close')"
            class="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <svg class="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      <!-- 募集情報 -->
      <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
        <h4 class="text-lg font-medium text-gray-900 mb-2">{{ request?.attraction }}</h4>
        <div class="space-y-2 text-sm text-gray-600">
          <div class="flex items-center">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            ホスト: {{ request?.hostDisplayName }}
          </div>
          <div class="flex items-center">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {{ formatDateTime(request?.preferredDateTime) }}
          </div>
          <div class="flex items-center">
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            {{ request?.currentApplications }}/{{ request?.maxParticipants }}人
          </div>
        </div>
        <div v-if="request?.description" class="mt-3 p-3 bg-white rounded-lg">
          <p class="text-sm text-gray-700">{{ request.description }}</p>
        </div>
      </div>

      <!-- エラー表示 -->
      <div v-if="error" class="px-6 py-4">
        <div class="p-3 bg-red-50 border border-red-200 rounded-lg">
          <div class="flex">
            <svg class="w-5 h-5 text-red-400 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
            <p class="text-red-700 text-sm">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- 応募制限チェック -->
      <div v-if="hasExistingApplication" class="px-6 py-4">
        <div class="p-3 bg-orange-50 border border-orange-200 rounded-lg">
          <div class="flex">
            <svg class="w-5 h-5 text-orange-400 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
            </svg>
            <div>
              <p class="text-orange-700 text-sm font-medium">既に他の募集に応募しています</p>
              <p class="text-orange-600 text-xs mt-1">一度に一つの募集にしか応募できません</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 応募フォーム -->
      <form @submit.prevent="handleSubmit" class="px-6 py-4">
        <!-- 注意事項 -->
        <div class="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
          <h5 class="text-sm font-medium text-yellow-800 mb-2">⚠️ 応募前にご確認ください</h5>
          <ul class="text-sm text-yellow-700 space-y-1">
            <li>• 一度に一つの募集にしか応募できません</li>
            <li>• 応募すると自動的に待機室に参加します</li>
            <li>• ホストが承認するとチャットルームが作成されます</li>
            <li>• マナーを守って楽しみましょう</li>
          </ul>
        </div>

        <!-- ボタン -->
        <div class="flex gap-3">
          <button 
            type="button"
            @click="$emit('close')"
            :disabled="isLoading"
            class="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium"
          >
            キャンセル
          </button>
          <button 
            type="submit"
            :disabled="isLoading || hasExistingApplication"
            class="flex-1 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors font-medium flex items-center justify-center"
          >
            <svg v-if="isLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ isLoading ? '応募中...' : '応募する' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMatchingStore } from '@/stores/matching'
import type { MatchingRequestDto, CreateMatchingApplicationRequest } from '@/types'

// Props & Emits
interface Props {
  request: MatchingRequestDto | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  applied: []
}>()

// Store
const matchingStore = useMatchingStore()

// State
const error = ref<string | null>(null)

// Computed
const isLoading = computed(() => matchingStore.isLoading)
const hasExistingApplication = computed(() => {
  return matchingStore.myApplications.some(app => 
    app.status === 'PENDING' || app.status === 'ACCEPTED'
  )
})

// Methods
const formatDateTime = (dateTimeStr?: string) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}/${day} ${hours}:${minutes}`
}

const handleSubmit = async () => {
  if (!props.request || hasExistingApplication.value) return

  try {
    error.value = null
    
    const applicationData: CreateMatchingApplicationRequest = {
      message: undefined // メッセージは不要
    }

    await matchingStore.applyToRequest(props.request.id, applicationData)
    
    // 成功時は親コンポーネントに通知
    emit('applied')
  } catch (err: any) {
    error.value = err.response?.data?.message || '応募に失敗しました'
  }
}

// 初期化時に応募一覧を取得
onMounted(async () => {
  try {
    await matchingStore.fetchMyApplications()
  } catch (err) {
    console.error('応募一覧の取得に失敗しました:', err)
  }
})

// モーダル外クリックで閉じる
const handleBackdropClick = (event: Event) => {
  if (event.target === event.currentTarget) {
    emit('close')
  }
}
</script>

<style scoped>
/* モーダルアニメーション */
.fixed {
  animation: fadeIn 0.2s ease-out;
}

.bg-white {
  animation: slideInUp 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* スクロールバーのスタイリング */
textarea::-webkit-scrollbar {
  width: 6px;
}

textarea::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

textarea::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* フォーカススタイル */
textarea:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(147, 51, 234, 0.1);
}
</style> 