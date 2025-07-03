import axios from 'axios'
import type {
    ApiResponse,
    MatchingRequestDto,
    CreateMatchingRequestRequest,
    UpdateMatchingRequestRequest,
    MatchingRequestSearchRequest,
    MatchingRequestStatus
} from '@/types'
import { MATCHING_REQUEST_PATHS } from '@/constants/apiPaths'

const api = axios.create({
    baseURL: '', // baseURLを空にしてフルパスを使用
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// リクエストインターセプター（JWTトークンを自動追加）
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// レスポンスインターセプター（エラーハンドリング）
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            // トークンが無効な場合はログイン画面にリダイレクト
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export const matchingRequestApi = {
    // 募集作成
    createMatchingRequest: (data: CreateMatchingRequestRequest) =>
        api.post<ApiResponse<MatchingRequestDto>>(MATCHING_REQUEST_PATHS.CREATE, data),

    // アクティブな募集一覧取得
    getActiveMatchingRequests: (params?: MatchingRequestSearchRequest) =>
        api.get<ApiResponse<MatchingRequestDto[]>>(MATCHING_REQUEST_PATHS.LIST, { params }),

    // 募集詳細取得
    getMatchingRequest: (id: number) =>
        api.get<ApiResponse<MatchingRequestDto>>(MATCHING_REQUEST_PATHS.BY_ID(id)),

    // 自分の募集一覧取得
    getMyMatchingRequests: () =>
        api.get<ApiResponse<MatchingRequestDto[]>>(MATCHING_REQUEST_PATHS.MY),

    // 募集更新
    updateMatchingRequest: (id: number, data: UpdateMatchingRequestRequest) =>
        api.put<ApiResponse<MatchingRequestDto>>(MATCHING_REQUEST_PATHS.UPDATE(id), data),

    // 募集キャンセル
    cancelMatchingRequest: (id: number) =>
        api.delete<ApiResponse<MatchingRequestDto>>(MATCHING_REQUEST_PATHS.DELETE(id)),

    // ステータス更新（内部利用）
    updateStatus: (id: number, status: MatchingRequestStatus) =>
        api.put<ApiResponse<MatchingRequestDto>>(MATCHING_REQUEST_PATHS.UPDATE_STATUS(id), null, {
            params: { status }
        }),

    // 期限切れの募集をチェック（管理用）
    checkExpiredRequests: () =>
        api.post<ApiResponse<number>>('/expire-old'),

    // 特定アトラクションの募集検索
    searchByAttraction: (attraction: string) =>
        api.get<ApiResponse<MatchingRequestDto[]>>(MATCHING_REQUEST_PATHS.LIST, {
            params: { attraction }
        }),

    // 応募可能な募集のみ取得
    getAvailableRequests: (params?: MatchingRequestSearchRequest) =>
        api.get<ApiResponse<MatchingRequestDto[]>>(MATCHING_REQUEST_PATHS.LIST, {
            params: { ...params, status: 'OPEN,WAITING' }
        }),

    // 日時範囲での募集検索
    searchByDateRange: (attraction: string, dateFrom: string, dateTo: string) =>
        api.get<ApiResponse<MatchingRequestDto[]>>(MATCHING_REQUEST_PATHS.LIST, {
            params: { attraction, dateFrom, dateTo }
        })
}

export default matchingRequestApi 