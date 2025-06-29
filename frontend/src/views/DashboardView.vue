<template>
  <div class="dashboard">
    <DashboardHeader 
      :display-name="profile?.displayName || user?.email?.split('@')[0]" 
    />

    <div class="dashboard-content">
      <div class="dashboard-grid">
        <DashboardProfileCard
          :profile="profile"
          :user-email="user?.email"
          :formatted-created-at="formattedCreatedAt"
          :is-profile-complete="isProfileComplete"
          :has-profile="hasProfile"
          :missing-fields="missingFields"
          @navigate-to-profile="navigateToProfile"
        />

        <QuickActionsCard
          :has-profile="hasProfile"
          :is-profile-complete="isProfileComplete"
          @navigate-to-profile="navigateToProfile"
          @navigate-to-search="navigateToSearch"
        />

        <AnnouncementsCard />

        <StatsCard
          :stats="stats"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProfileStore } from '@/stores/profile'
import { 
  DashboardHeader, 
  DashboardProfileCard, 
  QuickActionsCard, 
  AnnouncementsCard, 
  StatsCard 
} from '@/components/dashboard'

const router = useRouter()
const authStore = useAuthStore()
const profileStore = useProfileStore()

const user = computed(() => authStore.user)
const profile = computed(() => profileStore.profile)
const isProfileComplete = computed(() => profileStore.isProfileComplete)
const hasProfile = computed(() => profileStore.hasProfile)
const missingFields = computed(() => profileStore.missingFields)

// 統計情報（現在は全て0だが、将来的にAPIから取得）
const stats = computed(() => ({
  chatRooms: 0,
  messages: 0,
  friends: 0
}))

const formattedCreatedAt = computed(() => {
  if (!user.value?.createdAt) return ''
  return new Date(user.value.createdAt).toLocaleDateString('ja-JP')
})

const navigateToProfile = () => {
  router.push('/profile')
}

const navigateToSearch = () => {
  if (!isProfileComplete.value) {
    router.push({ 
      name: 'profile', 
      query: { 
        incomplete: 'true',
        missing: missingFields.value.join(','),
        from: 'profile-search'
      }
    })
  } else {
    router.push('/profile/search')
  }
}

onMounted(() => {
  // ユーザー情報を最新に更新
  if (!user.value) {
    authStore.getCurrentUser()
  }
  
  // プロフィール情報を取得
  profileStore.getMyProfile()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 1rem;
}

.dashboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  align-items: start;
}

@media (max-width: 768px) {
  .dashboard {
    padding: 1rem 0.5rem;
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}
</style> 