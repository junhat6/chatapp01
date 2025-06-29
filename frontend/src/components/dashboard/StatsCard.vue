<template>
  <BaseCard>
    <template #header>
      <h2 class="card-title">統計情報</h2>
    </template>

    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-number">{{ stats.chatRooms }}</div>
        <div class="stat-label">チャットルーム</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ stats.messages }}</div>
        <div class="stat-label">メッセージ</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ stats.friends }}</div>
        <div class="stat-label">フレンド</div>
      </div>
    </div>
  </BaseCard>
</template>

<script setup lang="ts">
import { BaseCard } from '@/components/base'

interface Stats {
  chatRooms: number
  messages: number
  friends: number
}

interface Props {
  stats?: Stats
}

const props = withDefaults(defineProps<Props>(), {
  stats: () => ({
    chatRooms: 0,
    messages: 0,
    friends: 0
  })
})
</script>

<style scoped>
.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
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
  transition: transform 0.2s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #1976d2;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .stat-item {
    padding: 0.75rem;
  }
  
  .stat-number {
    font-size: 1.5rem;
  }
  
  .stat-label {
    font-size: 0.8rem;
  }
}
</style> 