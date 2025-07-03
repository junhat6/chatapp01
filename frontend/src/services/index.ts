// 既存のAPIサービス
export { default as api } from './api'
export { profileApi } from './profileApi'

// マッチング機能APIサービス
export { matchingRequestApi } from './matchingRequestApi'
export { matchingApplicationApi } from './matchingApplicationApi'
export { matchingRoomApi } from './matchingRoomApi'
export { chatRoomApi } from './chatRoomApi'

// 統合マッチングAPIサービス
export { matchingApi } from './matchingApi'

// WebSocket通信サービス（STOMP）
export { default as stompWebSocketService } from './stompWebSocketService'

// 統合APIサービスオブジェクト
import { matchingRequestApi } from './matchingRequestApi'
import { matchingApplicationApi } from './matchingApplicationApi'
import { matchingRoomApi } from './matchingRoomApi'
import { chatRoomApi } from './chatRoomApi'
import { matchingApi } from './matchingApi'
import { profileApi } from './profileApi'

export const apiServices = {
    matching: {
        requests: matchingRequestApi,
        applications: matchingApplicationApi,
        rooms: matchingRoomApi,
        chat: chatRoomApi,
        integrated: matchingApi
    },
    profile: profileApi
}

// デフォルトエクスポート
export default apiServices 