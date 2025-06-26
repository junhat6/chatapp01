<template>
  <div class="search-page">
    <div class="search-container">
      <div class="card">
        <div class="search-header">
          <h1 class="search-title">プロフィール検索</h1>
          <p class="search-subtitle">
            同じUSJ好きの仲間を見つけよう！
          </p>
        </div>

        <!-- 検索フォーム -->
        <form @submit.prevent="handleSearch" class="search-form">
          <div class="form-row">
            <div class="form-group">
              <label for="minAge" class="form-label">年齢範囲</label>
              <div class="age-range">
                <input
                  id="minAge"
                  v-model.number="searchForm.minAge"
                  type="number"
                  class="form-input"
                  placeholder="最小"
                  min="13"
                  max="120"
                  :disabled="loading"
                />
                <span class="age-separator">〜</span>
                <input
                  v-model.number="searchForm.maxAge"
                  type="number"
                  class="form-input"
                  placeholder="最大"
                  min="13"
                  max="120"
                  :disabled="loading"
                />
              </div>
            </div>

            <div class="form-group">
              <label for="gender" class="form-label">性別</label>
              <select
                id="gender"
                v-model="searchForm.gender"
                class="form-input"
                :disabled="loading"
              >
                <option value="">すべて</option>
                <option value="男性">男性</option>
                <option value="女性">女性</option>
                <option value="その他">その他</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label">年間パス</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input
                    v-model="searchForm.hasUsjAnnualPass"
                    type="checkbox"
                    :disabled="loading"
                  />
                  年間パス保有者のみ
                </label>
              </div>
            </div>

            <div class="form-group">
              <label for="locationPrefecture" class="form-label">都道府県</label>
              <select
                id="locationPrefecture"
                v-model="searchForm.locationPrefecture"
                class="form-input"
                :disabled="loading"
              >
                <option value="">すべて</option>
                <option v-for="prefecture in prefectures" :key="prefecture" :value="prefecture">
                  {{ prefecture }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="favoriteAttraction" class="form-label">好きなアトラクション</label>
            <select
              id="favoriteAttraction"
              v-model="searchForm.favoriteAttraction"
              class="form-input"
              :disabled="loading"
            >
              <option value="">すべて</option>
              <option v-for="attraction in availableAttractions" :key="attraction" :value="attraction">
                {{ attraction }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label for="displayName" class="form-label">表示名</label>
            <input
              id="displayName"
              v-model="searchForm.displayName"
              type="text"
              class="form-input"
              placeholder="表示名で検索"
              :disabled="loading"
            />
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="loading"
            >
              {{ loading ? '検索中...' : '検索' }}
            </button>
            <button
              type="button"
              @click="clearSearch"
              class="btn btn-secondary"
              :disabled="loading"
            >
              クリア
            </button>
          </div>
        </form>

        <!-- クイック検索ボタン -->
        <div class="quick-search">
          <h3>クイック検索</h3>
          <div class="quick-buttons">
            <button
              @click="searchAnnualPassHolders"
              class="btn btn-outline-primary"
              :disabled="loading"
            >
              🎫 年間パス保有者
            </button>
          </div>
        </div>

        <!-- エラー表示 -->
        <div v-if="error" class="alert alert-error">
          {{ error }}
        </div>

        <!-- 検索結果 -->
        <div v-if="searchResults.length > 0" class="search-results">
          <h3>検索結果 ({{ searchResults.length }}件)</h3>
          <div class="profile-grid">
            <div v-for="profile in searchResults" :key="profile.userId" class="profile-card">
              <div class="profile-card-header">
                <h4>{{ profile.displayName }}</h4>
                <span class="user-id">ID: {{ profile.userId }}</span>
                <div class="profile-badges">
                  <span v-if="profile.hasUsjAnnualPass" class="badge annual-pass-badge">
                    🎫 年間パス
                  </span>
                  <span v-if="profile.age" class="badge age-badge">
                    {{ profile.age }}歳
                  </span>
                  <span v-if="profile.gender" class="badge gender-badge">
                    {{ profile.gender }}
                  </span>
                </div>
              </div>

              <div class="profile-card-content">
                <div v-if="profile.locationPrefecture" class="profile-detail">
                  <strong>📍 都道府県:</strong> {{ profile.locationPrefecture }}
                </div>

                <div v-if="profile.favoriteAttractions.length > 0" class="profile-detail">
                  <strong>🎢 好きなアトラクション:</strong>
                  <div class="tags">
                    <span v-for="attraction in profile.favoriteAttractions" :key="attraction" class="tag attraction-tag">
                      {{ attraction }}
                    </span>
                  </div>
                </div>

                <div v-if="profile.hobbies.length > 0" class="profile-detail">
                  <strong>⭐ 趣味:</strong>
                  <div class="tags">
                    <span v-for="hobby in profile.hobbies" :key="hobby" class="tag hobby-tag">
                      {{ hobby }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 検索結果なし -->
        <div v-if="hasSearched && searchResults.length === 0 && !loading" class="no-results">
          <div class="no-results-icon">🔍</div>
          <h3>検索結果が見つかりませんでした</h3>
          <p>検索条件を変更して再度お試しください。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useProfileStore } from '@/stores/profile'
import type { UserProfile, UserProfileSearchRequest } from '@/types'
import { profileApi } from '@/services/profileApi'

const profileStore = useProfileStore()

const loading = computed(() => profileStore.loading)
const error = computed(() => profileStore.error)

const searchResults = ref<UserProfile[]>([])
const hasSearched = ref(false)
const availableAttractions = ref<string[]>([])

// 検索フォーム
const searchForm = ref<UserProfileSearchRequest>({
  minAge: undefined,
  maxAge: undefined,
  gender: '',
  hasUsjAnnualPass: undefined,
  favoriteAttraction: '',
  locationPrefecture: ''
})

// 都道府県リスト
const prefectures = [
  '北海道', '青森県', '岩手県', '宮城県', '秋田県', '山形県', '福島県',
  '茨城県', '栃木県', '群馬県', '埼玉県', '千葉県', '東京都', '神奈川県',
  '新潟県', '富山県', '石川県', '福井県', '山梨県', '長野県', '岐阜県',
  '静岡県', '愛知県', '三重県', '滋賀県', '京都府', '大阪府', '兵庫県',
  '奈良県', '和歌山県', '鳥取県', '島根県', '岡山県', '広島県', '山口県',
  '徳島県', '香川県', '愛媛県', '高知県', '福岡県', '佐賀県', '長崎県',
  '熊本県', '大分県', '宮崎県', '鹿児島県', '沖縄県'
]

// アトラクション一覧を取得
const fetchAvailableAttractions = async () => {
  try {
    const response = await profileApi.getAvailableAttractions()
    if (response.data.success) {
      availableAttractions.value = response.data.data || []
    }
  } catch (error) {
    console.error('アトラクション一覧の取得に失敗しました:', error)
  }
}

// 検索実行
const handleSearch = async () => {
  const searchParams: UserProfileSearchRequest = {
    minAge: searchForm.value.minAge || undefined,
    maxAge: searchForm.value.maxAge || undefined,
    gender: searchForm.value.gender || undefined,
    hasUsjAnnualPass: searchForm.value.hasUsjAnnualPass || undefined,
    favoriteAttraction: searchForm.value.favoriteAttraction || undefined,
    locationPrefecture: searchForm.value.locationPrefecture || undefined
  }

  searchResults.value = await profileStore.searchProfiles(searchParams)
  hasSearched.value = true
}

// 年間パス保有者検索
const searchAnnualPassHolders = async () => {
  searchResults.value = await profileStore.getAnnualPassHolders()
  hasSearched.value = true
}

// 検索クリア
const clearSearch = () => {
  searchForm.value = {
    minAge: undefined,
    maxAge: undefined,
    gender: '',
    hasUsjAnnualPass: undefined,
    favoriteAttraction: '',
    locationPrefecture: ''
  }
  searchResults.value = []
  hasSearched.value = false
}

// 初期化
onMounted(() => {
  fetchAvailableAttractions()
})
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 1rem;
}

.search-container {
  max-width: 1000px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  padding: 2rem;
}

.search-header {
  text-align: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f3f4f6;
}

.search-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.search-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
  margin: 0;
}

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

