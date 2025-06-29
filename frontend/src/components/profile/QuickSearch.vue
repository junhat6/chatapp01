<template>
  <div class="quick-search">
    <h3>クイック検索</h3>
    <div class="quick-buttons">
      <BaseButton
        variant="outline-primary"
        :disabled="loading"
        @click="$emit('annualPassSearch')"
      >
        🎫 年間パス保有者
      </BaseButton>
      <!-- 将来的に他のクイック検索ボタンを追加可能 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { BaseButton } from '@/components/base'

interface Props {
  loading: boolean
}

defineProps<Props>()

defineEmits<{
  annualPassSearch: []
}>()
</script>

<style scoped>
.quick-search {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: #f9fafb;
  border-radius: 8px;
}

.quick-search h3 {
  margin: 0 0 1rem 0;
  color: #374151;
  font-size: 1.125rem;
}

.quick-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
</style> 