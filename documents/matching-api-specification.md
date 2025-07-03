# マッチングシステム API 仕様書

## 概要

このドキュメントは、マッチング募集機能の API 仕様を定義します。すべての API は RESTful 設計に従い、JSON 形式でデータをやり取りします。

## 基本情報

- **ベース URL**: `/api/matching`
- **認証方式**: JWT Bearer Token
- **Content-Type**: `application/json`
- **文字エンコーディング**: UTF-8

## 共通レスポンス形式

```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
}
```

## エラーレスポンス

```typescript
interface ErrorResponse {
  success: false;
  message: string;
  errors?: string[];
  timestamp: string;
  path: string;
}
```

## 1. 募集関連 API

### 1.1 募集作成

**POST** `/api/matching/requests`

募集を作成し、同時に待機室も作成します。

#### リクエスト

```typescript
interface CreateMatchingRequestRequest {
  attraction: string;
  preferredDateTime: string; // ISO 8601形式
  maxParticipants: number; // 2-8の範囲
  description?: string;
}
```

#### レスポンス

```typescript
interface CreateMatchingResponse {
  request: MatchingRequestDto;
  room: MatchingRoomDto;
}

interface MatchingRequestDto {
  id: number;
  hostUserId: number;
  hostDisplayName: string;
  attraction: string;
  preferredDateTime: string;
  maxParticipants: number;
  currentApplications: number;
  status: MatchingRequestStatus;
  description?: string;
  createdAt: string;
  updatedAt: string;
}
```

#### ステータスコード

- `201 Created`: 募集作成成功
- `400 Bad Request`: バリデーションエラー
- `401 Unauthorized`: 認証エラー
- `422 Unprocessable Entity`: ビジネスロジックエラー

#### バリデーションルール

- `attraction`: 必須、有効なアトラクション名
- `preferredDateTime`: 必須、現在時刻より未来
- `maxParticipants`: 必須、2-8 の範囲
- `description`: 任意、最大 500 文字

### 1.2 募集一覧取得

**GET** `/api/matching/requests`

アクティブな募集一覧を取得します。

#### クエリパラメータ

```typescript
interface MatchingRequestSearchRequest {
  attraction?: string;
  maxParticipants?: number;
  dateFrom?: string; // YYYY-MM-DD形式
  dateTo?: string; // YYYY-MM-DD形式
  status?: MatchingRequestStatus;
  page?: number;
  size?: number;
}
```

#### レスポンス

```typescript
MatchingRequestDto[]
```

#### ステータスコード

- `200 OK`: 取得成功
- `400 Bad Request`: パラメータエラー

### 1.3 募集詳細取得

**GET** `/api/matching/requests/{id}`

特定の募集の詳細情報を取得します。

#### パスパラメータ

- `id`: 募集 ID（数値）

#### レスポンス

```typescript
interface MatchingRequestWithActions extends MatchingRequestDto {
  userApplication?: MatchingApplicationDto;
  canApply: boolean;
  canEdit: boolean;
  canCancel: boolean;
}
```

#### ステータスコード

- `200 OK`: 取得成功
- `404 Not Found`: 募集が見つからない

### 1.4 自分の募集一覧取得

**GET** `/api/matching/requests/my`

ログインユーザーが作成した募集一覧を取得します。

#### レスポンス

```typescript
MatchingRequestDto[]
```

#### ステータスコード

- `200 OK`: 取得成功
- `401 Unauthorized`: 認証エラー

### 1.5 募集更新

**PUT** `/api/matching/requests/{id}`

募集情報を更新します（ホストのみ）。

#### パスパラメータ

- `id`: 募集 ID（数値）

#### リクエスト

```typescript
interface UpdateMatchingRequestRequest {
  attraction?: string;
  preferredDateTime?: string;
  maxParticipants?: number;
  description?: string;
}
```

#### レスポンス

```typescript
MatchingRequestDto;
```

#### ステータスコード

- `200 OK`: 更新成功
- `400 Bad Request`: バリデーションエラー
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 募集が見つからない

### 1.6 募集キャンセル

**DELETE** `/api/matching/requests/{id}`

募集をキャンセルします（ホストのみ）。

#### パスパラメータ

- `id`: 募集 ID（数値）

#### レスポンス

```typescript
MatchingRequestDto;
```

#### ステータスコード

- `200 OK`: キャンセル成功
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 募集が見つからない

## 2. 応募関連 API

### 2.1 募集に応募

**POST** `/api/matching/applications/requests/{requestId}`

募集に応募します。

#### パスパラメータ

- `requestId`: 募集 ID（数値）

#### リクエスト

```typescript
interface CreateMatchingApplicationRequest {
  message?: string; // 任意、最大300文字
}
```

