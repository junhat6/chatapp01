import axios from 'axios'
import type {
    ApiResponse,
    MatchingApplicationDto,
    CreateMatchingApplicationRequest,
    UpdateApplicationStatusRequest
} from '@/types'
import { MatchingApplicationStatus } from '@/types'
import { MATCHING_APPLICATION_PATHS } from '@/constants/apiPaths'

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
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

export const matchingApplicationApi = {
    // 募集に応募
    applyToMatching: (requestId: number, data: CreateMatchingApplicationRequest) =>
        api.post<ApiResponse<MatchingApplicationDto>>(MATCHING_APPLICATION_PATHS.APPLY(requestId), data),

    // 応募取り消し
    withdrawApplication: (requestId: number) =>
        api.delete<ApiResponse<boolean>>(MATCHING_APPLICATION_PATHS.CANCEL(requestId)),

    // 募集への応募一覧取得（ホスト用）
    getApplicationsForRequest: (requestId: number) =>
        api.get<ApiResponse<MatchingApplicationDto[]>>(MATCHING_APPLICATION_PATHS.LIST_BY_REQUEST(requestId)),

    // 自分の応募一覧取得
    getMyApplications: () =>
        api.get<ApiResponse<MatchingApplicationDto[]>>(MATCHING_APPLICATION_PATHS.MY),

    // 応募ステータス更新（ホストが承認/拒否）
    updateApplicationStatus: (applicationId: number, data: UpdateApplicationStatusRequest) =>
        api.put<ApiResponse<MatchingApplicationDto>>(MATCHING_APPLICATION_PATHS.UPDATE_STATUS(applicationId), data),

    // 承認済み参加者取得
    getAcceptedParticipants: (requestId: number) =>
        api.get<ApiResponse<MatchingApplicationDto[]>>(MATCHING_APPLICATION_PATHS.ACCEPTED(requestId)),

    // 応募可能かチェック
    canApply: (requestId: number) =>
        api.get<ApiResponse<boolean>>(MATCHING_APPLICATION_PATHS.CAN_APPLY(requestId)),

    // 特定ステータスの応募をフィルタリング
    getApplicationsByStatus: (requestId: number, status: MatchingApplicationStatus) => {
        return api.get<ApiResponse<MatchingApplicationDto[]>>(MATCHING_APPLICATION_PATHS.LIST_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    const filteredApplications = response.data.data.filter(
                        app => app.status === status
                    )
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: filteredApplications
                        }
                    }
                }
                return response
            })
    },

    // 応募数カウント
    getApplicationCount: (requestId: number, status?: MatchingApplicationStatus) => {
        return api.get<ApiResponse<MatchingApplicationDto[]>>(MATCHING_APPLICATION_PATHS.LIST_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    const count = status 
                        ? response.data.data.filter(app => app.status === status).length
                        : response.data.data.length
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: count
                        }
                    }
                }
                return response
            })
    },

    // 一括承認（ホスト用）
    approveMultipleApplications: async (applicationIds: number[]) => {
        const promises = applicationIds.map(id => 
            api.put<ApiResponse<MatchingApplicationDto>>(MATCHING_APPLICATION_PATHS.UPDATE_STATUS(id), {
                status: MatchingApplicationStatus.ACCEPTED
            })
        )
        return Promise.all(promises)
    },

    // 一括拒否（ホスト用）
    rejectMultipleApplications: async (applicationIds: number[]) => {
        const promises = applicationIds.map(id => 
            api.put<ApiResponse<MatchingApplicationDto>>(MATCHING_APPLICATION_PATHS.UPDATE_STATUS(id), {
                status: MatchingApplicationStatus.REJECTED
            })
        )
        return Promise.all(promises)
    }
}

export default matchingApplicationApi 