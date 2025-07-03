import axios from 'axios'
import type {
    ApiResponse,
    MatchingRoomDto
} from '@/types'
import { MatchingRoomStatus } from '@/types'
import { MATCHING_ROOM_PATHS } from '@/constants/apiPaths'

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

export const matchingRoomApi = {
    // 待機室取得
    getMatchingRoom: (requestId: number) =>
        api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId)),

    // 待機室作成
    createMatchingRoom: (requestId: number) =>
        api.post<ApiResponse<MatchingRoomDto>>(MATCHING_ROOM_PATHS.CREATE(requestId)),

    // 待機室参加
    joinMatchingRoom: (requestId: number) =>
        api.post<ApiResponse<MatchingRoomDto>>(MATCHING_ROOM_PATHS.JOIN(requestId)),

    // 待機室退出
    leaveMatchingRoom: (requestId: number) =>
        api.post<ApiResponse<MatchingRoomDto>>(MATCHING_ROOM_PATHS.LEAVE(requestId)),

    // 待機室確定（ホストが確定ボタンを押す）- モンストの「クエスト開始」に相当
    confirmMatchingRoom: (requestId: number) =>
        api.post<ApiResponse<MatchingRoomDto>>(MATCHING_ROOM_PATHS.CONFIRM(requestId)),

    // 待機室解散
    disbandMatchingRoom: (requestId: number) =>
        api.post<ApiResponse<MatchingRoomDto>>(MATCHING_ROOM_PATHS.DISBAND(requestId)),

    // ユーザーの待機室一覧
    getMyMatchingRooms: () =>
        api.get<ApiResponse<MatchingRoomDto[]>>(MATCHING_ROOM_PATHS.MY),

    // アクティブな待機室一覧
    getActiveMatchingRooms: () =>
        api.get<ApiResponse<MatchingRoomDto[]>>(MATCHING_ROOM_PATHS.ACTIVE),

    // 待機室のステータスをリアルタイムで監視（ポーリング用）
    pollMatchingRoomStatus: (requestId: number) =>
        api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId)),

    // 待機室の参加者リストをリアルタイムで取得
    getParticipants: (requestId: number) => {
        return api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: response.data.data.participants
                        }
                    }
                }
                return response
            })
    },

    // 参加者数チェック
    getParticipantCount: (requestId: number) => {
        return api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: response.data.data.participants.length
                        }
                    }
                }
                return response
            })
    },

    // ホストかどうかチェック
    isHost: (requestId: number, userId: number) => {
        return api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    const isHost = response.data.data.participants.some(
                        p => p.userId === userId && p.isHost
                    )
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: isHost
                        }
                    }
                }
                return response
            })
    },

    // 待機室が確定可能かチェック（最小人数など）
    canConfirm: (requestId: number) => {
        return api.get<ApiResponse<MatchingRoomDto | null>>(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId))
            .then(response => {
                if (response.data.success && response.data.data) {
                    const room = response.data.data
                    const canConfirm = room.status === MatchingRoomStatus.WAITING && 
                                     room.participants.length >= 2
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: canConfirm
                        }
                    }
                }
                return response
            })
    }
}

export default matchingRoomApi 