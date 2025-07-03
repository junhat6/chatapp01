package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.*
import com.usjchatapp.domain.repository.*
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MatchingRoomService(
        private val matchingRoomRepository: MatchingRoomRepository,
        private val matchingRequestRepository: MatchingRequestRepository,
        private val matchingApplicationRepository: MatchingApplicationRepository,
        private val userProfileRepository: UserProfileRepository,
        private val matchingRequestService: MatchingRequestService
) {

    /** 待機室作成（初回応募時に自動作成） */
    fun createMatchingRoom(matchingRequestId: Long): MatchingRoomDto {
        // 既存の待機室があるかチェック
        val existingRoom = matchingRoomRepository.findByMatchingRequestId(matchingRequestId)
        if (existingRoom != null) {
            return convertToDto(existingRoom)
        }

        val matchingRequest = getMatchingRequestEntity(matchingRequestId)

        // 待機室作成（初期はホストのみ）
        val matchingRoom =
                MatchingRoom(
                        matchingRequestId = matchingRequestId,
                        participantUserIds = arrayOf(matchingRequest.hostUserId),
                        status = MatchingRoomStatus.WAITING
                )

        val saved = matchingRoomRepository.save(matchingRoom)
        return convertToDto(saved)
    }

    /** 待機室参加（応募承認時） */
    fun joinMatchingRoom(matchingRequestId: Long, participantUserId: Long): MatchingRoomDto {
        val room = getRoomByRequestId(matchingRequestId)

        // 既に参加している場合はそのまま返す
        if (participantUserId in room.participantUserIds) {
            return convertToDto(room)
        }

        validateRoomJoin(room, participantUserId)

        val updatedRoom =
                room.copy(
                        participantUserIds = room.participantUserIds.plus(participantUserId),
                        updatedAt = LocalDateTime.now()
                )

        val saved = matchingRoomRepository.save(updatedRoom)
        return convertToDto(saved)
    }

    /** 待機室退出 */
    fun leaveMatchingRoom(matchingRequestId: Long, participantUserId: Long): MatchingRoomDto {
        val room = getRoomByRequestId(matchingRequestId)
        val matchingRequest = getMatchingRequestEntity(matchingRequestId)

        validateRoomLeave(room, matchingRequest, participantUserId)

        val updatedRoom =
                room.copy(
                        participantUserIds =
                                room.participantUserIds
                                        .filter { it != participantUserId }
                                        .toTypedArray(),
                        updatedAt = LocalDateTime.now()
                )

        val saved = matchingRoomRepository.save(updatedRoom)

        // 対応する応募を削除
        handleApplicationOnLeave(matchingRequestId, participantUserId)

        return convertToDto(saved)
    }

    /** 待機室確定（ホストが確定ボタンを押した時） */
    fun confirmMatchingRoom(matchingRequestId: Long, hostUserId: Long): MatchingRoomDto {
        val room = getRoomByRequestId(matchingRequestId)
        val matchingRequest = getMatchingRequestEntity(matchingRequestId)

        validateRoomConfirm(room, matchingRequest, hostUserId)

        val confirmedRoom =
                room.copy(status = MatchingRoomStatus.CONFIRMED, updatedAt = LocalDateTime.now())

        val saved = matchingRoomRepository.save(confirmedRoom)

        // 募集のステータスを確定に変更
        matchingRequestService.updateStatus(matchingRequestId, MatchingRequestStatus.CONFIRMED)

        return convertToDto(saved)
    }

    /** 待機室解散 */
    fun disbandMatchingRoom(matchingRequestId: Long, hostUserId: Long): MatchingRoomDto {
        val room = getRoomByRequestId(matchingRequestId)
        val matchingRequest = getMatchingRequestEntity(matchingRequestId)

        validateRoomDisband(matchingRequest, hostUserId)

        val disbandedRoom =
                room.copy(status = MatchingRoomStatus.DISBANDED, updatedAt = LocalDateTime.now())

        val saved = matchingRoomRepository.save(disbandedRoom)

        // 募集をクローズ
        matchingRequestService.updateStatus(matchingRequestId, MatchingRequestStatus.CLOSED)

        return convertToDto(saved)
    }

    /** 待機室取得 */
    fun getMatchingRoom(matchingRequestId: Long): MatchingRoomDto? {
        val room = matchingRoomRepository.findByMatchingRequestId(matchingRequestId) ?: return null
        return convertToDto(room)
    }

    /** ユーザーが参加している待機室一覧 */
    fun getUserMatchingRooms(userId: Long): List<MatchingRoomDto> {
        return matchingRoomRepository.findByParticipantUserId(userId).map { convertToDto(it) }
    }

    /** アクティブな待機室一覧 */
    fun getActiveMatchingRooms(): List<MatchingRoomDto> {
        return matchingRoomRepository.findByStatusOrderByCreatedAtDesc(MatchingRoomStatus.WAITING)
                .map { convertToDto(it) }
    }

    /** 募集情報取得（WebSocketController用） */
    fun getMatchingRequest(matchingRequestId: Long): MatchingRequestDto? {
        val entity =
                matchingRequestRepository.findById(matchingRequestId).orElse(null) ?: return null

        // ホストの表示名を取得
        val hostProfile = userProfileRepository.findById(entity.hostUserId).orElse(null)
        val hostDisplayName = hostProfile?.displayName ?: "ユーザー${entity.hostUserId}"

        // 応募数を取得
        val applications =
                matchingApplicationRepository.findByMatchingRequestIdOrderByAppliedAtAsc(
                        matchingRequestId
                )

        return MatchingRequestDto(
                id = entity.id,
                hostUserId = entity.hostUserId,
                hostDisplayName = hostDisplayName,
                attraction = entity.attraction,
                preferredDateTime = entity.preferredDateTime,
                maxParticipants = entity.maxParticipants,
                description = entity.description,
                status = entity.status,
                currentApplications = applications.size,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
        )
    }

    // === Private Helper Methods ===

    private fun getMatchingRequestEntity(matchingRequestId: Long): MatchingRequest {
        return matchingRequestRepository.findById(matchingRequestId).orElseThrow {
            IllegalArgumentException("募集が見つかりません")
        }
    }

    private fun getRoomByRequestId(matchingRequestId: Long): MatchingRoom {
        return matchingRoomRepository.findByMatchingRequestId(matchingRequestId)
                ?: throw IllegalArgumentException("待機室が見つかりません")
    }

    private fun validateRoomJoin(room: MatchingRoom, participantUserId: Long) {
        if (room.status != MatchingRoomStatus.WAITING) {
            throw IllegalArgumentException("待機室は現在参加を受け付けていません")
        }

        // 募集情報を取得して最大参加者数をチェック
        val matchingRequest = getMatchingRequestEntity(room.matchingRequestId)
        if (room.participantUserIds.size >= matchingRequest.maxParticipants) {
            throw IllegalArgumentException("待機室の参加者数が上限に達しています")
        }

        // ホスト以外の参加者IDバリデーション
        if (participantUserId == matchingRequest.hostUserId) {
            throw IllegalArgumentException("ホストは追加で参加する必要がありません")
        }
    }

    private fun validateRoomLeave(
            room: MatchingRoom,
            matchingRequest: MatchingRequest,
            participantUserId: Long
    ) {
        if (participantUserId == matchingRequest.hostUserId) {
            throw IllegalArgumentException("ホストは待機室を退出できません")
        }

        if (participantUserId !in room.participantUserIds) {
            throw IllegalArgumentException("参加していない待機室です")
        }
    }

    private fun validateRoomConfirm(
            room: MatchingRoom,
            matchingRequest: MatchingRequest,
            hostUserId: Long
    ) {
        if (matchingRequest.hostUserId != hostUserId) {
            throw IllegalArgumentException("ホストのみ確定できます")
        }

        if (room.status != MatchingRoomStatus.WAITING) {
            throw IllegalArgumentException("待機中の状態ではありません")
        }

        if (room.participantUserIds.size < 2) {
            throw IllegalArgumentException("参加者が足りません（最低2人必要）")
        }
    }

    private fun validateRoomDisband(matchingRequest: MatchingRequest, hostUserId: Long) {
        if (matchingRequest.hostUserId != hostUserId) {
            throw IllegalArgumentException("ホストのみ解散できます")
        }
    }

    private fun handleApplicationOnLeave(matchingRequestId: Long, participantUserId: Long) {
        val application =
                matchingApplicationRepository.findByMatchingRequestIdAndApplicantUserId(
                        matchingRequestId,
                        participantUserId
                )
        if (application != null && application.status == MatchingApplicationStatus.ACCEPTED) {
            matchingApplicationRepository.delete(application)
        }
    }

    /** DTOへの変換 */
    private fun convertToDto(room: MatchingRoom): MatchingRoomDto {
        val participants =
                room.participantUserIds.mapNotNull { userId ->
                    userProfileRepository.findById(userId).orElse(null)?.let { profile ->
                        val matchingRequest =
                                matchingRequestRepository
                                        .findById(room.matchingRequestId)
                                        .orElse(null)
                        ParticipantDto(
                                userId = profile.userId,
                                displayName = profile.displayName,
                                profileImage = profile.profileImage,
                                isHost = userId == matchingRequest?.hostUserId
                        )
                    }
                }

        return MatchingRoomDto(
                id = room.id,
                matchingRequestId = room.matchingRequestId,
                participants = participants,
                status = room.status,
                createdAt = room.createdAt,
                updatedAt = room.updatedAt
        )
    }
}
