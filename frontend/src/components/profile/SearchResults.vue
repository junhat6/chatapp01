<template>
  <div v-if="results.length > 0" class="search-results">
    <h3>検索結果 ({{ results.length }}件)</h3>
    <div class="profile-grid">
      <ProfileCard
        v-for="profile in results"
        :key="profile.userId"
        :profile="profile"
      />
    </div>
  </div>

  <!-- 検索結果なし -->
  <div v-else-if="hasSearched && !loading" class="no-results">
    <div class="no-results-icon">🔍</div>
    <h3>検索結果が見つかりませんでした</h3>
    <p>検索条件を変更して再度お試しください。</p>
  </div>
</template>

<script setup lang="ts">
import ProfileCard from './ProfileCard.vue'
import type { UserProfile } from '@/types'

interface Props {
  results: UserProfile[]
  hasSearched: boolean
  loading: boolean
}

defineProps<Props>()
</script>

<style scoped>
.search-results {
  margin-top: 2rem;
}

.search-results h3 {
  color: #374151;
  margin-bottom: 1.5rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.no-results {
  text-align: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-results h3 {
  color: #374151;
  margin-bottom: 0.5rem;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style> 