#### レスポンス

```typescript
interface ApplyAndJoinRoomResponse {
  application: MatchingApplicationDto;
  room: MatchingRoomDto;
}

interface MatchingApplicationDto {
  id: number;
  requestId: number;
  applicantUserId: number;
  applicantDisplayName: string;
  status: MatchingApplicationStatus;
  message?: string;
  createdAt: string;
  updatedAt: string;
}
```

#### ステータスコード

- `201 Created`: 応募成功
- `400 Bad Request`: バリデーションエラー
- `401 Unauthorized`: 認証エラー
- `404 Not Found`: 募集が見つからない
- `409 Conflict`: 既に応募済み
- `422 Unprocessable Entity`: 応募制限エラー

### 2.2 自分の応募一覧取得

**GET** `/api/matching/applications/my`

ログインユーザーの応募一覧を取得します。

#### レスポンス

```typescript
MatchingApplicationDto[]
```

#### ステータスコード

- `200 OK`: 取得成功
- `401 Unauthorized`: 認証エラー

### 2.3 募集への応募一覧取得

**GET** `/api/matching/applications/requests/{requestId}`

特定の募集への応募一覧を取得します（ホストのみ）。

#### パスパラメータ

- `requestId`: 募集 ID（数値）

#### レスポンス

```typescript
MatchingApplicationDto[]
```

#### ステータスコード

- `200 OK`: 取得成功
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 募集が見つからない

### 2.4 応募ステータス更新

**PUT** `/api/matching/applications/{applicationId}/status`

応募のステータスを更新します（ホストのみ）。

#### パスパラメータ

- `applicationId`: 応募 ID（数値）

#### リクエスト

```typescript
interface UpdateApplicationStatusRequest {
  status: MatchingApplicationStatus;
  message?: string;
}
```

#### レスポンス

```typescript
MatchingApplicationDto;
```

#### ステータスコード

- `200 OK`: 更新成功
- `400 Bad Request`: バリデーションエラー
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 応募が見つからない

### 2.5 応募取り消し

**DELETE** `/api/matching/applications/requests/{requestId}`

応募を取り消します。

#### パスパラメータ

- `requestId`: 募集 ID（数値）

#### レスポンス

```typescript
boolean;
```

#### ステータスコード

- `200 OK`: 取り消し成功
- `401 Unauthorized`: 認証エラー
- `404 Not Found`: 応募が見つからない

## 3. 待機室関連 API

### 3.1 待機室情報取得

**GET** `/api/matching/rooms/{roomId}`

待機室の詳細情報を取得します。

#### パスパラメータ

- `roomId`: 待機室 ID（数値）

#### レスポンス

```typescript
interface MatchingRoomDto {
  id: number;
  requestId: number;
  status: MatchingRoomStatus;
  participants: RoomParticipantDto[];
  createdAt: string;
  updatedAt: string;
}

interface RoomParticipantDto {
  userId: number;
  displayName: string;
  joinedAt: string;
  isHost: boolean;
}
```

#### ステータスコード

- `200 OK`: 取得成功
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 待機室が見つからない

### 3.2 待機室参加

**POST** `/api/matching/rooms/{roomId}/join`

待機室に参加します。

#### パスパラメータ

- `roomId`: 待機室 ID（数値）

#### レスポンス

```typescript
MatchingRoomDto;
```

#### ステータスコード

- `200 OK`: 参加成功
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: 待機室が見つからない
- `409 Conflict`: 既に参加済み

## 4. チャット関連 API

### 4.1 チャットルーム情報取得

**GET** `/api/matching/chat-rooms/{roomId}`

チャットルームの詳細情報を取得します。

#### パスパラメータ

- `roomId`: チャットルーム ID（数値）

#### レスポンス

```typescript
interface ChatRoomDto {
  id: number;
  requestId: number;
  participants: ChatParticipantDto[];
  messages: ChatMessageDto[];
  createdAt: string;
  updatedAt: string;
}

interface ChatParticipantDto {
  userId: number;
  displayName: string;
  joinedAt: string;
  isHost: boolean;
}

interface ChatMessageDto {
  id: number;
  userId: number;
  displayName: string;
  content: string;
  createdAt: string;
}
```

#### ステータスコード

- `200 OK`: 取得成功
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: チャットルームが見つからない

### 4.2 メッセージ送信

**POST** `/api/matching/chat-rooms/{roomId}/messages`

チャットルームにメッセージを送信します。

#### パスパラメータ

- `roomId`: チャットルーム ID（数値）

#### リクエスト

```typescript
interface CreateChatMessageRequest {
  content: string; // 最大1000文字
}
```

#### レスポンス

```typescript
ChatMessageDto;
```

