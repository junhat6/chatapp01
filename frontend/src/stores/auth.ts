import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/services/api'
import { useErrorHandler } from '@/composables/useErrorHandler'
import { useToast } from '@/composables/useToast'
import type { SignInRequest, SignUpRequest, User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
    const token = ref<string | null>(localStorage.getItem('token'))
    const user = ref<User | null>(null)
    const loading = ref(false)
    const error = ref<string | null>(null)

    const isAuthenticated = computed(() => !!token.value)
    
    const { handleError, handleAuthError, handleValidationError } = useErrorHandler()
    const toast = useToast()

    const login = async (credentials: SignInRequest) => {
        try {
            loading.value = true
            error.value = null

            const response = await authApi.signIn(credentials)

            if (response.data.success) {
                token.value = response.data.data!.token
                user.value = response.data.data!.user
                localStorage.setItem('token', response.data.data!.token)
                
                toast.success(`おかえりなさい、${response.data.data!.user.email}さん！`)
                return true
            } else {
                error.value = response.data.message
                handleValidationError(new Error(response.data.message), 'ログイン情報を確認してください')
                return false
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || 'ログインに失敗しました'
            handleAuthError(err)
            return false
        } finally {
            loading.value = false
        }
    }

    const register = async (userData: SignUpRequest) => {
        try {
            loading.value = true
            error.value = null

            const response = await authApi.signUp(userData)

            if (response.data.success) {
                token.value = response.data.data!.token
                user.value = response.data.data!.user
                localStorage.setItem('token', response.data.data!.token)
                
                toast.success(`ようこそ、${response.data.data!.user.email}さん！`)
                return true
            } else {
                error.value = response.data.message
                handleValidationError(new Error(response.data.message), '登録情報を確認してください')
                return false
            }
        } catch (err: any) {
            error.value = err.response?.data?.message || '登録に失敗しました'
            handleValidationError(err)
            return false
        } finally {
            loading.value = false
        }
    }

    const logout = () => {
        token.value = null
        user.value = null
        localStorage.removeItem('token')
        toast.info('ログアウトしました')
    }

    const getCurrentUser = async () => {
        try {
            const response = await authApi.getCurrentUser()
            if (response.data.success) {
                user.value = response.data.data!
            }
        } catch (err: any) {
            // 認証エラーの場合は静かにログアウト（トーストは表示しない）
            handleAuthError(err)
            logout()
        }
    }

    // アプリ起動時にトークンがあればユーザー情報を取得
    if (token.value) {
        getCurrentUser()
    }

    return {
        token,
        user,
        loading,
        error,
        isAuthenticated,
        login,
        register,
        logout,
        getCurrentUser
    }
}) 