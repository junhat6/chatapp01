<template>
  <div class="profile-header">
    <div class="header-top">
      <h1 class="profile-title">
        {{ isEditing ? 'プロフィール編集' : (profile ? 'プロフィール' : 'プロフィール作成') }}
      </h1>
      <div class="profile-actions" v-if="profile && !isEditing">
        <BaseButton variant="primary" @click="$emit('startEdit')">
          編集
        </BaseButton>
        <BaseButton variant="danger" @click="$emit('deleteProfile')">
          削除
        </BaseButton>
      </div>
    </div>
    
    <!-- プロフィール未完了の警告メッセージ -->
    <BaseAlert
      v-if="isIncompleteAccess"
      type="warning"
      title="⚠️ プロフィール設定が未完了です"
    >
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
    </BaseAlert>
    
    <!-- プロフィール完了ステータス表示（編集モードでない場合のみ） -->
    <div v-else-if="completionStatus && !isEditing" class="completion-status">
      <BaseAlert
        v-if="!isProfileComplete"
        type="warning"
        :message="`⚠️ プロフィール未完了 - 必須項目: ${missingFields.join(', ')}`"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { BaseButton, BaseAlert } from '@/components/base'
import type { UserProfile, ProfileCompletionStatus } from '@/types'

interface Props {
  profile?: UserProfile | null
  isEditing: boolean
  isIncompleteAccess: boolean
  fromRoute?: string
  missingFieldsFromQuery: string[]
  completionStatus?: ProfileCompletionStatus | null
  isProfileComplete: boolean
  missingFields: string[]
}

defineProps<Props>()

defineEmits<{
  startEdit: []
  deleteProfile: []
}>()

// ルート名から表示名を取得する関数
const getRouteDisplayName = (routeName: string): string => {
  const routeNames: Record<string, string> = {
    'profile-search': '仲間探し',
    'dashboard': 'ダッシュボード',
    'chat': 'チャット'
  }
  return routeNames[routeName] || routeName
}
</script>

<style scoped>
.profile-header {
  margin-bottom: 2rem;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.profile-title {
  font-size: 2rem;
  color: #333;
  margin: 0;
}

.profile-actions {
  display: flex;
  gap: 0.75rem;
}

.missing-fields {
  margin-top: 1rem;
}

.missing-fields ul {
  margin: 0.5rem 0 0 1.5rem;
}

.original-destination {
  margin-top: 1rem;
  font-style: italic;
}

.completion-status {
  margin-bottom: 1rem;
}

@media (max-width: 768px) {
  .header-top {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .profile-title {
    font-size: 1.5rem;
  }
}
</style> 