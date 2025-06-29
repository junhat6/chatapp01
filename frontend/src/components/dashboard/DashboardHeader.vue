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
  margin-bottom: 2rem;
  text-align: center;
}

.dashboard-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 0.5rem 0;
}

.dashboard-subtitle {
  color: #6b7280;
  font-size: 1.1rem;
  margin: 0;
}

@media (max-width: 768px) {
  .dashboard-title {
    font-size: 1.5rem;
  }
  
  .dashboard-subtitle {
    font-size: 1rem;
  }
}
</style> 