import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
    MatchingRequestDto,
    MatchingApplicationDto,
    MatchingRoomDto,
    ChatRoomDto,
    CreateMatchingRequestRequest,
    CreateMatchingApplicationRequest,
    MatchingRequestSearchRequest,
    MatchingSummaryDto,
    MatchingRequestWithActions
} from '@/types'
import {
    matchingRequestApi,
    matchingApplicationApi,
    matchingRoomApi,
    chatRoomApi,
    matchingApi
} from '@/services'
import { MatchingRequestStatus, MatchingApplicationStatus, MatchingRoomStatus } from '@/types'

export const useMatchingStore = defineStore('matching', () => {
    // State
    const isLoading = ref(false)
    const error = ref<string | null>(null)
    
    // 募集関連
    const activeRequests = ref<MatchingRequestDto[]>([])
    const myRequests = ref<MatchingRequestDto[]>([])
    const currentRequest = ref<MatchingRequestWithActions | null>(null)
    
    // 応募関連
    const myApplications = ref<MatchingApplicationDto[]>([])
    
    // 待機室関連
    const myRooms = ref<MatchingRoomDto[]>([])
    const currentRoom = ref<MatchingRoomDto | null>(null)
    
    // チャット関連
    const myChatRooms = ref<ChatRoomDto[]>([])
    
    // 検索条件
    const searchParams = ref<MatchingRequestSearchRequest>({})
    
    // 概要情報
    const summary = ref<MatchingSummaryDto>({
        totalRequests: 0,
        myRequests: 0,
        myApplications: 0,
        activeRooms: 0,
        activeChatRooms: 0
    })

    // Getters
    const openRequests = computed(() => 
        activeRequests.value.filter(req => req.status === MatchingRequestStatus.OPEN)
    )
    
    const myOpenRequests = computed(() => 
        myRequests.value.filter(req => req.status === MatchingRequestStatus.OPEN)
    )
    
    const pendingApplications = computed(() => 
        myApplications.value.filter(app => app.status === MatchingApplicationStatus.PENDING)
    )
    
    const acceptedApplications = computed(() => 
        myApplications.value.filter(app => app.status === MatchingApplicationStatus.ACCEPTED)
    )
    
    const activeRoomsComputed = computed(() => 
        myRooms.value.filter(room => room.status === MatchingRoomStatus.WAITING)
    )

    // エラーハンドリング共通処理
    const handleError = (err: any, defaultMessage: string) => {
        if (err.response?.data?.message) {
            error.value = err.response.data.message
        } else if (err.response?.data?.errors && Array.isArray(err.response.data.errors)) {
            error.value = err.response.data.errors.join(', ')
        } else if (err.response?.data?.error) {
            error.value = err.response.data.error
        } else if (err.message) {
            error.value = err.message
        } else {
            error.value = defaultMessage
        }
    }

    // 募集関連アクション
    const fetchActiveRequests = async (params?: MatchingRequestSearchRequest) => {
        try {
            isLoading.value = true
            error.value = null
            searchParams.value = params || {}
            
            const response = await matchingRequestApi.getActiveMatchingRequests(params)
            if (response.data.success) {
                activeRequests.value = response.data.data || []
            }
        } catch (err) {
            handleError(err, '募集一覧の取得に失敗しました')
        } finally {
            isLoading.value = false
        }
    }

    const createRequest = async (data: CreateMatchingRequestRequest) => {
        try {
            isLoading.value = true
            error.value = null
            
            const response = await matchingApi.createMatchingWithRoom(data)
            
            // 作成した募集を各配列に追加
            myRequests.value.unshift(response.request)
            activeRequests.value.unshift(response.request)
            myRooms.value.unshift(response.room)
            
            return response.request
        } catch (err) {
            handleError(err, '募集の作成に失敗しました')
            throw err
        } finally {
            isLoading.value = false
        }
    }

    const fetchMyRequests = async () => {
        try {
            const response = await matchingRequestApi.getMyMatchingRequests()
            if (response.data.success) {
                myRequests.value = response.data.data || []
            }
        } catch (err) {
            handleError(err, '自分の募集一覧の取得に失敗しました')
        }
    }

    // 応募関連アクション
    const applyToRequest = async (requestId: number, data: CreateMatchingApplicationRequest) => {
        try {
            isLoading.value = true
            error.value = null
            
            const result = await matchingApi.applyAndJoinRoom(requestId, data)
            
            // 応募一覧に追加
            myApplications.value.unshift(result.application)
            
            return result
        } catch (err) {
            handleError(err, '応募に失敗しました')
            throw err
        } finally {
            isLoading.value = false
        }
    }

    const fetchMyApplications = async () => {
        try {
            const response = await matchingApplicationApi.getMyApplications()
            if (response.data.success) {
                myApplications.value = response.data.data || []
            }
        } catch (err) {
            handleError(err, '自分の応募一覧の取得に失敗しました')
        }
    }

    // 状態リセット
    const resetState = () => {
        activeRequests.value = []
        myRequests.value = []
        currentRequest.value = null
        myApplications.value = []
        myRooms.value = []
        currentRoom.value = null
        myChatRooms.value = []
        searchParams.value = {}
        error.value = null
    }

    return {
        // State
        isLoading,
        error,
        activeRequests,
        myRequests,
        currentRequest,
        myApplications,
        myRooms,
        currentRoom,
        myChatRooms,
        searchParams,
        summary,
        
        // Getters
        openRequests,
        myOpenRequests,
        pendingApplications,
        acceptedApplications,
        activeRooms: activeRoomsComputed,
        
        // Actions
        fetchActiveRequests,
        fetchMyRequests,
        createRequest,
        applyToRequest,
        fetchMyApplications,
        resetState
    }
}) 