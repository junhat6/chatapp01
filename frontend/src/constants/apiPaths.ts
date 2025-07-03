/**
 * APIパス定数を一元管理するファイル
 * バックエンドのApiPaths.ktと同期を保つ必要がある
 */

// ベースパス（開発環境ではプロキシを使用するため相対パス）
export const API_BASE = '/api';

// 認証関連
export const AUTH_PATHS = {
  BASE: `${API_BASE}/auth`,
  SIGNUP: `${API_BASE}/auth/signup`,
  SIGNIN: `${API_BASE}/auth/signin`,
  ME: `${API_BASE}/auth/me`,
} as const;

// ユーザープロフィール関連
export const PROFILE_PATHS = {
  BASE: `${API_BASE}/profiles`,
  CREATE_OR_UPDATE: `${API_BASE}/profiles`,
  ME: `${API_BASE}/profiles/me`,
  COMPLETION_STATUS: `${API_BASE}/profiles/completion-status`,
  BY_ID: (userId: string) => `${API_BASE}/profiles/${userId}`,
  UPDATE: `${API_BASE}/profiles`,
  DELETE: `${API_BASE}/profiles`,
  SEARCH: `${API_BASE}/profiles/search`,
  ANNUAL_PASS_HOLDERS: `${API_BASE}/profiles/annual-pass-holders`,
  BY_ATTRACTION: (attraction: string) => `${API_BASE}/profiles/attraction/${attraction}`,
  ATTRACTIONS: `${API_BASE}/profiles/attractions`,
} as const;

// マッチング募集関連
export const MATCHING_REQUEST_PATHS = {
  BASE: `${API_BASE}/matching/requests`,
  CREATE: `${API_BASE}/matching/requests`,
  LIST: `${API_BASE}/matching/requests`,
  BY_ID: (id: string | number) => `${API_BASE}/matching/requests/${id}`,
  MY: `${API_BASE}/matching/requests/my`,
  UPDATE: (id: string | number) => `${API_BASE}/matching/requests/${id}`,
  DELETE: (id: string | number) => `${API_BASE}/matching/requests/${id}`,
  UPDATE_STATUS: (id: string | number) => `${API_BASE}/matching/requests/${id}/status`,
} as const;

// マッチング応募関連
export const MATCHING_APPLICATION_PATHS = {
  BASE: `${API_BASE}/matching/applications`,
  APPLY: (requestId: string | number) => `${API_BASE}/matching/applications/requests/${requestId}`,
  CANCEL: (requestId: string | number) => `${API_BASE}/matching/applications/requests/${requestId}`,
  LIST_BY_REQUEST: (requestId: string | number) => `${API_BASE}/matching/applications/requests/${requestId}`,
  MY: `${API_BASE}/matching/applications/my`,
  UPDATE_STATUS: (applicationId: string | number) => `${API_BASE}/matching/applications/${applicationId}/status`,
  ACCEPTED: (requestId: string | number) => `${API_BASE}/matching/applications/requests/${requestId}/accepted`,
  CAN_APPLY: (requestId: string | number) => `${API_BASE}/matching/applications/requests/${requestId}/can-apply`,
} as const;

// 待機室関連
export const MATCHING_ROOM_PATHS = {
  BASE: `${API_BASE}/matching/rooms`,
  GET_BY_REQUEST: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}`,
  CREATE: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}`,
  JOIN: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}/join`,
  LEAVE: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}/leave`,
  CONFIRM: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}/confirm`,
  DISBAND: (requestId: string | number) => `${API_BASE}/matching/rooms/requests/${requestId}/disband`,
  MY: `${API_BASE}/matching/rooms/my`,
  ACTIVE: `${API_BASE}/matching/rooms/active`,
} as const;

// チャットルーム関連
export const CHAT_ROOM_PATHS = {
  BASE: `${API_BASE}/chat/rooms`,
  BY_ID: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}`,
  MY: `${API_BASE}/chat/rooms/my`,
  SEND_MESSAGE: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}/messages`,
  GET_MESSAGES: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}/messages`,
  GET_MESSAGES_SINCE: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}/messages/since`,
  SEND_SYSTEM_MESSAGE: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}/system-messages`,
  SEARCH: `${API_BASE}/chat/rooms/search`,
  DELETE: (chatRoomId: string | number) => `${API_BASE}/chat/rooms/${chatRoomId}`,
  FROM_REQUEST: (requestId: string | number) => `${API_BASE}/chat/rooms/from-request/${requestId}`,
} as const;

// ヘルス関連
export const HEALTH_PATHS = {
  BASE: `${API_BASE}/health`,
  CHECK: `${API_BASE}/health`,
} as const;

// WebSocket関連
export const WEBSOCKET_PATHS = {
  ENDPOINT: '/ws',
  APP_PREFIX: '/app',
  TOPIC_PREFIX: '/topic',
  
  ROOMS: {
    JOIN: (id: string | number) => `/rooms/${id}/join`,
    LEAVE: (id: string | number) => `/rooms/${id}/leave`,
    CONFIRM: (id: string | number) => `/rooms/${id}/confirm`,
    DISBAND: (id: string | number) => `/rooms/${id}/disband`,
    TOPIC: (id: string | number) => `/rooms/${id}`,
  },
} as const;

// 完全なURLを生成するヘルパー関数
export const buildApiUrl = (basePath: string, endpoint: string = '') => {
  return `${basePath}${endpoint}`;
};

// WebSocket用のURL生成ヘルパー
export const buildWebSocketUrl = (action: string, id?: string | number) => {
  if (id !== undefined) {
    return action.replace('{id}', String(id));
  }
  return action;
}; 