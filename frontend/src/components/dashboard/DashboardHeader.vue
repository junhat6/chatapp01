<template>
  <div class="dashboard-header">
    <h1 class="dashboard-title">ダッシュボード</h1>
    <p class="dashboard-subtitle">
      おかえりなさい、{{ displayName }}さん！
    </p>
  </div>
</template>

<script setup lang="ts">
interface Props {
  displayName?: string
}

const props = withDefaults(defineProps<Props>(), {
  displayName: 'ゲスト'
})
</script>

<style scoped>
.dashboard-header {
  margin-bottom: var(--space-8);
  text-align: center;
}

.dashboard-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-2) 0;
}

.dashboard-subtitle {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  margin: 0;
}

@media (max-width: 768px) {
  .dashboard-title {
    font-size: var(--font-size-2xl);
  }
  
  .dashboard-subtitle {
    font-size: var(--font-size-base);
  }
}
</style> 