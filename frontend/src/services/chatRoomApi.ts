import axios from 'axios'
import type {
    ApiResponse,
    ChatRoomDto,
    ChatMessageDto,
    SendMessageRequest
} from '@/types'
import { ChatMessageType } from '@/types'
import { CHAT_ROOM_PATHS } from '@/constants/apiPaths'

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

export const chatRoomApi = {
    // チャットルーム取得
    getChatRoom: (chatRoomId: number) =>
        api.get<ApiResponse<ChatRoomDto>>(CHAT_ROOM_PATHS.BY_ID(chatRoomId)),

    // マッチングリクエストIDからチャットルーム取得
    getChatRoomByRequestId: (requestId: number) =>
        api.get<ApiResponse<ChatRoomDto>>(CHAT_ROOM_PATHS.FROM_REQUEST(requestId)),

    // マイチャットルーム一覧取得
    getMyChatRooms: () =>
        api.get<ApiResponse<ChatRoomDto[]>>(CHAT_ROOM_PATHS.MY),

    // メッセージ送信
    sendMessage: (chatRoomId: number, data: SendMessageRequest) =>
        api.post<ApiResponse<ChatMessageDto>>(CHAT_ROOM_PATHS.SEND_MESSAGE(chatRoomId), data),

    // メッセージ一覧取得（ページング対応）
    getMessages: (chatRoomId: number, page: number = 0, size: number = 50) =>
        api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES(chatRoomId), {
            params: { page, size }
        }),

    // 特定の時刻以降のメッセージを取得（リアルタイム更新用）
    getMessagesSince: (chatRoomId: number, since: string, limit: number = 50) =>
        api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES_SINCE(chatRoomId), {
            params: { since, limit }
        }),

    // システムメッセージ作成（入室・退室通知など）
    createSystemMessage: (chatRoomId: number, content: string) =>
        api.post<ApiResponse<ChatMessageDto>>(CHAT_ROOM_PATHS.SEND_SYSTEM_MESSAGE(chatRoomId), {
            content,
            messageType: ChatMessageType.SYSTEM
        }),

    // テキストメッセージ送信（簡便関数）
    sendTextMessage: (chatRoomId: number, content: string) =>
        api.post<ApiResponse<ChatMessageDto>>(CHAT_ROOM_PATHS.SEND_MESSAGE(chatRoomId), {
            content,
            messageType: ChatMessageType.TEXT
        }),

    // 最新メッセージ取得
    getLatestMessage: (chatRoomId: number) => {
        return api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES(chatRoomId), {
            params: { page: 0, size: 1 }
        }).then(response => {
            if (response.data.success && response.data.data && response.data.data.length > 0) {
                return {
                    ...response,
                    data: {
                        ...response.data,
                        data: response.data.data[0]
                    }
                }
            }
            return response
        })
    },

    // 未読メッセージ数取得（ローカルストレージの最終読み取り時刻と比較）
    getUnreadCount: (chatRoomId: number) => {
        const lastReadKey = `lastRead_${chatRoomId}`
        const lastReadTime = localStorage.getItem(lastReadKey)
        
        if (!lastReadTime) {
            // 初回アクセスの場合は全件数を返す
            return api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES(chatRoomId), {
                params: { page: 0, size: 1000 }
            }).then(response => {
                if (response.data.success && response.data.data) {
                    return {
                        ...response,
                        data: {
                            ...response.data,
                            data: response.data.data.length
                        }
                    }
                }
                return response
            })
        }

        return api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES_SINCE(chatRoomId), {
            params: { since: lastReadTime, limit: 1000 }
        }).then(response => {
            if (response.data.success && response.data.data) {
                return {
                    ...response,
                    data: {
                        ...response.data,
                        data: response.data.data.length
                    }
                }
            }
            return response
        })
    },

    // 既読マーク更新
    markAsRead: (chatRoomId: number) => {
        const lastReadKey = `lastRead_${chatRoomId}`
        const now = new Date().toISOString()
        localStorage.setItem(lastReadKey, now)
        return Promise.resolve(true)
    },

    // チャットルーム参加者一覧
    getParticipants: (chatRoomId: number) => {
        return api.get<ApiResponse<ChatRoomDto>>(CHAT_ROOM_PATHS.BY_ID(chatRoomId))
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

    // メッセージ履歴の詳細検索
    searchMessages: (chatRoomId: number, keyword: string, page: number = 0, size: number = 20) => {
        return api.get<ApiResponse<ChatMessageDto[]>>(CHAT_ROOM_PATHS.GET_MESSAGES(chatRoomId), {
            params: { page, size }
        }).then(response => {
            if (response.data.success && response.data.data) {
                const filteredMessages = response.data.data.filter(message =>
                    message.content.toLowerCase().includes(keyword.toLowerCase())
                )
                return {
                    ...response,
                    data: {
                        ...response.data,
                        data: filteredMessages
                    }
                }
            }
            return response
        })
    }
}

export default chatRoomApi 