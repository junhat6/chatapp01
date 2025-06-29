import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { profileApi } from '@/services/profileApi'
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useToast } from '@/composables/useToast'
import type { 
    UserProfile, 
    CreateUserProfileRequest, 
    UpdateUserProfileRequest, 
    UserProfileSearchRequest,
    ProfileCompletionStatus
} from '@/types'

export const useProfileStore = defineStore('profile', () => {
    const profile = ref<UserProfile | null>(null)
    const completionStatus = ref<ProfileCompletionStatus | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)

    // プロフィール完了チェック
    const isProfileComplete = computed(() => completionStatus.value?.isComplete ?? false)
    const hasProfile = computed(() => completionStatus.value?.hasProfile ?? false)
    const missingFields = computed(() => completionStatus.value?.missingFields ?? [])
    
    const { handleError, handleValidationError, handleServerError } = useErrorHandler()
    const toast = useToast()

    // プロフィール完了ステータス取得
    const getCompletionStatus = async (): Promise<void> => {
        try {
            const response = await profileApi.getCompletionStatus()
            
            if (response.data.success) {
                completionStatus.value = response.data.data || null
            } else {
                error.value = response.data.message
                handleError(new Error(response.data.message), { 
                    showToast: false // ステータス取得エラーは静かに処理
                })
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'ステータス取得に失敗しました'
            completionStatus.value = null
            handleError(err, { showToast: false })
        }
    }

    // プロフィール作成
    const createProfile = async (data: CreateUserProfileRequest): Promise<boolean> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.createProfile(data)
            
            if (response.data.success && response.data.data) {
                profile.value = response.data.data
                // 作成後にステータスを更新
                await getCompletionStatus()
                toast.success('プロフィールが作成されました！')
                return true
            } else {
                error.value = response.data.message
                handleValidationError(new Error(response.data.message))
                return false
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'プロフィール作成に失敗しました'
            handleValidationError(err)
            return false
        } finally {
            loading.value = false
        }
    }

    // 自分のプロフィール取得
    const getMyProfile = async (): Promise<void> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.getMyProfile()
            
            if (response.data.success) {
                profile.value = response.data.data || null
                // プロフィール取得後にステータスも更新
                await getCompletionStatus()
            } else {
                error.value = response.data.message
                handleError(new Error(response.data.message), { showToast: false })
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'プロフィール取得に失敗しました'
            profile.value = null
            handleServerError(err)
        } finally {
            loading.value = false
        }
    }

    // プロフィール更新
    const updateProfile = async (data: UpdateUserProfileRequest): Promise<boolean> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.updateProfile(data)
            
            if (response.data.success && response.data.data) {
                profile.value = response.data.data
                // 更新後にステータスを更新
                await getCompletionStatus()
                toast.success('プロフィールが更新されました！')
                return true
            } else {
                error.value = response.data.message
                handleValidationError(new Error(response.data.message))
                return false
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'プロフィール更新に失敗しました'
            handleValidationError(err)
            return false
        } finally {
            loading.value = false
        }
    }

    // プロフィール削除
    const deleteProfile = async (): Promise<boolean> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.deleteProfile()
            
            if (response.data.success) {
                profile.value = null
                toast.success('プロフィールが削除されました')
                return true
            } else {
                error.value = response.data.message
                handleValidationError(new Error(response.data.message))
                return false
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'プロフィール削除に失敗しました'
            handleValidationError(err)
            return false
        } finally {
            loading.value = false
        }
    }

    // プロフィール検索
    const searchProfiles = async (searchParams: UserProfileSearchRequest): Promise<UserProfile[]> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.searchProfiles(searchParams)
            
            if (response.data.success && response.data.data) {
                if (response.data.data.length === 0) {
                    toast.info('検索条件に一致するユーザーが見つかりませんでした')
                } else {
                    toast.success(`${response.data.data.length}件のプロフィールが見つかりました`)
                }
                return response.data.data
            } else {
                error.value = response.data.message
                handleError(new Error(response.data.message))
                return []
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || '検索に失敗しました'
            handleServerError(err)
            return []
        } finally {
            loading.value = false
        }
    }

    // 年間パス保有者取得
    const getAnnualPassHolders = async (): Promise<UserProfile[]> => {
        try {
            loading.value = true
            error.value = null

            const response = await profileApi.getAnnualPassHolders()
            
            if (response.data.success && response.data.data) {
                toast.success(`${response.data.data.length}人の年間パス保有者が見つかりました`)
                return response.data.data
            } else {
                error.value = response.data.message
                handleError(new Error(response.data.message))
                return []
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || '取得に失敗しました'
            handleServerError(err)
            return []
        } finally {
            loading.value = false
        }
    }

    // プロフィールリセット
    const resetProfile = () => {
        profile.value = null
        error.value = null
    }

    return {
        profile,
        completionStatus,
        loading,
        error,
        isProfileComplete,
        hasProfile,
        missingFields,
        getCompletionStatus,
        createProfile,
        getMyProfile,
        updateProfile,
        deleteProfile,
        searchProfiles,
        getAnnualPassHolders,
        resetProfile
    }
}) 