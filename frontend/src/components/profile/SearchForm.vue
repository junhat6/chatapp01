<template>
  <form @submit.prevent="handleSearch" class="search-form">
    <div class="form-row">
      <div class="form-group">
        <label for="minAge" class="form-label">年齢範囲</label>
        <div class="age-range">
          <BaseInput
            v-model="searchData.minAge"
            type="number"
            placeholder="最小"
            :min="13"
            :max="120"
            :disabled="loading"
          />
          <span class="age-separator">〜</span>
          <BaseInput
            v-model="searchData.maxAge"
            type="number"
            placeholder="最大"
            :min="13"
            :max="120"
            :disabled="loading"
          />
        </div>
      </div>

      <BaseSelect
        v-model="searchData.gender"
        label="性別"
        placeholder="すべて"
        :options="genderOptions"
        :disabled="loading"
      />
    </div>

    <div class="form-row">
      <div class="form-group">
        <label class="form-label">年間パス</label>
        <div class="checkbox-group">
          <label class="checkbox-label">
            <input
              v-model="searchData.hasUsjAnnualPass"
              type="checkbox"
              :disabled="loading"
            />
            年間パス保有者のみ
          </label>
        </div>
      </div>

      <BaseSelect
        v-model="searchData.locationPrefecture"
        label="都道府県"
        placeholder="すべて"
        :options="prefectures"
        :disabled="loading"
      />
    </div>

    <BaseSelect
      v-model="searchData.favoriteAttraction"
      label="好きなアトラクション"
      placeholder="すべて"
      :options="attractionOptions"
      :disabled="loading"
    />

    <BaseInput
      v-model="searchData.displayName"
      label="表示名"
      placeholder="表示名で検索"
      :disabled="loading"
    />

    <div class="form-actions">
      <BaseButton
        type="submit"
        variant="primary"
        :loading="loading"
        :disabled="loading"
      >
        検索
      </BaseButton>
      <BaseButton
        type="button"
        variant="secondary"
        :disabled="loading"
        @click="handleClear"
      >
        クリア
      </BaseButton>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { BaseInput, BaseSelect, BaseButton } from '@/components/base'
import type { UserProfileSearchRequest } from '@/types'
import { profileApi } from '@/services/profileApi'

interface Props {
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  search: [params: UserProfileSearchRequest]
  clear: []
}>()

const searchData = ref<UserProfileSearchRequest>({
  minAge: undefined,
  maxAge: undefined,
  gender: '',
  hasUsjAnnualPass: undefined,
  favoriteAttraction: '',
  locationPrefecture: '',
  displayName: ''
})

const availableAttractions = ref<string[]>([])

// オプション配列
const genderOptions = [
  '男性',
  '女性',
  'その他'
]

const prefectures = [
  '北海道', '青森県', '岩手県', '宮城県', '秋田県', '山形県', '福島県',
  '茨城県', '栃木県', '群馬県', '埼玉県', '千葉県', '東京都', '神奈川県',
  '新潟県', '富山県', '石川県', '福井県', '山梨県', '長野県', '岐阜県',
  '静岡県', '愛知県', '三重県', '滋賀県', '京都府', '大阪府', '兵庫県',
  '奈良県', '和歌山県', '鳥取県', '島根県', '岡山県', '広島県', '山口県',
  '徳島県', '香川県', '愛媛県', '高知県', '福岡県', '佐賀県', '長崎県',
  '熊本県', '大分県', '宮崎県', '鹿児島県', '沖縄県'
]

const attractionOptions = computed(() => {
  return availableAttractions.value.map(attraction => ({
    label: attraction,
    value: attraction
  }))
})

// アトラクション一覧を取得
const fetchAvailableAttractions = async () => {
  try {
    const response = await profileApi.getAvailableAttractions()
    if (response.data.success) {
      availableAttractions.value = response.data.data || []
    }
  } catch (error) {
    console.error('アトラクション一覧の取得に失敗しました:', error)
    // フォールバック用のデフォルトアトラクション
    availableAttractions.value = [
      'ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー',
      'ザ・フライング・ダイナソー',
      'ハリウッド・ドリーム・ザ・ライド',
      'スペース・ファンタジー・ザ・ライド',
      'ジュラシック・パーク・ザ・ライド',
      'アメージング・アドベンチャー・オブ・スパイダーマン・ザ・ライド 4K3D',
      'バックドラフト',
      'ターミネーター 2:3-D',
      'シュレック 4-D アドベンチャー',
      'ミニオン・ハチャメチャ・ライド',
      'ヨッシーアドベンチャー',
      'マリオカート〜クッパの挑戦状〜'
    ]
  }
}

// 検索実行
const handleSearch = () => {
  const searchParams: UserProfileSearchRequest = {
    minAge: searchData.value.minAge || undefined,
    maxAge: searchData.value.maxAge || undefined,
    gender: searchData.value.gender || undefined,
    hasUsjAnnualPass: searchData.value.hasUsjAnnualPass || undefined,
    favoriteAttraction: searchData.value.favoriteAttraction || undefined,
    locationPrefecture: searchData.value.locationPrefecture || undefined,
    displayName: searchData.value.displayName || undefined
  }
  emit('search', searchParams)
}

// 検索クリア
const handleClear = () => {
  searchData.value = {
    minAge: undefined,
    maxAge: undefined,
    gender: '',
    hasUsjAnnualPass: undefined,
    favoriteAttraction: '',
    locationPrefecture: '',
    displayName: ''
  }
  emit('clear')
}

onMounted(() => {
  fetchAvailableAttractions()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 2rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
}

.age-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.age-range :deep(.form-group) {
  margin-bottom: 0;
  flex: 1;
}

.age-separator {
  color: #6b7280;
  font-weight: 500;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: normal;
}

.form-actions {
  display: flex;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f3f4f6;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style> 