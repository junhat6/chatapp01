// マッチング機能の型定義

// MatchingRequest関連
export interface MatchingRequestDto {
  id: number
  hostUserId: number
  hostDisplayName: string
  attraction: string
  preferredDateTime: string
  maxParticipants: number
  description?: string
  status: MatchingRequestStatus
  currentApplications: number
  createdAt: string
  updatedAt: string
}

export interface CreateMatchingRequestRequest {
  attraction: string
  preferredDateTime: string
  maxParticipants: number
  description?: string
}

export interface UpdateMatchingRequestRequest {
  description?: string
  preferredDateTime?: string
  maxParticipants?: number
}

export interface MatchingRequestSearchRequest {
  attraction?: string
  dateFrom?: string
  dateTo?: string
  maxParticipants?: number
  status?: MatchingRequestStatus
}

export enum MatchingRequestStatus {
  OPEN = 'OPEN',           // 募集中
  WAITING = 'WAITING',     // 待機中（参加者が集まっている状態）
  CONFIRMED = 'CONFIRMED', // 確定済み（チャットルーム作成済み）
  CLOSED = 'CLOSED',       // 終了
  EXPIRED = 'EXPIRED'      // 期限切れ
}

// MatchingApplication関連
export interface MatchingApplicationDto {
  id: number
  matchingRequestId: number
  applicantUserId: number
  applicantDisplayName: string
  message?: string
  status: MatchingApplicationStatus
  appliedAt: string
  updatedAt: string
}

export interface CreateMatchingApplicationRequest {
  message?: string
}

export interface UpdateApplicationStatusRequest {
  status: MatchingApplicationStatus
}

export enum MatchingApplicationStatus {
  PENDING = 'PENDING',   // 保留中
  ACCEPTED = 'ACCEPTED', // 承認済み
  REJECTED = 'REJECTED'  // 拒否済み
}

// MatchingRoom関連
export interface MatchingRoomDto {
  id: number
  matchingRequestId: number
  participants: ParticipantDto[]
  status: MatchingRoomStatus
  createdAt: string
  updatedAt: string
}

export interface ParticipantDto {
  userId: number
  displayName: string
  profileImage?: string
  isHost: boolean
}

export enum MatchingRoomStatus {
  WAITING = 'WAITING',     // 待機中
  CONFIRMED = 'CONFIRMED', // 確定済み
  DISBANDED = 'DISBANDED'  // 解散
}

// ChatRoom関連
export interface ChatRoomDto {
  id: number
  name: string
  matchingRequestId: number
  participants: ParticipantDto[]
  lastMessage?: ChatMessageDto
  createdAt: string
  updatedAt: string
}

export interface CreateChatRoomRequest {
  name: string
  matchingRequestId: number
  participantUserIds: number[]
}

// ChatMessage関連
export interface ChatMessageDto {
  id: number
  chatRoomId: number
  senderUserId: number
  senderDisplayName: string
  senderProfileImage?: string
  content: string
  messageType: ChatMessageType
  sentAt: string
}

export interface SendMessageRequest {
  content: string
  messageType?: ChatMessageType
}

export enum ChatMessageType {
  TEXT = 'TEXT',       // テキストメッセージ
  SYSTEM = 'SYSTEM',   // システムメッセージ（入室・退室など）
  IMAGE = 'IMAGE',     // 画像（将来的な拡張用）
  LOCATION = 'LOCATION' // 位置情報（将来的な拡張用）
}

// マッチング概要
export interface MatchingSummaryDto {
  totalRequests: number
  myRequests: number
  myApplications: number
  activeRooms: number
  activeChatRooms: number
}

// リアルタイム通信用
export interface MatchingRoomUpdateDto {
  type: string // "JOIN", "LEAVE", "STATUS_CHANGE"
  matchingRoomId: number
  participants: ParticipantDto[]
  status: MatchingRoomStatus
  message?: string
}

export interface ChatMessageUpdateDto {
  type: string // "NEW_MESSAGE", "USER_JOIN", "USER_LEAVE"
  chatRoomId: number
  message?: ChatMessageDto
  systemMessage?: string
}

// フロントエンド専用の便利な型
export interface MatchingRequestWithActions extends MatchingRequestDto {
  canApply: boolean
  userApplication?: MatchingApplicationDto
  matchingRoom?: MatchingRoomDto
}

export interface ChatRoomWithUnread extends ChatRoomDto {
  unreadCount: number
  isActive: boolean
} 