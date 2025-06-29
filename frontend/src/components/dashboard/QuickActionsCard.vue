<template>
  <BaseCard>
    <template #header>
      <h2 class="card-title">クイックアクション</h2>
    </template>

    <div class="action-buttons">
      <BaseButton
        variant="primary"
        block
        class="action-btn"
        @click="$emit('navigate-to-profile')"
      >
        <span class="action-icon">👤</span>
        <span class="action-text">プロフィール設定</span>
        <small class="action-label">{{ hasProfile ? '編集' : '作成' }}</small>
      </BaseButton>

      <BaseButton
        variant="secondary"
        block
        class="action-btn"
        :disabled="true"
      >
        <span class="action-icon">🎪</span>
        <span class="action-text">チャットルーム作成</span>
        <small class="action-label">(開発中)</small>
      </BaseButton>

      <BaseButton
        variant="secondary"
        block
        class="action-btn"
        :class="{ 'disabled': !isProfileComplete }"
        @click="$emit('navigate-to-search')"
        :disabled="!isProfileComplete"
      >
        <span class="action-icon">🔍</span>
        <span class="action-text">仲間探し</span>
        <small v-if="isProfileComplete" class="action-label">検索</small>
        <small v-else class="action-label restriction-text">プロフィール完了が必要</small>
      </BaseButton>

      <BaseButton
        variant="secondary"
        block
        class="action-btn"
        :disabled="true"
      >
        <span class="action-icon">👥</span>
        <span class="action-text">フレンド管理</span>
        <small class="action-label">(開発中)</small>
      </BaseButton>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { BaseCard, BaseButton } from '@/components/base'

interface Props {
  hasProfile: boolean
  isProfileComplete: boolean
}

defineProps<Props>()

defineEmits<{
  'navigate-to-profile': []
  'navigate-to-search': []
}>()
</script>

<style scoped>
.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem !important;
  text-align: left !important;
  position: relative;
  justify-content: flex-start !important;
  height: auto !important;
  min-height: 60px;
}

.action-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.action-text {
  flex: 1;
  font-weight: 600;
}

.action-label {
  position: absolute;
  right: 1rem;
  font-size: 0.75rem;
  background: rgba(0, 0, 0, 0.1);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  white-space: nowrap;
}

.restriction-text {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1) !important;
}

.action-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn:disabled .action-label {
  background: rgba(156, 163, 175, 0.3);
}

@media (max-width: 768px) {
  .action-btn {
    padding: 0.75rem !important;
    min-height: 50px;
  }
  
  .action-icon {
    font-size: 1.25rem;
  }
  
  .action-label {
    font-size: 0.7rem;
    padding: 0.15rem 0.4rem;
  }
}
</style> 