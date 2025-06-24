<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1 class="dashboard-title">ダッシュボード</h1>
      <p class="dashboard-subtitle">
        おかえりなさい、{{ user?.displayName }}さん！
      </p>
    </div>

    <div class="dashboard-content">
      <div class="dashboard-grid">
        <!-- ユーザー情報カード -->
        <div class="card profile-card">
          <h2 class="card-title">プロフィール</h2>
          <div class="profile-info">
            <div class="profile-item">
              <strong>表示名:</strong> {{ user?.displayName }}
            </div>
            <div class="profile-item">
              <strong>メールアドレス:</strong> {{ user?.email }}
            </div>
            <div class="profile-item" v-if="user?.bio">
              <strong>自己紹介:</strong> {{ user?.bio }}
            </div>
            <div class="profile-item">
              <strong>登録日:</strong> {{ formattedCreatedAt }}
            </div>
          </div>
        </div>

        <!-- クイックアクションカード -->
        <div class="card quick-actions-card">
          <h2 class="card-title">クイックアクション</h2>
          <div class="action-buttons">
            <button class="btn btn-primary action-btn" disabled>
              <span class="action-icon">🎪</span>
              チャットルーム作成
              <small>(開発中)</small>
            </button>
            <button class="btn btn-secondary action-btn" disabled>
              <span class="action-icon">🔍</span>
              ルーム検索
              <small>(開発中)</small>
            </button>
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
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const user = computed(() => authStore.user)

const formattedCreatedAt = computed(() => {
  if (!user.value?.createdAt) return ''
  const date = new Date(user.value.createdAt)
  return date.toLocaleDateString('ja-JP', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
})

onMounted(() => {
  // ユーザー情報を最新に更新
  if (!user.value) {
    authStore.getCurrentUser()
  }
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
  font-size: 0.875rem;
  color: #666;
  margin-top: 0.25rem;
}

@media (max-width: 768px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-btn {
    font-size: 0.9rem;
  }
}
</style> 