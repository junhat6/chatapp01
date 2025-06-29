<template>
  <div class="search-page">
    <div class="search-container">
      <BaseCard>
        <SearchHeader />

        <SearchForm
          :loading="loading"
          @search="handleSearch"
          @clear="clearSearch"
        />

        <QuickSearch
          :loading="loading"
          @annual-pass-search="searchAnnualPassHolders"
        />

        <!-- エラー表示 -->
        <BaseAlert
          v-if="error"
          type="error"
          :message="error"
          :show="!!error"
        />

        <SearchResults
          :results="searchResults"
          :has-searched="hasSearched"
          :loading="loading"
        />
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useProfileStore } from '@/stores/profile'
import { BaseCard, BaseAlert } from '@/components/base'
import { SearchHeader, SearchForm, QuickSearch, SearchResults } from '@/components/profile'
import type { UserProfile, UserProfileSearchRequest } from '@/types'

const profileStore = useProfileStore()

const loading = computed(() => profileStore.loading)
const error = computed(() => profileStore.error)

const searchResults = ref<UserProfile[]>([])
const hasSearched = ref(false)

// 検索実行
const handleSearch = async (searchParams: UserProfileSearchRequest) => {
  searchResults.value = await profileStore.searchProfiles(searchParams)
  hasSearched.value = true
}

// 年間パス保有者検索
const searchAnnualPassHolders = async () => {
  searchResults.value = await profileStore.getAnnualPassHolders()
  hasSearched.value = true
}

// 検索クリア
const clearSearch = () => {
  searchResults.value = []
  hasSearched.value = false
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 1rem;
}

.search-container {
  max-width: 1000px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .search-page {
    padding: 1rem 0.5rem;
  }
}
</style> 