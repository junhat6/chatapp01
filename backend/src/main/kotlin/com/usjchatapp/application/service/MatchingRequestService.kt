package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.*
import com.usjchatapp.domain.repository.*
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MatchingRequestService(
        private val matchingRequestRepository: MatchingRequestRepository,
        private val matchingApplicationRepository: MatchingApplicationRepository,
        private val userProfileRepository: UserProfileRepository
) {

    // 募集作成
    fun createMatchingRequest(
            hostUserId: Long,
            request: CreateMatchingRequestRequest
    ): MatchingRequestDto {
        // ホストのプロフィール確認
        if (!userProfileRepository.existsByUserId(hostUserId)) {
            throw IllegalArgumentException("プロフィールを完成させてから募集を作成してください")
        }

        val matchingRequest =
                MatchingRequest(
                        hostUserId = hostUserId,
                        attraction = request.attraction,
                        preferredDateTime = request.preferredDateTime,
                        maxParticipants = request.maxParticipants,
                        description = request.description
                )

        val saved = matchingRequestRepository.save(matchingRequest)
        return convertToDto(saved)
    }

    // 募集一覧取得（アクティブな募集のみ）
    fun getActiveMatchingRequests(
            searchRequest: MatchingRequestSearchRequest
    ): List<MatchingRequestDto> {
        val activeStatuses = listOf(MatchingRequestStatus.OPEN, MatchingRequestStatus.WAITING)

        val requests =
                when {
                    searchRequest.attraction != null -> {
                        matchingRequestRepository.findByAttractionAndStatusInOrderByCreatedAtDesc(
                                searchRequest.attraction,
                                activeStatuses
                        )
                    }
                    searchRequest.dateFrom != null &&
                            searchRequest.dateTo != null &&
                            searchRequest.attraction != null -> {
                        matchingRequestRepository.findByAttractionAndDateTimeRange(
                                searchRequest.attraction,
                                searchRequest.dateFrom,
                                searchRequest.dateTo,
                                activeStatuses
                        )
                    }
                    else -> {
                        matchingRequestRepository.findByStatusInOrderByCreatedAtDesc(activeStatuses)
                    }
                }

        return requests.map { convertToDto(it) }
    }

    // 募集詳細取得
    fun getMatchingRequest(id: Long): MatchingRequestDto {
        val request =
                matchingRequestRepository.findById(id).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }
        return convertToDto(request)
    }

    // ユーザーの募集一覧取得
    fun getUserMatchingRequests(userId: Long): List<MatchingRequestDto> {
        return matchingRequestRepository.findByHostUserIdOrderByCreatedAtDesc(userId).map {
            convertToDto(it)
        }
    }

    // 募集更新
    fun updateMatchingRequest(
            id: Long,
            hostUserId: Long,
            request: UpdateMatchingRequestRequest
    ): MatchingRequestDto {
        val existing =
                matchingRequestRepository.findById(id).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        if (existing.hostUserId != hostUserId) {
            throw IllegalArgumentException("自分の募集のみ編集できます")
        }

        if (existing.status !in listOf(MatchingRequestStatus.OPEN, MatchingRequestStatus.WAITING)) {
            throw IllegalArgumentException("確定済みまたは終了した募集は編集できません")
        }

        val updated =
                existing.copy(
                        description = request.description ?: existing.description,
                        preferredDateTime = request.preferredDateTime ?: existing.preferredDateTime,
                        maxParticipants = request.maxParticipants ?: existing.maxParticipants,
                        updatedAt = LocalDateTime.now()
                )

        val saved = matchingRequestRepository.save(updated)
        return convertToDto(saved)
    }

    // 募集キャンセル
    fun cancelMatchingRequest(id: Long, hostUserId: Long): MatchingRequestDto {
        val existing =
                matchingRequestRepository.findById(id).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        if (existing.hostUserId != hostUserId) {
            throw IllegalArgumentException("自分の募集のみキャンセルできます")
        }

        if (existing.status == MatchingRequestStatus.CONFIRMED) {
            throw IllegalArgumentException("確定済みの募集はキャンセルできません")
        }

        val cancelled =
                existing.copy(
                        status = MatchingRequestStatus.CLOSED,
                        updatedAt = LocalDateTime.now()
                )

        val saved = matchingRequestRepository.save(cancelled)
        return convertToDto(saved)
    }

    // 募集のステータス更新
    fun updateStatus(id: Long, status: MatchingRequestStatus): MatchingRequestDto {
        val existing =
                matchingRequestRepository.findById(id).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        val updated = existing.copy(status = status, updatedAt = LocalDateTime.now())

        val saved = matchingRequestRepository.save(updated)
        return convertToDto(saved)
    }

    // 期限切れの募集を自動的にクローズ
    fun expireOldRequests(): Int {
        val now = LocalDateTime.now()
        val activeStatuses = listOf(MatchingRequestStatus.OPEN, MatchingRequestStatus.WAITING)

        val expiredRequests =
                matchingRequestRepository.findByPreferredDateTimeBeforeAndStatusIn(
                        now,
                        activeStatuses
                )

        val updatedRequests =
                expiredRequests.map { request ->
                    request.copy(status = MatchingRequestStatus.EXPIRED, updatedAt = now)
                }

        matchingRequestRepository.saveAll(updatedRequests)
        return updatedRequests.size
    }

    // DTOへの変換
    private fun convertToDto(request: MatchingRequest): MatchingRequestDto {
        val hostProfile = userProfileRepository.findById(request.hostUserId).orElse(null)
        val currentApplications =
                matchingApplicationRepository.countByMatchingRequestIdAndStatus(
                        request.id,
                        MatchingApplicationStatus.PENDING
                )

        return MatchingRequestDto(
                id = request.id,
                hostUserId = request.hostUserId,
                hostDisplayName = hostProfile?.displayName ?: "不明なユーザー",
                attraction = request.attraction,
                preferredDateTime = request.preferredDateTime,
                maxParticipants = request.maxParticipants,
                description = request.description,
                status = request.status,
                currentApplications = currentApplications,
                createdAt = request.createdAt,
                updatedAt = request.updatedAt
        )
    }
}
