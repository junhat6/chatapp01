<template>
  <div class="profile-display">
    <div class="profile-grid">
      <div class="profile-item">
        <strong>表示名:</strong>
        <span>{{ profile.displayName }}</span>
      </div>
      <div class="profile-item" v-if="profile.bio">
        <strong>自己紹介:</strong>
        <span>{{ profile.bio }}</span>
      </div>
      <div class="profile-item">
        <strong>年齢:</strong>
        <span>{{ profile.age || '未設定' }}{{ profile.age ? '歳' : '' }}</span>
      </div>
      <div class="profile-item">
        <strong>性別:</strong>
        <span>{{ profile.gender || '未設定' }}</span>
      </div>
      <div class="profile-item">
        <strong>年間パス:</strong>
        <span class="annual-pass" :class="{ 'has-pass': profile.hasUsjAnnualPass }">
          {{ profile.hasUsjAnnualPass ? '保有中 🎫' : '未保有' }}
        </span>
      </div>
      <div class="profile-item">
        <strong>都道府県:</strong>
        <span>{{ profile.locationPrefecture || '未設定' }}</span>
      </div>
    </div>

    <div class="profile-section">
      <strong>好きなアトラクション:</strong>
      <div class="tags">
        <span v-for="attraction in profile.favoriteAttractions" :key="attraction" class="tag attraction-tag">
          🎢 {{ attraction }}
        </span>
        <span v-if="profile.favoriteAttractions.length === 0" class="no-data">
          未設定
        </span>
      </div>
    </div>

    <div class="profile-section">
      <strong>趣味:</strong>
      <div class="tags">
        <span v-for="hobby in profile.hobbies" :key="hobby" class="tag hobby-tag">
          ⭐ {{ hobby }}
        </span>
        <span v-if="profile.hobbies.length === 0" class="no-data">
          未設定
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { UserProfile } from '@/types'

interface Props {
  profile: UserProfile
}

defineProps<Props>()
</script>

<style scoped>
.profile-display {
  margin-bottom: 2rem;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.profile-item {
  display: flex;
  flex-direction: column;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #1976d2;
}

.profile-item strong {
  color: #333;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.profile-item span {
  color: #666;
  line-height: 1.5;
}

.annual-pass {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-weight: 500;
  font-size: 0.875rem;
  background-color: #f5f5f5;
  color: #666;
  display: inline-block;
}

.annual-pass.has-pass {
  background-color: #e8f5e8;
  color: #2e7d32;
}

.profile-section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.profile-section strong {
  display: block;
  margin-bottom: 1rem;
  color: #333;
  font-weight: 600;
  font-size: 1.1rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  display: inline-block;
}

.attraction-tag {
  background-color: #e3f2fd;
  color: #1976d2;
  border: 1px solid #bbdefb;
}

.hobby-tag {
  background-color: #f3e5f5;
  color: #7b1fa2;
  border: 1px solid #ce93d8;
}

.no-data {
  color: #999;
  font-style: italic;
  padding: 0.5rem 0;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
  
  .profile-section {
    padding: 1rem;
  }
}
</style> 