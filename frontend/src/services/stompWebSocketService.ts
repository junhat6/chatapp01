import { Client, Message, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import type { 
  JoinRoomMessage,
  LeaveRoomMessage,
  ConfirmRoomMessage,
  DisbandRoomMessage,
  RoomStateUpdateMessage,
  ParticipantInfo,
  ChatMessageDto
} from '@/types'

interface WebSocketEventCallbacks {
  onJoinRoom?: (message: JoinRoomMessage) => void
  onLeaveRoom?: (message: LeaveRoomMessage) => void
  onConfirmRoom?: (message: ConfirmRoomMessage) => void
  onDisbandRoom?: (message: DisbandRoomMessage) => void
  onRoomStateUpdate?: (message: RoomStateUpdateMessage) => void
  onError?: (error: string) => void
  onConnected?: () => void
  onDisconnected?: () => void
  // チャット機能追加
  onNewMessage?: (message: ChatMessageDto) => void
  onUserJoinedChat?: (data: { chatRoomId: number, participant: any }) => void
  onUserLeftChat?: (data: { chatRoomId: number, userId: number }) => void
  onTypingStart?: (data: { chatRoomId: number, userId: number, displayName: string }) => void
  onTypingStop?: (data: { chatRoomId: number, userId: number }) => void
}

class StompWebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, StompSubscription> = new Map()
  private eventCallbacks: WebSocketEventCallbacks = {}
  private reconnectAttempts = 0
  private maxReconnectAttempts = 10
  private isConnecting = false

  constructor() {
    this.initializeClient()
  }

  private initializeClient(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(import.meta.env.VITE_WEBSOCKET_URL || 'http://localhost:8080/ws'),
      debug: (str: string) => {
        if (import.meta.env.DEV) {
          console.log('🔌 STOMP Debug:', str)
        }
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('🔌 STOMP WebSocket connected')
        this.reconnectAttempts = 0
        this.isConnecting = false
        this.eventCallbacks.onConnected?.()
      },
      onDisconnect: () => {
        console.log('🔌 STOMP WebSocket disconnected')
        this.isConnecting = false
        this.eventCallbacks.onDisconnected?.()
      },
      onStompError: (frame: any) => {
        console.error('🔌 STOMP Error:', frame)
        this.eventCallbacks.onError?.(frame.headers?.message || 'STOMP接続エラー')
      }
    })
  }

  // 接続
  async connect(): Promise<boolean> {
    return new Promise((resolve, reject) => {
      if (this.isConnecting) {
        reject(new Error('既に接続中です'))
        return
      }

      if (this.client?.connected) {
        resolve(true)
        return
      }

      this.isConnecting = true

      try {
        this.client?.activate()
        
        // 接続タイムアウト
        const timeout = setTimeout(() => {
          this.isConnecting = false
          reject(new Error('接続タイムアウト'))
        }, 10000)

        // 接続成功時の処理
        if (this.client) {
          this.client.onConnect = () => {
            clearTimeout(timeout)
            this.reconnectAttempts = 0
            this.isConnecting = false
            console.log('🔌 STOMP WebSocket connected')
            this.eventCallbacks.onConnected?.()
            resolve(true)
          }
        }

        // エラー時の処理
        const originalOnStompError = this.client?.onStompError
        if (this.client) {
          this.client.onStompError = (frame: any) => {
            clearTimeout(timeout)
            this.isConnecting = false
            console.error('🔌 STOMP Error:', frame)
            this.eventCallbacks.onError?.(frame.headers?.message || 'STOMP接続エラー')
            reject(new Error(frame.headers?.message || 'STOMP接続エラー'))
            // 元のコールバックも呼び出し
            originalOnStompError?.(frame)
          }
        }

      } catch (error) {
        this.isConnecting = false
        console.error('🔌 STOMP connection error:', error)
        reject(error)
      }
    })
  }

  // 切断
  disconnect(): void {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe()
    })
    this.subscriptions.clear()
    
    this.client?.deactivate()
    console.log('🔌 STOMP WebSocket disconnected')
  }

  // 待機室にサブスクライブ
  subscribeToRoom(requestId: number): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const topic = `/topic/rooms/${requestId}`
    
    if (this.subscriptions.has(topic)) {
      console.log('既にサブスクライブ済み:', topic)
      return
    }

    const subscription = this.client.subscribe(topic, (message: Message) => {
      try {
        const data = JSON.parse(message.body)
        this.handleRoomMessage(data)
      } catch (error) {
        console.error('メッセージ解析エラー:', error)
      }
    })

    this.subscriptions.set(topic, subscription)
    console.log('待機室にサブスクライブ:', topic)
  }

  // 待機室のサブスクリプション解除
  unsubscribeFromRoom(requestId: number): void {
    const topic = `/topic/rooms/${requestId}`
    const subscription = this.subscriptions.get(topic)
    
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(topic)
      console.log('待機室のサブスクリプション解除:', topic)
    }
  }

  // 待機室参加
  joinRoom(requestId: number, userId: number, userDisplayName: string): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message: JoinRoomMessage = {
      userId,
      userDisplayName,
      requestId
    }

    this.client.publish({
      destination: `/app/rooms/${requestId}/join`,
      body: JSON.stringify(message)
    })

    console.log('待機室参加リクエスト送信:', message)
  }

  // 待機室退出
  leaveRoom(requestId: number, userId: number, userDisplayName: string): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message: LeaveRoomMessage = {
      userId,
      userDisplayName,
      requestId
    }

    this.client.publish({
      destination: `/app/rooms/${requestId}/leave`,
      body: JSON.stringify(message)
    })

    console.log('待機室退出リクエスト送信:', message)
  }

  // 待機室確定
  confirmRoom(requestId: number, confirmedBy: number): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message: ConfirmRoomMessage = {
      requestId,
      confirmedBy
    }

    this.client.publish({
      destination: `/app/rooms/${requestId}/confirm`,
      body: JSON.stringify(message)
    })

    console.log('待機室確定リクエスト送信:', message)
  }

  // 待機室解散
  disbandRoom(requestId: number, disbandedBy: number): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message: DisbandRoomMessage = {
      requestId,
      disbandedBy
    }

    this.client.publish({
      destination: `/app/rooms/${requestId}/disband`,
      body: JSON.stringify(message)
    })

    console.log('待機室解散リクエスト送信:', message)
  }

  // === チャット機能 ===

  // チャットルームにサブスクライブ
  subscribeToChatRoom(chatRoomId: number): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const topic = `/topic/chat/${chatRoomId}`
    
    if (this.subscriptions.has(topic)) {
      console.log('既にサブスクライブ済み:', topic)
      return
    }

    const subscription = this.client.subscribe(topic, (message: Message) => {
      try {
        const data = JSON.parse(message.body)
        this.handleChatMessage(data)
      } catch (error) {
        console.error('チャットメッセージ解析エラー:', error)
      }
    })

    this.subscriptions.set(topic, subscription)
    console.log('チャットルームにサブスクライブ:', topic)
  }

  // チャットルームのサブスクリプション解除
  unsubscribeFromChatRoom(chatRoomId: number): void {
    const topic = `/topic/chat/${chatRoomId}`
    const subscription = this.subscriptions.get(topic)
    
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(topic)
      console.log('チャットルームのサブスクリプション解除:', topic)
    }
  }

  // チャットルーム参加
  joinChatRoom(chatRoomId: number, userId: number, displayName: string): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message = {
      chatRoomId,
      userId,
      displayName
    }

    this.client.publish({
      destination: `/app/chat/${chatRoomId}/join`,
      body: JSON.stringify(message)
    })

    console.log('チャットルーム参加リクエスト送信:', message)
  }

  // チャットルーム退出
  leaveChatRoom(chatRoomId: number, userId: number, displayName: string): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message = {
      chatRoomId,
      userId,
      displayName
    }

    this.client.publish({
      destination: `/app/chat/${chatRoomId}/leave`,
      body: JSON.stringify(message)
    })

    console.log('チャットルーム退出リクエスト送信:', message)
  }

  // メッセージ送信
  sendMessage(chatRoomId: number, content: string, messageType: string = 'TEXT'): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message = {
      chatRoomId,
      content,
      messageType
    }

    this.client.publish({
      destination: `/app/chat/${chatRoomId}/message`,
      body: JSON.stringify(message)
    })

    console.log('メッセージ送信:', message)
  }

  // タイピング開始通知
  startTyping(chatRoomId: number, userId: number, displayName: string): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message = {
      chatRoomId,
      userId,
      displayName
    }

    this.client.publish({
      destination: `/app/chat/${chatRoomId}/typing/start`,
      body: JSON.stringify(message)
    })
  }

  // タイピング終了通知
  stopTyping(chatRoomId: number, userId: number): void {
    if (!this.client?.connected) {
      console.error('WebSocketが接続されていません')
      return
    }

    const message = {
      chatRoomId,
      userId
    }

    this.client.publish({
      destination: `/app/chat/${chatRoomId}/typing/stop`,
      body: JSON.stringify(message)
    })
  }

  // メッセージハンドラー
  private handleRoomMessage(data: any): void {
    console.log('待機室メッセージ受信:', data)

    // メッセージタイプを判定
    if (data.userId && data.userDisplayName && data.requestId) {
      if (data.confirmedBy !== undefined) {
        this.eventCallbacks.onConfirmRoom?.(data as ConfirmRoomMessage)
      } else if (data.disbandedBy !== undefined) {
        this.eventCallbacks.onDisbandRoom?.(data as DisbandRoomMessage)
      } else if (data.participants !== undefined) {
        this.eventCallbacks.onRoomStateUpdate?.(data as RoomStateUpdateMessage)
      } else {
        // 参加・退出メッセージ
        if (data.type === 'JOIN') {
          this.eventCallbacks.onJoinRoom?.(data as JoinRoomMessage)
        } else if (data.type === 'LEAVE') {
          this.eventCallbacks.onLeaveRoom?.(data as LeaveRoomMessage)
        }
      }
    }
  }

  // チャットメッセージハンドラー
  private handleChatMessage(data: any): void {
    console.log('チャットメッセージ受信:', data)

    // メッセージタイプを判定
    if (data.messageType === 'TEXT' || data.messageType === 'SYSTEM') {
      this.eventCallbacks.onNewMessage?.(data as ChatMessageDto)
    } else if (data.type === 'USER_JOINED') {
      this.eventCallbacks.onUserJoinedChat?.(data)
    } else if (data.type === 'USER_LEFT') {
      this.eventCallbacks.onUserLeftChat?.(data)
    } else if (data.type === 'TYPING_START') {
      this.eventCallbacks.onTypingStart?.(data)
    } else if (data.type === 'TYPING_STOP') {
      this.eventCallbacks.onTypingStop?.(data)
    }
  }

  // イベントコールバック設定
  on(event: keyof WebSocketEventCallbacks, callback: Function): void {
    this.eventCallbacks[event] = callback as any
  }

  // 接続状態確認
  isConnected(): boolean {
    return this.client?.connected || false
  }

  // 接続統計情報
  getConnectionStats(): object {
    return {
      connected: this.isConnected(),
      subscriptions: this.subscriptions.size,
      reconnectAttempts: this.reconnectAttempts
    }
  }
}

// シングルトンインスタンス
export const stompWebSocketService = new StompWebSocketService()
export default stompWebSocketService

// 型定義をエクスポート
export type {
  JoinRoomMessage,
  LeaveRoomMessage,
  ConfirmRoomMessage,
  DisbandRoomMessage,
  RoomStateUpdateMessage,
  ParticipantInfo
} 