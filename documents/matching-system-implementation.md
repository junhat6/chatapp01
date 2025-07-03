# マッチング募集機能 実装ドキュメント

## 概要

マッチング募集機能は、ユーザーが USJ のアトラクションに一緒に行く仲間を募集・応募できるシステムです。ホストが募集を作成し、参加者が応募して、承認後に待機室でチャットができる仕組みになっています。

## 主要機能

### 1. 募集作成機能

- ホストがアトラクション、日時、最大参加者数を指定して募集を作成
- 募集と同時に待機室が自動作成される
- 募集ステータス管理（OPEN, WAITING, CONFIRMED, CLOSED, EXPIRED）

### 2. 募集一覧・検索機能

- アクティブな募集の一覧表示
- アトラクション、参加者数、日付での絞り込み検索
- 募集状況のリアルタイム表示

### 3. 応募機能

- 参加者は最大 1 つの募集にのみ応募可能
- メッセージ入力は不要（シンプルな応募）
- 応募ステータス管理（PENDING, ACCEPTED, REJECTED）

### 4. 待機室・チャット機能

- 承認された参加者は待機室に参加可能
- リアルタイムチャット機能
- 募集確定後にチャットルームに移行

## 技術スタック

### フロントエンド

- **フレームワーク**: Vue 3 + TypeScript
- **状態管理**: Pinia
- **UI ライブラリ**: Naive UI
- **HTTP 通信**: Axios
- **ルーティング**: Vue Router

### バックエンド

- **フレームワーク**: Spring Boot + Java
- **データベース**: PostgreSQL
- **認証**: JWT
- **WebSocket**: リアルタイム通信

## データモデル

### 主要エンティティ

```typescript
// 募集情報
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

// 応募情報
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

// 待機室情報
interface MatchingRoomDto {
  id: number;
  requestId: number;
  status: MatchingRoomStatus;
  participants: RoomParticipantDto[];
  createdAt: string;
  updatedAt: string;
}

// チャットルーム情報
interface ChatRoomDto {
  id: number;
  requestId: number;
  participants: ChatParticipantDto[];
  messages: ChatMessageDto[];
  createdAt: string;
  updatedAt: string;
}
```

## フロントエンド実装詳細

### 1. 状態管理 (Pinia Store)

#### `useMatchingStore`

```typescript
// 主要な状態
const activeRequests = ref<MatchingRequestDto[]>([])
const myRequests = ref<MatchingRequestDto[]>([])
const myApplications = ref<MatchingApplicationDto[]>([])
const myRooms = ref<MatchingRoomDto[]>([])

// 主要なアクション
const fetchActiveRequests = async (params?: MatchingRequestSearchRequest)
const createRequest = async (data: CreateMatchingRequestRequest)
const applyToRequest = async (requestId: number, data: CreateMatchingApplicationRequest)
const fetchMyApplications = async ()
```

### 2. API サービス層

#### `matchingRequestApi`

- 募集の CRUD 操作
- 検索・フィルタリング機能
- ステータス管理

#### `matchingApplicationApi`

- 応募の作成・管理
- 応募ステータス更新
- 応募可能チェック

#### `matchingApi`

- 募集作成と待機室作成の統合
- 応募と待機室参加の統合

### 3. コンポーネント構成

#### 募集作成画面 (`MatchingCreateView.vue`)

- アトラクション選択（カテゴリ別）
- 日時選択（現在時刻より未来）
- 参加者数設定
- 説明文入力（任意）

#### 募集一覧画面 (`MatchingListView.vue`)

- 募集カード表示
- 検索・フィルタリング
- 応募ボタン（制限付き）
- 待機室・チャットへの遷移

#### 応募モーダル (`ApplicationModal.vue`)

- 募集詳細表示
- 応募制限チェック
- シンプルな応募（メッセージ不要）

## バックエンド実装詳細

### 1. コントローラー層

#### `MatchingRequestController`

```java
@RestController
@RequestMapping("/api/matching/requests")
public class MatchingRequestController {

    @PostMapping
    public ResponseEntity<ApiResponse<MatchingRequestDto>> createRequest(
        @RequestBody CreateMatchingRequestRequest request)

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchingRequestDto>>> getActiveRequests(
        @RequestParam(required = false) String attraction,
        @RequestParam(required = false) Integer maxParticipants,
        @RequestParam(required = false) String dateFrom)

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MatchingRequestDto>>> getMyRequests()
}
```

#### `MatchingApplicationController`

```java
@RestController
@RequestMapping("/api/matching/applications")
public class MatchingApplicationController {

    @PostMapping("/requests/{requestId}")
    public ResponseEntity<ApiResponse<MatchingApplicationDto>> applyToRequest(
        @PathVariable Long requestId,
        @RequestBody CreateMatchingApplicationRequest request)

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MatchingApplicationDto>>> getMyApplications()

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApiResponse<MatchingApplicationDto>> updateStatus(
        @PathVariable Long applicationId,
        @RequestBody UpdateApplicationStatusRequest request)
}
```

### 2. サービス層

#### `MatchingService`

- 募集作成時の待機室自動作成
- 応募時の重複チェック
- ステータス遷移管理

#### `MatchingApplicationService`

- 応募制限チェック（最大 1 つ）
- 応募ステータス管理
- 承認時の待機室参加処理

### 3. データアクセス層

#### `MatchingRequestRepository`

- 募集情報の CRUD
- 検索条件に基づくクエリ
- ステータス別フィルタリング

#### `MatchingApplicationRepository`

- 応募情報の CRUD
- ユーザー別応募履歴
- 募集別応募一覧

## セキュリティ・バリデーション

### 1. 認証・認可

- JWT トークンによる認証
- ユーザー ID による認可チェック
- ホストのみが募集を編集・削除可能

### 2. バリデーション

- 日時は現在時刻より未来である必要
- 参加者数は 2-8 人の範囲
- 応募は最大 1 つまで
- 募集ステータスに応じた操作制限

### 3. エラーハンドリング

- フロントエンド：ユーザーフレンドリーなエラーメッセージ
- バックエンド：詳細なエラー情報と HTTP ステータス
- ネットワークエラーの適切な処理

## パフォーマンス最適化

### 1. フロントエンド

- コンポーネントの適切な分割
- 計算プロパティによる効率的な再計算
- 不要な API 呼び出しの削減

### 2. バックエンド

- データベースクエリの最適化
- インデックスの適切な設定
- ページネーション対応

## 今後の拡張予定

### 1. 機能拡張

- 通知機能（メール・プッシュ通知）
- 評価・レビュー機能
- 写真共有機能

### 2. パフォーマンス改善

- キャッシュ機能の導入
- CDN 活用
- データベース最適化

### 3. セキュリティ強化

- レート制限の実装
- 入力値のサニタイゼーション強化
- 監査ログの実装
