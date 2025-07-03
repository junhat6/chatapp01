<template>
  <div class="min-h-screen bg-gray-50">
    <div class="container-app py-6">
      <!-- ヘッダー -->
      <div class="mb-8">
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
        
        <div class="mb-6">
          <h1 class="text-3xl font-bold text-gray-900 mb-2">新しい募集を作成</h1>
          <p class="text-gray-600">一緒に楽しむメンバーを募集しましょう</p>
        </div>
      </div>

      <div class="max-w-2xl mx-auto">
        <!-- エラーアラート -->
        <n-alert 
          v-if="matchingStore.error" 
          type="error" 
          :show-icon="true"
          class="mb-6"
          closable
          @close="matchingStore.error = null"
        >
          {{ matchingStore.error }}
        </n-alert>

        <!-- メインフォームカード -->
        <n-card 
          class="card-feature"
          :bordered="true"
        >
          <template #header>
            <div class="py-2">
              <h2 class="text-xl font-semibold text-gray-900">募集内容を入力</h2>
            </div>
          </template>
          
          <form @submit.prevent="handleSubmit" class="space-y-6">
            <!-- アトラクション選択 -->
            <n-form-item label="アトラクション" :required="true">
              <n-select
                v-model:value="form.attraction"
                placeholder="アトラクションを選択してください"
                :options="attractionOptions"
                :disabled="isLoading"
                size="medium"
                class="w-full"
              />
            </n-form-item>

            <!-- 日時選択 -->
            <n-form-item label="希望日時" :required="true">
              <n-grid :cols="2" :x-gap="16">
                <n-grid-item>
                  <n-date-picker
                    v-model:value="dateValue"
                    type="date"
                    placeholder="日付を選択"
                    :is-date-disabled="isDateDisabled"
                    :disabled="isLoading"
                    size="medium"
                    class="w-full"
                  />
                </n-grid-item>
                <n-grid-item>
                  <n-time-picker
                    v-model:value="timeValue"
                    placeholder="時間を選択"
                    :disabled="isLoading"
                    size="medium"
                    class="w-full"
                    :status="!isTimeValid && timeValue ? 'error' : undefined"
                  />
                  <div v-if="!isTimeValid && timeValue" class="text-red-500 text-sm mt-1">
                    選択された時間は過去の時間です。現在時刻より後の時間を選択してください。
                  </div>
                </n-grid-item>
              </n-grid>
            </n-form-item>

            <!-- 募集人数 -->
            <n-form-item label="募集人数（自分含む）" :required="true">
              <div class="flex items-center justify-center space-x-4">
                <n-button
                  circle
                  @click="decrementParticipants"
                  :disabled="form.maxParticipants <= 2 || isLoading"
                  size="medium"
                  class="btn-secondary"
                >
                  <template #icon>
                    <div class="i-carbon-subtract"></div>
                  </template>
                </n-button>
                
                <div class="text-center min-w-16">
                  <div class="text-2xl font-bold text-primary-600">{{ form.maxParticipants }}</div>
                  <div class="text-sm text-gray-500">人</div>
                </div>
                
                <n-button
                  circle
                  @click="incrementParticipants"
                  :disabled="form.maxParticipants >= 8 || isLoading"
                  size="medium"
                  class="btn-secondary"
                >
                  <template #icon>
                    <div class="i-carbon-add"></div>
                  </template>
                </n-button>
              </div>
              <div class="text-center text-sm text-gray-500 mt-2">
                2〜8人まで設定できます
              </div>
            </n-form-item>

            <!-- メッセージ -->
            <n-form-item label="メッセージ・説明（任意）">
              <n-input
                v-model:value="form.description"
                type="textarea"
                placeholder="一緒に楽しみたいことや、参加者への要望があれば入力してください。"
                :maxlength="500"
                :disabled="isLoading"
                :autosize="{ minRows: 3, maxRows: 5 }"
                show-count
                size="medium"
              />
            </n-form-item>

            <!-- アクションボタン -->
            <n-space justify="center" size="large" class="pt-4">
              <n-button
                size="medium"
                @click="$router.push('/matching')"
                :disabled="isLoading"
                class="btn-secondary px-6"
              >
                キャンセル
              </n-button>
              
              <n-button
                type="primary"
                size="medium"
                attr-type="submit"
                :disabled="isLoading || !isFormValid"
                :loading="isLoading"
                class="btn-primary px-8"
              >
                {{ isLoading ? '作成中...' : '募集を作成' }}
              </n-button>
            </n-space>
          </form>
        </n-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMatchingStore } from '@/stores/matching'
import type { CreateMatchingRequestRequest } from '@/types'

const router = useRouter()
const matchingStore = useMatchingStore()

