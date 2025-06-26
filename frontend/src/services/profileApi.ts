import axios from 'axios'
import type { 
    UserProfile, 
    CreateUserProfileRequest, 
    UpdateUserProfileRequest, 
    UserProfileSearchRequest,
    ApiResponse,
    ProfileCompletionStatus
} from '@/types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
    baseURL: `${API_BASE_URL}/api/profiles`,
    headers: {
        'Content-Type': 'application/json',
    },
})

// リクエストインターセプターでトークンを追加
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

export const profileApi = {
    // プロフィール作成
    createProfile: (data: CreateUserProfileRequest) => 
        api.post<ApiResponse<UserProfile>>('/', data),

    // 自分のプロフィール取得
    getMyProfile: () => 
        api.get<ApiResponse<UserProfile | null>>('/me'),

    // プロフィール完了ステータス取得
    getCompletionStatus: () => 
        api.get<ApiResponse<ProfileCompletionStatus>>('/completion-status'),

    // 他人のプロフィール取得
    getUserProfile: (userId: number) => 
        api.get<ApiResponse<UserProfile | null>>(`/${userId}`),

    // プロフィール更新
    updateProfile: (data: UpdateUserProfileRequest) => 
        api.put<ApiResponse<UserProfile>>('/', data),

    // プロフィール削除
    deleteProfile: () => 
        api.delete<ApiResponse<boolean>>('/'),

    // プロフィール検索
    searchProfiles: (params: UserProfileSearchRequest) => 
        api.get<ApiResponse<UserProfile[]>>('/search', { params }),

    // 年間パス保有者一覧
    getAnnualPassHolders: () => 
        api.get<ApiResponse<UserProfile[]>>('/annual-pass-holders'),

    // 特定アトラクション好きな人一覧
    getUsersByFavoriteAttraction: (attraction: string) => 
        api.get<ApiResponse<UserProfile[]>>(`/attraction/${attraction}`),
} 