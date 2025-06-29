<template>
  <div class="profile-card">
    <div class="profile-card-header">
      <h4>{{ profile.displayName }}</h4>
      <span class="user-id">ID: {{ profile.userId }}</span>
      <div class="profile-badges">
        <span v-if="profile.hasUsjAnnualPass" class="badge annual-pass-badge">
          🎫 年間パス
        </span>
        <span v-if="profile.age" class="badge age-badge">
          {{ profile.age }}歳
        </span>
        <span v-if="profile.gender" class="badge gender-badge">
          {{ profile.gender }}
        </span>
      </div>
    </div>

    <div class="profile-card-content">
      <div v-if="profile.locationPrefecture" class="profile-detail">
        <strong>📍 都道府県:</strong> {{ profile.locationPrefecture }}
      </div>

      <div v-if="profile.favoriteAttractions.length > 0" class="profile-detail">
        <strong>🎢 好きなアトラクション:</strong>
        <div class="tags">
          <span v-for="attraction in profile.favoriteAttractions" :key="attraction" class="tag attraction-tag">
            {{ attraction }}
          </span>
        </div>
      </div>

      <div v-if="profile.hobbies.length > 0" class="profile-detail">
        <strong>⭐ 趣味:</strong>
        <div class="tags">
          <span v-for="hobby in profile.hobbies" :key="hobby" class="tag hobby-tag">
            {{ hobby }}
          </span>
        </div>
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
.profile-card {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.profile-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.profile-card-header {
  margin-bottom: 1rem;
}

.profile-card-header h4 {
  margin: 0 0 0.5rem 0;
  color: #1f2937;
  font-size: 1.1rem;
}

.user-id {
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: normal;
}

.profile-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.annual-pass-badge {
  background: #dcfce7;
  color: #166534;
}

.age-badge {
  background: #dbeafe;
  color: #1e40af;
}

.gender-badge {
  background: #fce7f3;
  color: #be185d;
}

.profile-detail {
  margin-bottom: 1rem;
}

.profile-detail:last-child {
  margin-bottom: 0;
}

.profile-detail strong {
  display: block;
  color: #374151;
  margin-bottom: 0.5rem;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.tag {
  background: #f3f4f6;
  color: #374151;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.75rem;
}

.attraction-tag {
  background: #dbeafe;
  color: #1e40af;
}

.hobby-tag {
  background: #fef3c7;
  color: #92400e;
}
</style> 