<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="card">
        <div class="profile-header">
          <h1 class="profile-title">
            {{ isEditing ? 'プロフィール編集' : (profile ? 'プロフィール' : 'プロフィール作成') }}
          </h1>
          <div class="profile-actions" v-if="profile && !isEditing">
            <button @click="startEditing" class="btn btn-primary">
              編集
            </button>
            <button @click="deleteProfileConfirm" class="btn btn-danger">
              削除
            </button>
          </div>
          
          <!-- プロフィール未完了の警告メッセージ -->
          <div v-if="isIncompleteAccess" class="incomplete-warning">
            <div class="warning-content">
              <h3>⚠️ プロフィール設定が未完了です</h3>
              <p>チャット機能や仲間探し機能を利用するには、プロフィールの設定を完了してください。</p>
              <div v-if="missingFieldsFromQuery.length > 0" class="missing-fields">
                <p><strong>必須項目：</strong></p>
                <ul>
                  <li v-for="field in missingFieldsFromQuery" :key="field">{{ field }}</li>
                </ul>
              </div>
              <div v-if="fromRoute" class="original-destination">
                <p>設定完了後、「{{ getRouteDisplayName(fromRoute) }}」ページに戻ることができます。</p>
              </div>
            </div>
          </div>
          
          <!-- プロフィール完了ステータス表示 -->
          <div v-else-if="completionStatus" class="completion-status">
            <div v-if="isProfileComplete" class="status-complete">
              ✅ プロフィール設定完了
            </div>
            <div v-else class="status-incomplete">
              ⚠️ プロフィール未完了 - 必須項目: {{ missingFields.join(', ') }}
            </div>
          </div>
        </div>

        <!-- エラー表示 -->
        <div v-if="error" class="alert alert-error">
          {{ error }}
        </div>

        <!-- プロフィール表示モード -->
        <div v-if="profile && !isEditing" class="profile-display">
          <div class="profile-grid">
            <div class="profile-item">
              <strong>表示名:</strong>
              <span>{{ profile.displayName }}</span>
            </div>
            <div class="profile-item" v-if="profile.bio">
              <strong>自己紹介:</strong>
              <span>{{ profile.bio }}</span>
            </div>
            <div class="profile-item">
              <strong>年齢:</strong>
              <span>{{ profile.age || '未設定' }}{{ profile.age ? '歳' : '' }}</span>
            </div>
            <div class="profile-item">
              <strong>性別:</strong>
              <span>{{ profile.gender || '未設定' }}</span>
            </div>
            <div class="profile-item">
              <strong>年間パス:</strong>
              <span class="annual-pass" :class="{ 'has-pass': profile.hasUsjAnnualPass }">
                {{ profile.hasUsjAnnualPass ? '保有中 🎫' : '未保有' }}
              </span>
            </div>
            <div class="profile-item">
              <strong>都道府県:</strong>
              <span>{{ profile.locationPrefecture || '未設定' }}</span>
            </div>
          </div>

          <div class="profile-section">
            <strong>好きなアトラクション:</strong>
            <div class="tags">
              <span v-for="attraction in profile.favoriteAttractions" :key="attraction" class="tag attraction-tag">
                🎢 {{ attraction }}
              </span>
              <span v-if="profile.favoriteAttractions.length === 0" class="no-data">
                未設定
              </span>
            </div>
          </div>

          <div class="profile-section">
            <strong>趣味:</strong>
            <div class="tags">
              <span v-for="hobby in profile.hobbies" :key="hobby" class="tag hobby-tag">
                ⭐ {{ hobby }}
              </span>
              <span v-if="profile.hobbies.length === 0" class="no-data">
                未設定
              </span>
            </div>
          </div>
        </div>

        <!-- プロフィール作成・編集フォーム -->
        <form v-if="!profile || isEditing" @submit.prevent="handleSubmit" class="profile-form">
          <div class="form-group">
            <label for="displayName" class="form-label">表示名 *</label>
            <input
              id="displayName"
              v-model="form.displayName"
              type="text"
              class="form-input"
              placeholder="表示名を入力"
              required
              :disabled="loading"
            />
          </div>

          <div class="form-group">
            <label for="bio" class="form-label">自己紹介</label>
            <textarea
              id="bio"
              v-model="form.bio"
              class="form-input"
              placeholder="自己紹介を入力してください"
              rows="3"
              :disabled="loading"
            ></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label for="age" class="form-label">年齢</label>
              <input
                id="age"
                v-model.number="form.age"
                type="number"
                class="form-input"
                placeholder="年齢を入力"
                min="13"
                max="120"
                :disabled="loading"
              />
            </div>

            <div class="form-group">
              <label for="gender" class="form-label">性別</label>
              <select
                id="gender"
                v-model="form.gender"
                class="form-input"
                :disabled="loading"
              >
                <option value="">選択してください</option>
                <option value="男性">男性</option>
                <option value="女性">女性</option>
                <option value="その他">その他</option>
                <option value="回答しない">回答しない</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label class="form-label checkbox-label">
                <input
                  v-model="form.hasUsjAnnualPass"
                  type="checkbox"
                  :disabled="loading"
                />
                USJ年間パスを持っています
              </label>
            </div>

            <div class="form-group">
              <label for="locationPrefecture" class="form-label">都道府県</label>
              <select
                id="locationPrefecture"
                v-model="form.locationPrefecture"
                class="form-input"
                :disabled="loading"
              >
                <option value="">選択してください</option>
                <option v-for="prefecture in prefectures" :key="prefecture" :value="prefecture">
                  {{ prefecture }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label for="favoriteAttractions" class="form-label">好きなアトラクション</label>
            <div class="attraction-input-section">
              <div class="input-with-button">
                <input
                  v-model="newAttraction"
                  type="text"
                  placeholder="アトラクション名を入力"
                  class="form-input"
                  @keyup.enter="addAttraction"
                  :disabled="loading"
                />
                <button
                  type="button"
                  @click="addAttraction"
                  class="btn btn-secondary btn-sm"
                  :disabled="loading || !newAttraction.trim()"
                >
                  追加
                </button>
              </div>
              <div class="tags">
                <span v-for="attraction in form.favoriteAttractions" :key="attraction" class="tag attraction-tag">
                  🎢 {{ attraction }}
                  <button
                    type="button"
                    @click="removeAttraction(attraction)"
                    class="tag-remove"
                    :disabled="loading"
                  >
                    ×
                  </button>
                </span>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label for="hobbies" class="form-label">趣味</label>
            <div class="hobby-input-section">
              <div class="input-with-button">
                <input
                  v-model="newHobby"
                  type="text"
                  placeholder="趣味を入力"
                  class="form-input"
                  @keyup.enter="addHobby"
                  :disabled="loading"
                />
                <button
                  type="button"
                  @click="addHobby"
                  class="btn btn-secondary btn-sm"
                  :disabled="loading || !newHobby.trim()"
                >
                  追加
                </button>
              </div>
              <div class="tags">
                <span v-for="hobby in form.hobbies" :key="hobby" class="tag hobby-tag">
                  ⭐ {{ hobby }}
                  <button
                    type="button"
                    @click="removeHobby(hobby)"
                    class="tag-remove"
                    :disabled="loading"
                  >
                    ×
                  </button>
                </span>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="loading"
            >
              {{ loading ? '処理中...' : (profile ? '更新' : '作成') }}
            </button>
            <button
              v-if="isEditing"
              type="button"
              @click="cancelEditing"
              class="btn btn-secondary"
              :disabled="loading"
            >
              キャンセル
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProfileStore } from '@/stores/profile'
import type { CreateUserProfileRequest, UpdateUserProfileRequest } from '@/types'