#### ステータスコード

- `201 Created`: 送信成功
- `400 Bad Request`: バリデーションエラー
- `401 Unauthorized`: 認証エラー
- `403 Forbidden`: 権限エラー
- `404 Not Found`: チャットルームが見つからない

## 5. 統合 API

### 5.1 募集作成と待機室作成

**POST** `/api/matching/create-with-room`

募集作成と待機室作成を一括で実行します。

#### リクエスト

```typescript
CreateMatchingRequestRequest;
```

#### レスポンス

```typescript
CreateMatchingResponse;
```

### 5.2 応募と待機室参加

**POST** `/api/matching/apply-and-join-room/{requestId}`

応募と待機室参加を一括で実行します。

#### パスパラメータ

- `requestId`: 募集 ID（数値）

#### リクエスト

```typescript
CreateMatchingApplicationRequest;
```

#### レスポンス

```typescript
ApplyAndJoinRoomResponse;
```

## 6. 列挙型定義

### 6.1 募集ステータス

```typescript
enum MatchingRequestStatus {
  OPEN = "OPEN", // 募集中
  WAITING = "WAITING", // 待機中
  CONFIRMED = "CONFIRMED", // 確定済み
  CLOSED = "CLOSED", // 終了
  EXPIRED = "EXPIRED", // 期限切れ
}
```

### 6.2 応募ステータス

```typescript
enum MatchingApplicationStatus {
  PENDING = "PENDING", // 承認待ち
  ACCEPTED = "ACCEPTED", // 承認済み
  REJECTED = "REJECTED", // 拒否
}
```

### 6.3 待機室ステータス

```typescript
enum MatchingRoomStatus {
  WAITING = "WAITING", // 待機中
  CONFIRMED = "CONFIRMED", // 確定済み
  CLOSED = "CLOSED", // 終了
}
```

## 7. エラーコード一覧

| HTTP ステータス | エラーコード               | 説明                 |
| --------------- | -------------------------- | -------------------- |
| 400             | VALIDATION_ERROR           | バリデーションエラー |
| 400             | INVALID_DATE_TIME          | 無効な日時           |
| 400             | INVALID_PARTICIPANTS       | 無効な参加者数       |
| 401             | UNAUTHORIZED               | 認証が必要           |
| 403             | FORBIDDEN                  | 権限が不足           |
| 404             | REQUEST_NOT_FOUND          | 募集が見つからない   |
| 404             | APPLICATION_NOT_FOUND      | 応募が見つからない   |
| 404             | ROOM_NOT_FOUND             | 待機室が見つからない |
| 409             | ALREADY_APPLIED            | 既に応募済み         |
| 409             | ALREADY_JOINED             | 既に参加済み         |
| 422             | APPLICATION_LIMIT_EXCEEDED | 応募制限超過         |
| 422             | REQUEST_FULL               | 募集が満員           |
| 422             | REQUEST_CLOSED             | 募集が終了           |
| 500             | INTERNAL_SERVER_ERROR      | 内部サーバーエラー   |

## 8. レート制限

- 募集作成: 1 分間に 3 回まで
- 応募: 1 分間に 5 回まで
- メッセージ送信: 1 分間に 20 回まで

## 9. WebSocket API

### 9.1 接続

**WebSocket** `/ws/matching/{roomId}`

待機室またはチャットルームの WebSocket 接続

#### パスパラメータ

- `roomId`: 待機室またはチャットルーム ID

#### 認証

クエリパラメータで JWT トークンを送信:

```
/ws/matching/123?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 9.2 メッセージ形式

#### 参加者参加通知

```json
{
  "type": "PARTICIPANT_JOINED",
  "data": {
    "userId": 123,
    "displayName": "ユーザー名",
    "joinedAt": "2024-01-01T12:00:00Z"
  }
}
```

#### 参加者退出通知

```json
{
  "type": "PARTICIPANT_LEFT",
  "data": {
    "userId": 123,
    "displayName": "ユーザー名"
  }
}
```

#### チャットメッセージ

```json
{
  "type": "CHAT_MESSAGE",
  "data": {
    "id": 456,
    "userId": 123,
    "displayName": "ユーザー名",
    "content": "メッセージ内容",
    "createdAt": "2024-01-01T12:00:00Z"
  }
}
```

#### 募集ステータス変更通知

```json
{
  "type": "REQUEST_STATUS_CHANGED",
  "data": {
    "requestId": 789,
    "status": "CONFIRMED",
    "updatedAt": "2024-01-01T12:00:00Z"
  }
}
```

この API 仕様書は、マッチングシステムの全機能を網羅しており、フロントエンドとバックエンドの連携に必要なすべての情報を含んでいます。
