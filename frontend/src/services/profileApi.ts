import api from './api'
import type { 
    UserProfile, 
    CreateUserProfileRequest, 
    UpdateUserProfileRequest, 
    UserProfileSearchRequest,
    ApiResponse,
    ProfileCompletionStatus
} from '@/types'
import { PROFILE_PATHS } from '@/constants/apiPaths'

export const profileApi = {
    // プロフィールを作成または更新
    createOrUpdateProfile: (data: CreateUserProfileRequest) =>
        api.post<ApiResponse<UserProfile>>(PROFILE_PATHS.CREATE_OR_UPDATE, data),

    // 自分のプロフィールを取得
    getMyProfile: () =>
        api.get<ApiResponse<UserProfile | null>>(PROFILE_PATHS.ME),

    // プロフィール完成度チェック
    checkProfileCompletion: () =>
        api.get<ApiResponse<ProfileCompletionStatus>>(PROFILE_PATHS.COMPLETION_STATUS),

    // ユーザーIDでプロフィールを取得
    getProfileByUserId: (userId: number) =>
        api.get<ApiResponse<UserProfile | null>>(PROFILE_PATHS.BY_ID(userId.toString())),

    // プロフィールを更新
    updateProfile: (data: UpdateUserProfileRequest) =>
        api.put<ApiResponse<UserProfile>>(PROFILE_PATHS.UPDATE, data),

    // プロフィールを削除
    deleteProfile: () =>
        api.delete<ApiResponse<boolean>>(PROFILE_PATHS.DELETE),

    // プロフィールを検索
    searchProfiles: (params: UserProfileSearchRequest) =>
        api.get<ApiResponse<UserProfile[]>>(PROFILE_PATHS.SEARCH, { params }),

    // 年パス保持者一覧を取得
    getAnnualPassHolders: () =>
        api.get<ApiResponse<UserProfile[]>>(PROFILE_PATHS.ANNUAL_PASS_HOLDERS),

    // アトラクション別プロフィール一覧を取得
    getProfilesByAttraction: (attraction: string) =>
        api.get<ApiResponse<UserProfile[]>>(PROFILE_PATHS.BY_ATTRACTION(attraction)),

    // 利用可能なアトラクション一覧を取得
    getAvailableAttractions: () =>
        api.get<ApiResponse<string[]>>(PROFILE_PATHS.ATTRACTIONS),
} 