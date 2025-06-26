<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1 class="dashboard-title">ダッシュボード</h1>
      <p class="dashboard-subtitle">
        おかえりなさい、{{ profile?.displayName }}さん！
      </p>
    </div>

    <div class="dashboard-content">
      <div class="dashboard-grid">
        <!-- ユーザー情報カード -->
        <div class="card profile-card">
          <h2 class="card-title">プロフィール</h2>
          
          <!-- プロフィール完了ステータス -->
          <div class="profile-status">
            <div v-if="isProfileComplete" class="status-complete">
              ✅ プロフィール設定完了
            </div>
            <div v-else-if="hasProfile" class="status-incomplete">
              ⚠️ プロフィール未完了
              <div class="missing-info">
                必須項目: {{ missingFields.join(', ') }}
              </div>
              <button @click="navigateToProfile" class="complete-button">
                プロフィールを完成させる
              </button>
            </div>
            <div v-else class="status-no-profile">
              ❌ プロフィール未作成
              <button @click="navigateToProfile" class="create-button">
                プロフィールを作成する
              </button>
            </div>
          </div>
          
          <div class="profile-info">
            <div class="profile-item" v-if="profile">
              <strong>表示名:</strong> {{ profile.displayName }}
            </div>
            <div class="profile-item" v-else>
              <strong>表示名:</strong> <span class="no-data">未設定</span>
            </div>
            <div class="profile-item">
              <strong>メールアドレス:</strong> {{ user?.email }}
            </div>
            <div class="profile-item" v-if="profile?.bio">
              <strong>自己紹介:</strong> {{ profile.bio }}
            </div>
            <div class="profile-item">
              <strong>登録日:</strong> {{ formattedCreatedAt }}
            </div>
            
            <!-- プロフィール詳細情報 -->
            <div v-if="profile" class="profile-details">
              <div class="profile-item" v-if="profile.age">
                <strong>年齢:</strong> {{ profile.age }}歳
              </div>
              <div class="profile-item" v-if="profile.hasUsjAnnualPass">
                <strong>年間パス:</strong> 
                <span class="annual-pass">保有中 🎫</span>
              </div>
              <div class="profile-item" v-if="profile.favoriteAttractions.length > 0">
                <strong>好きなアトラクション:</strong>
                <div class="mini-tags">
                  <span v-for="attraction in profile.favoriteAttractions.slice(0, 3)" :key="attraction" class="mini-tag">
                    {{ attraction }}
                  </span>
                  <span v-if="profile.favoriteAttractions.length > 3" class="more-count">
                    +{{ profile.favoriteAttractions.length - 3 }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="profile-actions">
            <router-link to="/profile" class="btn btn-primary">
              {{ profile ? 'プロフィール編集' : 'プロフィール作成' }}
            </router-link>
          </div>
        </div>

        <!-- クイックアクションカード -->
        <div class="card quick-actions-card">
          <h2 class="card-title">クイックアクション</h2>
          <div class="action-buttons">
            <router-link to="/profile" class="btn btn-primary action-btn">
              <span class="action-icon">👤</span>
              プロフィール設定
              <small>{{ profile ? '編集' : '作成' }}</small>
            </router-link>
            <button class="btn btn-secondary action-btn" disabled>
              <span class="action-icon">🎪</span>
              チャットルーム作成
              <small>(開発中)</small>
            </button>
            <div 
              @click="navigateToSearch" 
              class="btn btn-secondary action-btn"
              :class="{ 'disabled': !isProfileComplete }"
            >
              <span class="action-icon">🔍</span>
              仲間探し
              <small v-if="isProfileComplete">検索</small>
              <small v-else class="restriction-text">プロフィール完了が必要</small>
            </div>
            <button class="btn btn-secondary action-btn" disabled>
              <span class="action-icon">👥</span>
              フレンド管理
              <small>(開発中)</small>
            </button>
          </div>
        </div>

        <!-- お知らせカード -->
        <div class="card announcements-card">
          <h2 class="card-title">🎢 USJ Chat について</h2>
          <div class="announcement-content">
            <p>
              USJ Chat は現在開発中のアプリケーションです。
              今後以下の機能を追加予定です：
            </p>
            <ul class="feature-list">
              <li>リアルタイムチャット機能</li>
              <li>位置情報ベースのマッチング</li>
              <li>グループチャット作成</li>
              <li>アトラクション待ち時間共有</li>
              <li>フレンド機能</li>
            </ul>
          </div>
        </div>

        <!-- 統計カード -->
        <div class="card stats-card">
          <h2 class="card-title">統計情報</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-number">0</div>
              <div class="stat-label">チャットルーム</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">0</div>
              <div class="stat-label">メッセージ</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">0</div>
              <div class="stat-label">フレンド</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'

const router = useRouter()
const authStore = useAuthStore()
const profileStore = useProfileStore()

const user = computed(() => authStore.user)
const profile = computed(() => profileStore.profile)
const isProfileComplete = computed(() => profileStore.isProfileComplete)
const hasProfile = computed(() => profileStore.hasProfile)
const missingFields = computed(() => profileStore.missingFields)

const formattedCreatedAt = computed(() => {
  if (!user.value?.createdAt) return ''
  return new Date(user.value.createdAt).toLocaleDateString('ja-JP')
})

const navigateToProfile = () => {
  router.push('/profile')
}

const navigateToSearch = () => {
  if (!isProfileComplete.value) {
    router.push({ 
      name: 'profile', 
      query: { 
        incomplete: 'true',
        missing: missingFields.value.join(','),
        from: 'profile-search'
      }
    })
  } else {
    router.push('/profile/search')
  }
}

onMounted(() => {
  // ユーザー情報を最新に更新
  if (!user.value) {
    authStore.getCurrentUser()
  }
  
  // プロフィール情報を取得
  profileStore.getMyProfile()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 2rem;
}

.dashboard-title {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.dashboard-subtitle {
  font-size: 1.2rem;
  color: #666;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.card-title {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: #333;
  border-bottom: 2px solid #1976d2;
  padding-bottom: 0.5rem;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.profile-item {
  padding: 0.5rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.profile-item:last-child {
  border-bottom: none;
}

.profile-details {
  border-top: 2px solid #e3f2fd;
  padding-top: 1rem;
  margin-top: 1rem;
}

.annual-pass {
  color: #4caf50;
  font-weight: 600;
}

.mini-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-top: 0.5rem;
}

.mini-tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.more-count {
  background: #f5f5f5;
  color: #666;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.no-data {
  color: #999;
  font-style: italic;
}

.profile-actions {
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  text-align: left;
  position: relative;
  text-decoration: none;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-icon {
  font-size: 1.5rem;
}

.action-btn small {
  position: absolute;
  right: 1rem;
  font-size: 0.75rem;
  background: rgba(0, 0, 0, 0.1);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
}

.announcement-content {
  line-height: 1.6;
}

.feature-list {
  margin-top: 1rem;
  padding-left: 1.5rem;
}

.feature-list li {
  margin-bottom: 0.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #1976d2;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 0 0.5rem;
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.profile-status {
  margin-bottom: 1.5rem;
  padding: 1rem;
  border-radius: 8px;
}

.status-complete {
  background: linear-gradient(135deg, #4caf50 0%, #8bc34a 100%);
  color: white;
  text-align: center;
  font-weight: 500;
}

.status-incomplete {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  color: white;
  text-align: center;
  font-weight: 500;
}

.status-no-profile {
  background: linear-gradient(135deg, #f44336 0%, #e57373 100%);
  color: white;
  text-align: center;
  font-weight: 500;
}

.missing-info {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.complete-button, .create-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 2px solid white;
  border-radius: 6px;
  padding: 0.5rem 1rem;
  margin-top: 0.5rem;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.complete-button:hover, .create-button:hover {
  background: white;
  color: #f57c00;
}

.action-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  position: relative;
}

.action-btn.disabled:hover {
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.restriction-text {
  color: #ff5722;
  font-size: 0.8rem;
}
</style> 