// Form data
const form = ref({
  attraction: null as string | null,
  maxParticipants: 4,
  description: ''
})

// Date/Time values for Naive UI components
const dateValue = ref<number | null>(null)
const timeValue = ref<number | null>(null)

// Attraction options
const attractionOptions = [
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

// Loading state
const isLoading = computed(() => matchingStore.isLoading)

// Date validation - disable past dates and times
const isDateDisabled = (ts: number) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return ts < today.getTime()
}

// Time validation - ensure selected time is in the future
const isTimeValid = computed(() => {
  if (!dateValue.value || !timeValue.value) return false
  
  const selectedDate = new Date(dateValue.value)
  const selectedTime = new Date(timeValue.value)
  
  // Extract time components from selected time
  const hours = selectedTime.getHours()
  const minutes = selectedTime.getMinutes()
  
  // Create a new date object with the selected date and time
  const combinedDateTime = new Date(
    selectedDate.getFullYear(),
    selectedDate.getMonth(),
    selectedDate.getDate(),
    hours,
    minutes,
    0,
    0
  )
  
  // Ensure the datetime is in the future (add 1 minute buffer)
  const now = new Date()
  now.setMinutes(now.getMinutes() + 1) // Add 1 minute buffer
  
  return combinedDateTime > now
})

// Form validation
const isFormValid = computed(() => {
  return form.value.attraction && 
         dateValue.value && 
         timeValue.value && 
         form.value.maxParticipants >= 2 && 
         form.value.maxParticipants <= 8 &&
         isTimeValid.value
})

// Participant count controls
const incrementParticipants = () => {
  if (form.value.maxParticipants < 8) {
    form.value.maxParticipants++
  }
}

const decrementParticipants = () => {
  if (form.value.maxParticipants > 2) {
    form.value.maxParticipants--
  }
}

// Form submission
const handleSubmit = async () => {
  if (!isFormValid.value || !dateValue.value || !timeValue.value) return

  try {
    // Convert date/time to ISO string with proper timezone handling
    const selectedDate = new Date(dateValue.value)
    const selectedTime = new Date(timeValue.value)
    
    // Extract time components from selected time
    const hours = selectedTime.getHours()
    const minutes = selectedTime.getMinutes()
    
    // Create a new date object with the selected date and time
    // Use local timezone to avoid conversion issues
    const combinedDateTime = new Date(
      selectedDate.getFullYear(),
      selectedDate.getMonth(),
      selectedDate.getDate(),
      hours,
      minutes,
      0,
      0
    )
    
    // Ensure the datetime is in the future (add 1 minute buffer)
    const now = new Date()
    now.setMinutes(now.getMinutes() + 1) // Add 1 minute buffer
    
    if (combinedDateTime <= now) {
      throw new Error('希望時間は現在時刻より後に設定してください')
    }
    
    // Convert to ISO string with timezone offset
    const year = combinedDateTime.getFullYear()
    const month = String(combinedDateTime.getMonth() + 1).padStart(2, '0')
    const day = String(combinedDateTime.getDate()).padStart(2, '0')
    const hour = String(combinedDateTime.getHours()).padStart(2, '0')
    const minute = String(combinedDateTime.getMinutes()).padStart(2, '0')
    
    // Create ISO string in local timezone
    const preferredDateTime = `${year}-${month}-${day}T${hour}:${minute}:00`
    
    const requestData: CreateMatchingRequestRequest = {
      attraction: form.value.attraction!,
      preferredDateTime,
      maxParticipants: form.value.maxParticipants,
      description: form.value.description || undefined
    }

    const newRequest = await matchingStore.createRequest(requestData)
    
    // Navigate to waiting room on success
    router.push(`/matching/${newRequest.id}/room`)
  } catch (error) {
    console.error('募集作成エラー:', error)
    // Show error to user
    if (error instanceof Error) {
      matchingStore.error = error.message
    } else if (typeof error === 'object' && error !== null) {
      // Axios error response
      const axiosError = error as any
      if (axiosError.response?.data?.message) {
        matchingStore.error = axiosError.response.data.message
      } else if (axiosError.response?.data?.errors) {
        matchingStore.error = axiosError.response.data.errors.join(', ')
      } else if (axiosError.message) {
        matchingStore.error = axiosError.message
      } else {
        matchingStore.error = '募集作成に失敗しました'
      }
    } else {
      matchingStore.error = '募集作成に失敗しました'
    }
  }
}

// Initialize default time
onMounted(() => {
  // Set default date to today
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dateValue.value = today.getTime()
  
  // Set default time to 1 hour from now
  const now = new Date()
  const defaultTime = new Date()
  defaultTime.setHours(now.getHours() + 1, 0, 0, 0)
  timeValue.value = defaultTime.getTime()
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