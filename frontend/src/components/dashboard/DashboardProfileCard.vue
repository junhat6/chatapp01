<template>
  <BaseCard>
    <template #header>
      <h2 class="card-title">プロフィール</h2>
    </template>

    <!-- プロフィール完了ステータス -->
    <div class="profile-status">
      <BaseAlert
        v-if="isProfileComplete"
        type="success"
        message="✅ プロフィール設定完了"
      />
      <BaseAlert
        v-else-if="hasProfile"
        type="warning"
        title="⚠️ プロフィール未完了"
      >
        <div class="missing-info">
          必須項目: {{ missingFields.join(', ') }}
        </div>
        <BaseButton
          variant="outline-primary"
          size="sm"
          @click="$emit('navigate-to-profile')"
          class="complete-button"
        >
          プロフィールを完成させる
        </BaseButton>
      </BaseAlert>
      <BaseAlert
        v-else
        type="error"
        title="❌ プロフィール未作成"
      >
        <BaseButton
          variant="outline-primary"
          size="sm"
          @click="$emit('navigate-to-profile')"
          class="create-button"
        >
          プロフィールを作成する
        </BaseButton>
      </BaseAlert>
    </div>
    
    <div class="profile-info">
      <div class="profile-item" v-if="profile">
        <strong>表示名:</strong> {{ profile.displayName }}
      </div>
      <div class="profile-item" v-else>
        <strong>表示名:</strong> <span class="no-data">未設定</span>
      </div>
      <div class="profile-item">
        <strong>メールアドレス:</strong> {{ userEmail }}
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

    <template #footer>
      <div class="profile-actions">
        <BaseButton
          variant="primary"
          block
          @click="$emit('navigate-to-profile')"
        >
          {{ profile ? 'プロフィール編集' : 'プロフィール作成' }}
        </BaseButton>
      </div>
    </template>
  </BaseCard>
</template>

<script setup lang="ts">
import { BaseCard, BaseAlert, BaseButton } from '@/components/base'
import type { UserProfile } from '@/types'

interface Props {
  profile?: UserProfile | null
  userEmail?: string
  formattedCreatedAt: string
  isProfileComplete: boolean
  hasProfile: boolean
  missingFields: string[]
}

defineProps<Props>()

defineEmits<{
  'navigate-to-profile': []
}>()
</script>

<style scoped>
.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.profile-status {
  margin-bottom: 1.5rem;
}

.profile-info {
  margin-bottom: 1rem;
}

.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0.75rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.profile-item:last-child {
  border-bottom: none;
}

.profile-item strong {
  color: #374151;
  font-weight: 600;
  min-width: 120px;
  flex-shrink: 0;
}

.profile-item span {
  color: #6b7280;
  text-align: right;
  flex: 1;
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
  justify-content: flex-end;
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

.missing-info {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.complete-button,
.create-button {
  margin-top: 0.5rem;
}

.profile-actions {
  padding-top: 0;
}

@media (max-width: 768px) {
  .profile-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .profile-item strong {
    min-width: auto;
  }
  
  .profile-item span {
    text-align: left;
  }
  
  .mini-tags {
    justify-content: flex-start;
  }
}
</style> 