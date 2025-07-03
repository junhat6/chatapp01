import { matchingRequestApi } from './matchingRequestApi'
import { matchingApplicationApi } from './matchingApplicationApi'
import { matchingRoomApi } from './matchingRoomApi'
import { chatRoomApi } from './chatRoomApi'
import type {
    MatchingRequestDto,
    MatchingApplicationDto,
    MatchingRoomDto,
    ChatRoomDto,
    MatchingRequestWithActions,
    CreateMatchingRequestRequest,
    CreateMatchingApplicationRequest,
    MatchingSummaryDto
} from '@/types'
import { MatchingRequestStatus, MatchingApplicationStatus, MatchingRoomStatus } from '@/types'

/**
 * 統合マッチングAPIサービス
 * 複数のAPIサービスを組み合わせた便利な機能を提供
 */
export const matchingApi = {
    /**
     * 募集詳細とユーザーのアクション可能状態を取得
     */
    async getMatchingRequestWithActions(requestId: number): Promise<MatchingRequestWithActions> {
        const [requestResponse, canApplyResponse] = await Promise.all([
            matchingRequestApi.getMatchingRequest(requestId),
            matchingApplicationApi.canApply(requestId)
        ])

        const request = requestResponse.data.data!
        const canApply = canApplyResponse.data.data!

        // ユーザーの応募状況を取得
        let userApplication: MatchingApplicationDto | undefined
        try {
            const myApplicationsResponse = await matchingApplicationApi.getMyApplications()
            userApplication = myApplicationsResponse.data.data?.find(
                app => app.matchingRequestId === requestId
            )
        } catch (error) {
            // 応募していない場合はundefined
        }

        // 待機室の状況を取得
        let matchingRoom: MatchingRoomDto | undefined
        try {
            const roomResponse = await matchingRoomApi.getMatchingRoom(requestId)
            matchingRoom = roomResponse.data.data || undefined
        } catch (error) {
            // 待機室がない場合はundefined
        }

        return {
            ...request,
            canApply,
            userApplication,
            matchingRoom
        }
    },

    /**
     * 募集作成から待機室作成まで一括実行
     */
    async createMatchingWithRoom(data: CreateMatchingRequestRequest): Promise<{
        request: MatchingRequestDto
        room: MatchingRoomDto
    }> {
        // 1. 募集作成
        const requestResponse = await matchingRequestApi.createMatchingRequest(data)
        const request = requestResponse.data.data!

        // 2. 待機室作成
        const roomResponse = await matchingRoomApi.createMatchingRoom(request.id)
        const room = roomResponse.data.data!

        return { request, room }
    },

    /**
     * 応募から待機室参加まで一括実行
     */
    async applyAndJoinRoom(requestId: number, applicationData: CreateMatchingApplicationRequest): Promise<{
        application: MatchingApplicationDto
        room: MatchingRoomDto
    }> {
        // 1. 応募（バックエンドで自動的に待機室に参加）
        const applicationResponse = await matchingApplicationApi.applyToMatching(requestId, applicationData)
        const application = applicationResponse.data.data!

        // 2. 待機室情報を取得
        const roomResponse = await matchingRoomApi.getMatchingRoom(requestId)
        const room = roomResponse.data.data!

        return { application, room }
    },

    /**
     * 待機室確定からチャットルーム作成まで一括実行（ホスト用）
     */
    async confirmAndStartChat(requestId: number): Promise<{
        room: MatchingRoomDto
        chatRoom: ChatRoomDto
    }> {
        // 1. 待機室確定
        const roomResponse = await matchingRoomApi.confirmMatchingRoom(requestId)
        const room = roomResponse.data.data!

        // 2. チャットルーム情報を取得（バックエンドで自動作成される）
        // 少し待ってからチャットルーム一覧を取得
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const chatRoomsResponse = await chatRoomApi.getMyChatRooms()
        const chatRoom = chatRoomsResponse.data.data?.find(
            cr => cr.matchingRequestId === requestId
        )

        if (!chatRoom) {
            throw new Error('チャットルームの作成に失敗しました')
        }

        return { room, chatRoom }
    },

    /**
     * ユーザーのマッチング概要情報を取得
     */
    async getMatchingSummary(): Promise<MatchingSummaryDto> {
        const [
            myRequestsResponse,
            myApplicationsResponse,
            myRoomsResponse,
            myChatRoomsResponse
        ] = await Promise.all([
            matchingRequestApi.getMyMatchingRequests(),
            matchingApplicationApi.getMyApplications(),
            matchingRoomApi.getMyMatchingRooms(),
            chatRoomApi.getMyChatRooms()
        ])

        const myRequests = myRequestsResponse.data.data || []
        const myApplications = myApplicationsResponse.data.data || []
        const myRooms = myRoomsResponse.data.data || []
        const myChatRooms = myChatRoomsResponse.data.data || []

        // アクティブな募集数を計算
        const totalRequests = myRequests.filter(
            req => req.status === MatchingRequestStatus.OPEN || req.status === MatchingRequestStatus.WAITING
        ).length

        // アクティブな待機室数を計算
        const activeRooms = myRooms.filter(
            room => room.status === MatchingRoomStatus.WAITING
        ).length

        return {
            totalRequests,
            myRequests: myRequests.length,
            myApplications: myApplications.length,
            activeRooms,
            activeChatRooms: myChatRooms.length
        }
    },

    /**
     * リアルタイム更新用データを取得
     */
    async pollUpdates(requestId: number): Promise<{
        request: MatchingRequestDto
        room: MatchingRoomDto | null
        applications: MatchingApplicationDto[]
    }> {
        const [requestResponse, roomResponse, applicationsResponse] = await Promise.all([
            matchingRequestApi.getMatchingRequest(requestId),
            matchingRoomApi.getMatchingRoom(requestId),
            matchingApplicationApi.getApplicationsForRequest(requestId)
        ])

        return {
            request: requestResponse.data.data!,
            room: roomResponse.data.data || null,
            applications: applicationsResponse.data.data || []
        }
    },

    /**
     * ユーザーダッシュボード用のデータを一括取得
     */
    async getDashboardData(): Promise<{
        myRequests: MatchingRequestDto[]
        myApplications: MatchingApplicationDto[]
        myRooms: MatchingRoomDto[]
        myChatRooms: ChatRoomDto[]
        activeRequests: MatchingRequestDto[]
    }> {
        const [
            myRequestsResponse,
            myApplicationsResponse,
            myRoomsResponse,
            myChatRoomsResponse,
            activeRequestsResponse
        ] = await Promise.all([
            matchingRequestApi.getMyMatchingRequests(),
            matchingApplicationApi.getMyApplications(),
            matchingRoomApi.getMyMatchingRooms(),
            chatRoomApi.getMyChatRooms(),
            matchingRequestApi.getActiveMatchingRequests()
        ])

        return {
            myRequests: myRequestsResponse.data.data || [],
            myApplications: myApplicationsResponse.data.data || [],
            myRooms: myRoomsResponse.data.data || [],
            myChatRooms: myChatRoomsResponse.data.data || [],
            activeRequests: activeRequestsResponse.data.data || []
        }
    },

    /**
     * 募集とその関連データをクリーンアップ
     */
    async cleanupMatchingRequest(requestId: number): Promise<void> {
        try {
            // 待機室解散
            await matchingRoomApi.disbandMatchingRoom(requestId)
        } catch (error) {
            // 待機室がない場合は無視
        }

        // 募集キャンセル
        await matchingRequestApi.cancelMatchingRequest(requestId)
    }
}

export default matchingApi 