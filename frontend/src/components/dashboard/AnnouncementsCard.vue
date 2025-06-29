<template>
  <BaseCard>
    <template #header>
      <h2 class="card-title">🎢 USJ Chat について</h2>
    </template>

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
  </BaseCard>
</template>

<script setup lang="ts">
import { BaseCard } from '@/components/base'
</script>

<style scoped>
.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.announcement-content {
  line-height: 1.6;
  color: #374151;
}

.announcement-content p {
  margin: 0 0 1rem 0;
}

.feature-list {
  margin: 1rem 0 0 1.5rem;
  padding: 0;
}

.feature-list li {
  margin-bottom: 0.5rem;
  color: #6b7280;
}

.feature-list li:last-child {
  margin-bottom: 0;
}
</style> 