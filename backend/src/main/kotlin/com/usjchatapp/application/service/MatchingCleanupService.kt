package com.usjchatapp.application.service

import com.usjchatapp.domain.entity.MatchingRequestStatus
import com.usjchatapp.domain.repository.MatchingRequestRepository
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MatchingCleanupService(private val matchingRequestRepository: MatchingRequestRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /** 期限切れ募集の論理削除バッチ処理 毎時0分に実行され、期限切れから24時間経過した募集を論理削除する */
    @Scheduled(cron = "0 0 * * * *") // 毎時0分に実行
    fun softDeleteExpiredRequests() {
        logger.info("期限切れ募集の論理削除バッチ処理を開始します")

        try {
            val now = LocalDateTime.now()
            val expiredThreshold = now.minusHours(24) // 24時間前

            // 期限切れから24時間経過した募集を検索
            val expiredStatuses =
                    listOf(
                            MatchingRequestStatus.EXPIRED,
                            MatchingRequestStatus.OPEN,
                            MatchingRequestStatus.WAITING,
                            MatchingRequestStatus.CLOSED
                    )

            val requestsToDelete =
                    matchingRequestRepository.findExpiredRequestsForDeletion(
                            expiredThreshold = expiredThreshold,
                            expiredStatuses = expiredStatuses
                    )

            if (requestsToDelete.isEmpty()) {
                logger.info("論理削除対象の募集はありません")
                return
            }

            // 論理削除実行
            val deletedCount =
                    matchingRequestRepository.softDeleteByIds(
                            ids = requestsToDelete.map { it.id },
                            deletedAt = now,
                            updatedAt = now
                    )

            logger.info("期限切れ募集の論理削除が完了しました。削除件数: $deletedCount 件")

            // 削除した募集の詳細をログ出力（デバッグ用）
            requestsToDelete.forEach { request ->
                logger.debug(
                        "論理削除実行: ID=${request.id}, " +
                                "アトラクション=${request.attraction}, " +
                                "希望時間=${request.preferredDateTime}, " +
                                "ステータス=${request.status}"
                )
            }
        } catch (e: Exception) {
            logger.error("期限切れ募集の論理削除処理中にエラーが発生しました", e)
            throw e
        }
    }

    /** 手動実行用の論理削除メソッド 管理者API等から呼び出し可能 */
    fun manualSoftDeleteExpiredRequests(): Int {
        logger.info("手動による期限切れ募集の論理削除を実行します")

        val now = LocalDateTime.now()
        val expiredThreshold = now.minusHours(24)

        val expiredStatuses =
                listOf(
                        MatchingRequestStatus.EXPIRED,
                        MatchingRequestStatus.OPEN,
                        MatchingRequestStatus.WAITING,
                        MatchingRequestStatus.CLOSED
                )

        val requestsToDelete =
                matchingRequestRepository.findExpiredRequestsForDeletion(
                        expiredThreshold = expiredThreshold,
                        expiredStatuses = expiredStatuses
                )

        return if (requestsToDelete.isNotEmpty()) {
            val deletedCount =
                    matchingRequestRepository.softDeleteByIds(
                            ids = requestsToDelete.map { it.id },
                            deletedAt = now,
                            updatedAt = now
                    )

            logger.info("手動実行による論理削除が完了しました。削除件数: $deletedCount 件")
            deletedCount
        } else {
            logger.info("手動実行: 論理削除対象の募集はありません")
            0
        }
    }

    /** 統計情報取得: 論理削除された募集数をカウント */
    fun getDeletedRequestsCount(): Long {
        return matchingRequestRepository.count() -
                matchingRequestRepository.findByStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
                                MatchingRequestStatus.values().toList()
                        )
                        .size
    }
}