.form-input {
  padding: 0.75rem;
  border: 2px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.age-range {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.age-range .form-input {
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

.quick-search {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f9fafb;
  border-radius: 8px;
}

.quick-search h3 {
  margin: 0 0 1rem 0;
  color: #374151;
  font-size: 1.125rem;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.search-results {
  margin-top: 2rem;
}

.search-results h3 {
  color: #374151;
  margin-bottom: 1.5rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.profile-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.profile-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.profile-card-header {
  margin-bottom: 1rem;
}

.profile-card-header h4 {
  margin: 0 0 0.5rem 0;
  color: #1f2937;
  font-size: 1.1rem;
}

.profile-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.annual-pass-badge {
  background: #dcfce7;
  color: #166534;
}

.age-badge {
  background: #dbeafe;
  color: #1e40af;
}

.gender-badge {
  background: #fce7f3;
  color: #be185d;
}

.profile-detail {
  margin-bottom: 1rem;
}

.profile-detail:last-child {
  margin-bottom: 0;
}

.profile-detail strong {
  display: block;
  color: #374151;
  margin-bottom: 0.5rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.tag {
  background: #f3f4f6;
  color: #374151;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.attraction-tag {
  background: #dbeafe;
  color: #1e40af;
}

.hobby-tag {
  background: #fef3c7;
  color: #92400e;
}

.no-results {
  text-align: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-results h3 {
  color: #374151;
  margin-bottom: 0.5rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.btn-secondary {
  background: #6b7280;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #4b5563;
}

.btn-outline-primary {
  background: transparent;
  color: #3b82f6;
  border: 2px solid #3b82f6;
}

.btn-outline-primary:hover:not(:disabled) {
  background: #3b82f6;
  color: white;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.alert {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.alert-error {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

@media (max-width: 768px) {
  .search-page {
    padding: 1rem;
  }
  
  .card {
    padding: 1.5rem;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

.user-id {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: normal;
}
</style> 