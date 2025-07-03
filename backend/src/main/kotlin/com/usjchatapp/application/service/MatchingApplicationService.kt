package com.usjchatapp.application.service

import com.usjchatapp.application.dto.*
import com.usjchatapp.domain.entity.*
import com.usjchatapp.domain.repository.*
import java.time.LocalDateTime
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MatchingApplicationService(
        private val matchingApplicationRepository: MatchingApplicationRepository,
        private val matchingRequestRepository: MatchingRequestRepository,
        private val userProfileRepository: UserProfileRepository,
        private val matchingRequestService: MatchingRequestService,
        private val matchingRoomService: MatchingRoomService
) {

    // 応募作成
    fun applyToMatching(
            matchingRequestId: Long,
            applicantUserId: Long,
            request: CreateMatchingApplicationRequest
    ): MatchingApplicationDto {
        // 応募者のプロフィール確認
        if (!userProfileRepository.existsByUserId(applicantUserId)) {
            throw IllegalArgumentException("プロフィールを完成させてから応募してください")
        }

        // 募集の存在確認
        val matchingRequest =
                matchingRequestRepository.findById(matchingRequestId).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        // ビジネスルールチェック
        validateApplication(matchingRequest, applicantUserId)

        val application =
                MatchingApplication(
                        matchingRequestId = matchingRequestId,
                        applicantUserId = applicantUserId,
                        message = request.message
                )

        val saved = matchingApplicationRepository.save(application)

        // 募集のステータスを更新（応募者がいる場合はWAITINGに）
        if (matchingRequest.status == MatchingRequestStatus.OPEN) {
            matchingRequestService.updateStatus(matchingRequestId, MatchingRequestStatus.WAITING)
        }

        // 応募時に自動的に待機室に参加
        try {
            matchingRoomService.joinMatchingRoom(matchingRequestId, applicantUserId)
        } catch (e: Exception) {
            // 待機室参加に失敗しても応募は成功とする
            // ログ出力のみ行う
            println("Warning: Failed to join matching room for user $applicantUserId: ${e.message}")
        }

        return convertToDto(saved)
    }

    // 応募取り消し
    fun withdrawApplication(matchingRequestId: Long, applicantUserId: Long): Boolean {
        val application =
                matchingApplicationRepository.findByMatchingRequestIdAndApplicantUserId(
                        matchingRequestId,
                        applicantUserId
                )
                        ?: throw IllegalArgumentException("応募が見つかりません")

        if (application.status != MatchingApplicationStatus.PENDING) {
            throw IllegalArgumentException("既に処理済みの応募は取り消しできません")
        }

        matchingApplicationRepository.delete(application)

        // 応募取り消し時に待機室から退出
        try {
            matchingRoomService.leaveMatchingRoom(matchingRequestId, applicantUserId)
        } catch (e: Exception) {
            // 待機室退出に失敗しても応募取り消しは成功とする
            // ログ出力のみ行う
            println(
                    "Warning: Failed to leave matching room for user $applicantUserId: ${e.message}"
            )
        }

        // 残り応募数をチェックして募集ステータス更新
        val remainingApplications =
                matchingApplicationRepository.countByMatchingRequestIdAndStatus(
                        matchingRequestId,
                        MatchingApplicationStatus.PENDING
                )

        if (remainingApplications == 0) {
            matchingRequestService.updateStatus(matchingRequestId, MatchingRequestStatus.OPEN)
        }

        return true
    }

    // 募集への応募一覧取得
    fun getApplicationsForRequest(matchingRequestId: Long): List<MatchingApplicationDto> {
        return matchingApplicationRepository.findByMatchingRequestIdOrderByAppliedAtAsc(
                        matchingRequestId
                )
                .map { convertToDto(it) }
    }

    // ユーザーの応募一覧取得
    fun getUserApplications(userId: Long): List<MatchingApplicationDto> {
        return matchingApplicationRepository.findByApplicantUserIdOrderByAppliedAtDesc(userId).map {
            convertToDto(it)
        }
    }

    // 応募ステータス更新（ホストが承認/拒否）
    fun updateApplicationStatus(
            applicationId: Long,
            hostUserId: Long,
            request: UpdateApplicationStatusRequest
    ): MatchingApplicationDto {
        val application =
                matchingApplicationRepository.findById(applicationId).orElseThrow {
                    IllegalArgumentException("応募が見つかりません")
                }

        val matchingRequest =
                matchingRequestRepository.findById(application.matchingRequestId).orElseThrow {
                    IllegalArgumentException("募集が見つかりません")
                }

        if (matchingRequest.hostUserId != hostUserId) {
            throw IllegalArgumentException("自分の募集の応募のみ操作できます")
        }

        if (application.status != MatchingApplicationStatus.PENDING) {
            throw IllegalArgumentException("既に処理済みの応募です")
        }

        val updated = application.copy(status = request.status, updatedAt = LocalDateTime.now())

        val saved = matchingApplicationRepository.save(updated)
        return convertToDto(saved)
    }

    // 応募の承認済み参加者を取得
    fun getAcceptedParticipants(matchingRequestId: Long): List<MatchingApplicationDto> {
        return matchingApplicationRepository.findByMatchingRequestIdAndStatusOrderByAppliedAtAsc(
                        matchingRequestId,
                        MatchingApplicationStatus.ACCEPTED
                )
                .map { convertToDto(it) }
    }

    // 応募可能かチェック
    fun canApply(matchingRequestId: Long, applicantUserId: Long): Boolean {
        return try {
            val matchingRequest =
                    matchingRequestRepository.findById(matchingRequestId).orElseThrow {
                        IllegalArgumentException("募集が見つかりません")
                    }

            validateApplication(matchingRequest, applicantUserId)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    // バリデーション
    private fun validateApplication(matchingRequest: MatchingRequest, applicantUserId: Long) {
        // 自分の募集には応募できない
        if (matchingRequest.hostUserId == applicantUserId) {
            throw IllegalArgumentException("自分の募集には応募できません")
        }

        // 募集がアクティブでない場合は応募不可
        if (matchingRequest.status !in
                        listOf(MatchingRequestStatus.OPEN, MatchingRequestStatus.WAITING)
        ) {
            throw IllegalArgumentException("この募集は現在応募を受け付けていません")
        }

        // 期限切れチェック
        if (matchingRequest.preferredDateTime.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("期限切れの募集には応募できません")
        }

        // 重複応募チェック
        if (matchingApplicationRepository.existsByMatchingRequestIdAndApplicantUserId(
                        matchingRequest.id,
                        applicantUserId
                )
        ) {
            throw IllegalArgumentException("既に応募済みです")
        }

        // 定員チェック
        val currentApplications =
                matchingApplicationRepository.countByMatchingRequestIdAndStatus(
                        matchingRequest.id,
                        MatchingApplicationStatus.PENDING
                )
        val acceptedApplications =
                matchingApplicationRepository.countByMatchingRequestIdAndStatus(
                        matchingRequest.id,
                        MatchingApplicationStatus.ACCEPTED
                )

        if (currentApplications + acceptedApplications >= matchingRequest.maxParticipants - 1
        ) { // -1はホスト分
            throw IllegalArgumentException("定員に達しています")
        }
    }

    // DTOへの変換
    private fun convertToDto(application: MatchingApplication): MatchingApplicationDto {
        val applicantProfile =
                userProfileRepository.findById(application.applicantUserId).orElse(null)

        return MatchingApplicationDto(
                id = application.id,
                matchingRequestId = application.matchingRequestId,
                applicantUserId = application.applicantUserId,
                applicantDisplayName = applicantProfile?.displayName ?: "不明なユーザー",
                message = application.message,
                status = application.status,
                appliedAt = application.appliedAt,
                updatedAt = application.updatedAt
        )
    }
}
