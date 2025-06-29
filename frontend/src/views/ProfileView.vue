<template>
  <div class="profile-page">
    <div class="profile-container">
      <BaseCard>
        <ProfileHeader
          :profile="profile"
          :is-editing="isEditing"
          :is-incomplete-access="isIncompleteAccess"
          :from-route="fromRoute"
          :missing-fields-from-query="missingFieldsFromQuery"
          :completion-status="completionStatus"
          :is-profile-complete="isProfileComplete"
          :missing-fields="missingFields"
          @start-edit="startEditing"
          @delete-profile="deleteProfileConfirm"
        />

        <!-- エラー表示 -->
        <BaseAlert
          v-if="error"
          type="error"
          :message="error"
          :show="!!error"
        />

        <!-- プロフィール表示モード -->
        <ProfileDisplay
          v-if="profile && !isEditing"
          :profile="profile"
        />

        <!-- プロフィール作成・編集フォーム -->
        <ProfileForm
          v-if="!profile || isEditing"
          :profile="profile"
          :loading="loading"
          :is-editing="isEditing"
          @submit="handleSubmit"
          @cancel="cancelEditing"
        />
      </BaseCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProfileStore } from '@/stores/profile'
import { BaseCard, BaseAlert } from '@/components/base'
import { ProfileHeader, ProfileDisplay, ProfileForm } from '@/components/profile'
import type { CreateUserProfileRequest, UpdateUserProfileRequest } from '@/types'

const router = useRouter()
const route = useRoute()
const profileStore = useProfileStore()

const profile = computed(() => profileStore.profile)
const loading = computed(() => profileStore.loading)
const error = computed(() => profileStore.error)
const completionStatus = computed(() => profileStore.completionStatus)
const isProfileComplete = computed(() => profileStore.isProfileComplete)
const missingFields = computed(() => profileStore.missingFields)

// URL クエリパラメータから情報を取得
const isIncompleteAccess = computed(() => route.query.incomplete === 'true')
const fromRoute = computed(() => route.query.from as string)
const missingFieldsFromQuery = computed(() => {
  const missing = route.query.missing as string
  return missing ? missing.split(',') : []
})

const isEditing = ref(false)

// 編集開始
const startEditing = () => {
  isEditing.value = true
}

// 編集キャンセル
const cancelEditing = () => {
  isEditing.value = false
}

// フォーム送信
const handleSubmit = async (formData: CreateUserProfileRequest) => {
  let success = false
  
  if (profile.value) {
    // 更新
    const updateData: UpdateUserProfileRequest = {
      displayName: formData.displayName || undefined,
      profileImage: formData.profileImage || undefined,
      bio: formData.bio || undefined,
      age: formData.age || undefined,
      gender: formData.gender || undefined,
      hasUsjAnnualPass: formData.hasUsjAnnualPass,
      favoriteAttractions: formData.favoriteAttractions,
      locationPrefecture: formData.locationPrefecture || undefined,
      hobbies: formData.hobbies
    }
    success = await profileStore.updateProfile(updateData)
  } else {
    // 作成
    success = await profileStore.createProfile(formData)
  }

  if (success) {
    isEditing.value = false
    
    // プロフィール完了後、元のページがあればリダイレクト
    if (isIncompleteAccess.value && fromRoute.value) {
      router.push({ name: fromRoute.value })
    }
  }
}

// プロフィール削除確認
const deleteProfileConfirm = () => {
  if (confirm('プロフィールを削除してもよろしいですか？この操作は取り消せません。')) {
    profileStore.deleteProfile()
  }
}

// 初期化
onMounted(() => {
  profileStore.getMyProfile()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem 1rem;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 1rem 0.5rem;
  }
}
</style> 