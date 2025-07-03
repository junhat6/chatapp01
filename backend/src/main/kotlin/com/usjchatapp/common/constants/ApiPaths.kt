package com.usjchatapp.common.constants

/** APIパス定数を一元管理するクラス フロントエンドとの整合性を保つため、パスの変更時はこのクラスを更新する */
object ApiPaths {

    // ベースパス
    const val API_BASE = "/api"

    // 認証関連
    object Auth {
        const val BASE = "$API_BASE/auth"
        const val SIGNUP = "/signup"
        const val SIGNIN = "/signin"
        const val ME = "/me"
    }

    // ユーザープロフィール関連
    object Profile {
        const val BASE = "$API_BASE/profiles"
        const val CREATE_OR_UPDATE = ""
        const val ME = "/me"
        const val COMPLETION_STATUS = "/completion-status"
        const val BY_ID = "/{userId}"
        const val UPDATE = ""
        const val DELETE = ""
        const val SEARCH = "/search"
        const val ANNUAL_PASS_HOLDERS = "/annual-pass-holders"
        const val BY_ATTRACTION = "/attraction/{attraction}"
        const val ATTRACTIONS = "/attractions"
    }

    // マッチング募集関連
    object MatchingRequest {
        const val BASE = "$API_BASE/matching/requests"
        const val CREATE = ""
        const val LIST = ""
        const val BY_ID = "/{id}"
        const val MY = "/my"
        const val UPDATE = "/{id}"
        const val DELETE = "/{id}"
        const val UPDATE_STATUS = "/{id}/status"
    }

    // マッチング応募関連
    object MatchingApplication {
        const val BASE = "$API_BASE/matching/applications"
        const val APPLY = "/requests/{requestId}"
        const val CANCEL = "/requests/{requestId}"
        const val LIST_BY_REQUEST = "/requests/{requestId}"
        const val MY = "/my"
        const val UPDATE_STATUS = "/{applicationId}/status"
        const val ACCEPTED = "/requests/{requestId}/accepted"
        const val CAN_APPLY = "/requests/{requestId}/can-apply"
    }

    // 待機室関連
    object MatchingRoom {
        const val BASE = "$API_BASE/matching/rooms"
        const val GET_BY_REQUEST = "/requests/{requestId}"
        const val CREATE = "/requests/{requestId}"
        const val JOIN = "/requests/{requestId}/join"
        const val LEAVE = "/requests/{requestId}/leave"
        const val CONFIRM = "/requests/{requestId}/confirm"
        const val DISBAND = "/requests/{requestId}/disband"
        const val MY = "/my"
        const val ACTIVE = "/active"
    }

    // チャットルーム関連
    object ChatRoom {
        const val BASE = "$API_BASE/chat/rooms"
        const val BY_ID = "/{chatRoomId}"
        const val MY = "/my"
        const val SEND_MESSAGE = "/{chatRoomId}/messages"
        const val GET_MESSAGES = "/{chatRoomId}/messages"
        const val GET_MESSAGES_SINCE = "/{chatRoomId}/messages/since"
        const val SEND_SYSTEM_MESSAGE = "/{chatRoomId}/system-messages"
        const val SEARCH = "/search"
        const val DELETE = "/{chatRoomId}"
        const val FROM_REQUEST = "/from-request/{requestId}"
    }

    // ヘルス関連
    object Health {
        const val BASE = "$API_BASE/health"
        const val CHECK = ""
    }

    // WebSocket関連
    object WebSocket {
        const val ENDPOINT = "/ws"
        const val APP_PREFIX = "/app"
        const val TOPIC_PREFIX = "/topic"

        object Rooms {
            const val JOIN = "/rooms/{id}/join"
            const val LEAVE = "/rooms/{id}/leave"
            const val CONFIRM = "/rooms/{id}/confirm"
            const val DISBAND = "/rooms/{id}/disband"
            const val TOPIC = "/rooms/{id}"
        }
    }
}