const router = useRouter()
const route = useRoute()
const profileStore = useProfileStore()

const profile = computed(() => profileStore.profile)
const loading = computed(() => profileStore.loading)
const error = computed(() => profileStore.error)
const completionStatus = computed(() => profileStore.completionStatus)
const isProfileComplete = computed(() => profileStore.isProfileComplete)
const missingFields = computed(() => profileStore.missingFields)

// URL クエリパラメータから情報を取得
const isIncompleteAccess = computed(() => route.query.incomplete === 'true')
const fromRoute = computed(() => route.query.from as string)
const missingFieldsFromQuery = computed(() => {
  const missing = route.query.missing as string
  return missing ? missing.split(',') : []
})

// ルート名から表示名を取得する関数
const getRouteDisplayName = (routeName: string): string => {
  const routeNames: Record<string, string> = {
    'profile-search': '仲間探し',
    'dashboard': 'ダッシュボード',
    'chat': 'チャット'
  }
  return routeNames[routeName] || routeName
}

const isEditing = ref(false)
const newAttraction = ref('')
const newHobby = ref('')

// フォームデータ
const form = ref<CreateUserProfileRequest>({
  displayName: '',
  profileImage: '',
  bio: '',
  age: undefined,
  gender: '',
  hasUsjAnnualPass: false,
  favoriteAttractions: [],
  locationPrefecture: '',
  hobbies: []
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

// アトラクション追加
const addAttraction = () => {
  const attraction = newAttraction.value.trim()
  if (attraction && !form.value.favoriteAttractions.includes(attraction)) {
    form.value.favoriteAttractions.push(attraction)
    newAttraction.value = ''
  }
}

// アトラクション削除
const removeAttraction = (attraction: string) => {
  const index = form.value.favoriteAttractions.indexOf(attraction)
  if (index > -1) {
    form.value.favoriteAttractions.splice(index, 1)
  }
}

// 趣味追加
const addHobby = () => {
  const hobby = newHobby.value.trim()
  if (hobby && !form.value.hobbies.includes(hobby)) {
    form.value.hobbies.push(hobby)
    newHobby.value = ''
  }
}

// 趣味削除
const removeHobby = (hobby: string) => {
  const index = form.value.hobbies.indexOf(hobby)
  if (index > -1) {
    form.value.hobbies.splice(index, 1)
  }
}

// 編集開始
const startEditing = () => {
  if (profile.value) {
    form.value = {
      displayName: profile.value.displayName,
      profileImage: profile.value.profileImage || '',
      bio: profile.value.bio || '',
      age: profile.value.age,
      gender: profile.value.gender || '',
      hasUsjAnnualPass: profile.value.hasUsjAnnualPass,
      favoriteAttractions: [...profile.value.favoriteAttractions],
      locationPrefecture: profile.value.locationPrefecture || '',
      hobbies: [...profile.value.hobbies]
    }
    isEditing.value = true
  }
}

// 編集キャンセル
const cancelEditing = () => {
  isEditing.value = false
  // フォームをリセット
  form.value = {
    displayName: '',
    profileImage: '',
    bio: '',
    age: undefined,
    gender: '',
    hasUsjAnnualPass: false,
    favoriteAttractions: [],
    locationPrefecture: '',
    hobbies: []
  }
}

// フォーム送信
const handleSubmit = async () => {
  let success = false
  if (profile.value) {
    // 更新
    const updateData: UpdateUserProfileRequest = {
      displayName: form.value.displayName || undefined,
      profileImage: form.value.profileImage || undefined,
      bio: form.value.bio || undefined,
      age: form.value.age || undefined,
      gender: form.value.gender || undefined,
      hasUsjAnnualPass: form.value.hasUsjAnnualPass,
      favoriteAttractions: form.value.favoriteAttractions,
      locationPrefecture: form.value.locationPrefecture || undefined,
      hobbies: form.value.hobbies
    }
    success = await profileStore.updateProfile(updateData)
  } else {
    // 作成
    const createData: CreateUserProfileRequest = {
      displayName: form.value.displayName,
      profileImage: form.value.profileImage || undefined,
      bio: form.value.bio || undefined,
      age: form.value.age || undefined,
      gender: form.value.gender || undefined,
      hasUsjAnnualPass: form.value.hasUsjAnnualPass,
      favoriteAttractions: form.value.favoriteAttractions,
      locationPrefecture: form.value.locationPrefecture || undefined,
      hobbies: form.value.hobbies
    }
    success = await profileStore.createProfile(createData)
  }

  if (success) {
    isEditing.value = false
    
    // プロフィール完了後、元のページがあればリダイレクト
    if (isIncompleteAccess.value && fromRoute.value) {
      router.push({ name: fromRoute.value })
    }
  }
}

// プロフィール削除確認
const deleteProfileConfirm = () => {
  if (confirm('プロフィールを削除してもよろしいですか？この操作は取り消せません。')) {
    profileStore.deleteProfile()
  }
}

// 初期化
onMounted(() => {
  profileStore.getMyProfile()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 1rem;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  padding: 2rem;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f3f4f6;
}

.profile-title {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.profile-actions {
  display: flex;
  gap: 0.5rem;
}

.profile-display {
  space-y: 1.5rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.profile-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.profile-item strong {
  color: #374151;
  font-weight: 600;
}

.profile-item span {
  color: #6b7280;
  font-size: 1.1rem;
}

.annual-pass.has-pass {
  color: #059669;
  font-weight: 600;
}

.profile-section {
  margin-bottom: 1.5rem;
}

.profile-section strong {
  display: block;
  color: #374151;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  background: #f3f4f6;
  color: #374151;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.attraction-tag {
  background: #dbeafe;
  color: #1e40af;
}

.hobby-tag {
  background: #fef3c7;
  color: #92400e;
}

.tag-remove {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: none;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  margin-left: 0.25rem;
}

.tag-remove:hover {
  background: rgba(239, 68, 68, 0.2);
}

.no-data {
  color: #9ca3af;
  font-style: italic;
}

.profile-form {
  space-y: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
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

.checkbox-label {
  flex-direction: row;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
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

.input-with-button {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.input-with-button .form-input {
  flex: 1;
}

.attraction-input-section,
.hobby-input-section {
  space-y: 0.75rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  padding-top: 1.5rem;
  border-top: 2px solid #f3f4f6;
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

.btn-danger {
  background: #ef4444;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
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
  .profile-page {
    padding: 1rem;
  }
  
  .card {
    padding: 1.5rem;
  }
  
  .profile-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .profile-actions {
    justify-content: center;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

.main-content {
  min-height: calc(100vh - 80px);
}

.incomplete-warning {
  background: linear-gradient(135deg, #ffeb3b 0%, #ff9800 100%);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 6px rgba(255, 152, 0, 0.2);
}

.warning-content h3 {
  margin: 0 0 1rem 0;
  color: #e65100;
  font-size: 1.2rem;
}

.warning-content p {
  margin: 0.5rem 0;
  color: #e65100;
  font-weight: 500;
}

.missing-fields {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 8px;
}

.missing-fields ul {
  margin: 0.5rem 0 0 1rem;
  color: #d84315;
}

.original-destination {
  margin-top: 1rem;
  padding: 0.8rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  border-left: 4px solid #e65100;
}

.completion-status {
  margin-bottom: 1.5rem;
}

.status-complete {
  background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%);
  color: white;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}

.status-incomplete {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  color: white;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  font-weight: 500;
}
</style> 