package com.usjchatapp.application.dto

import com.fasterxml.jackson.annotation.JsonProperty

// 待機室参加メッセージ
data class JoinRoomMessage(
        @JsonProperty("userId") val userId: Long,
        @JsonProperty("userDisplayName") val userDisplayName: String,
        @JsonProperty("requestId") val requestId: Long
)

// 待機室退出メッセージ
data class LeaveRoomMessage(
        @JsonProperty("userId") val userId: Long,
        @JsonProperty("userDisplayName") val userDisplayName: String,
        @JsonProperty("requestId") val requestId: Long
)

// 待機室確定メッセージ
data class ConfirmRoomMessage(
        @JsonProperty("requestId") val requestId: Long,
        @JsonProperty("confirmedBy") val confirmedBy: Long
)

// 待機室解散メッセージ
data class DisbandRoomMessage(
        @JsonProperty("requestId") val requestId: Long,
        @JsonProperty("disbandedBy") val disbandedBy: Long
)

// 参加者情報
data class ParticipantInfo(
        @JsonProperty("userId") val userId: Long,
        @JsonProperty("displayName") val displayName: String,
        @JsonProperty("isHost") val isHost: Boolean,
        @JsonProperty("joinedAt") val joinedAt: String
)

// 待機室状態更新メッセージ
data class RoomStateUpdateMessage(
        @JsonProperty("requestId") val requestId: Long,
        @JsonProperty("participants") val participants: List<ParticipantInfo>,
        @JsonProperty("currentCount") val currentCount: Int,
        @JsonProperty("maxParticipants") val maxParticipants: Int,
        @JsonProperty("isConfirmed") val isConfirmed: Boolean
)
