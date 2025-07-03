# マッチングシステム シーケンス図

## 1. 募集作成フロー

```mermaid
sequenceDiagram
    participant U as ユーザー
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース

    U->>F: 募集作成画面を開く
    F->>F: アトラクション一覧を表示

    U->>F: アトラクション、日時、参加者数を入力
    F->>F: バリデーション（日時チェック）

    U->>F: 募集作成ボタンをクリック
    F->>B: POST /api/matching/requests
    Note over F,B: CreateMatchingRequestRequest

    B->>B: バリデーション
    B->>DB: 募集情報を保存
    B->>DB: 待機室を作成
    B->>B: ホストを待機室に参加させる

    B->>F: 201 Created + MatchingRequestDto
    F->>F: 成功メッセージを表示
    F->>F: 募集一覧画面に遷移
```

## 2. 募集一覧表示・検索フロー

```mermaid
sequenceDiagram
    participant U as ユーザー
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース

    U->>F: 募集一覧画面を開く
    F->>B: GET /api/matching/requests
    B->>DB: アクティブな募集を取得
    DB->>B: 募集一覧
    B->>F: 200 OK + MatchingRequestDto[]
    F->>F: 募集カードを表示

    U->>F: 検索条件を入力
    F->>B: GET /api/matching/requests?attraction=xxx&maxParticipants=4
    B->>DB: 条件に合う募集を検索
    DB->>B: フィルタリング結果
    B->>F: 200 OK + フィルタリング結果
    F->>F: 検索結果を表示
```

## 3. 応募フロー

```mermaid
sequenceDiagram
    participant U as 参加者
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース

    U->>F: 募集カードの「応募する」をクリック
    F->>F: 応募制限チェック
    F->>F: 応募モーダルを表示

    U->>F: 「応募する」ボタンをクリック
    F->>B: POST /api/matching/applications/requests/{requestId}
    Note over F,B: CreateMatchingApplicationRequest

    B->>B: 応募制限チェック（最大1つ）
    B->>DB: 応募情報を保存
    B->>DB: 募集の応募数を更新

    B->>F: 201 Created + MatchingApplicationDto
    F->>F: 成功メッセージを表示
    F->>F: モーダルを閉じる
    F->>F: 募集一覧を更新
```

## 4. 応募承認フロー（ホスト側）

```mermaid
sequenceDiagram
    participant H as ホスト
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース

    H->>F: 自分の募集詳細を開く
    F->>B: GET /api/matching/applications/requests/{requestId}
    B->>DB: 応募一覧を取得
    DB->>B: 応募一覧
    B->>F: 200 OK + MatchingApplicationDto[]
    F->>F: 応募者一覧を表示

    H->>F: 応募者を承認
    F->>B: PUT /api/matching/applications/{applicationId}/status
    Note over F,B: UpdateApplicationStatusRequest

    B->>B: 応募ステータスを更新
    B->>DB: 応募ステータスを保存
    B->>DB: 参加者を待機室に追加

    B->>F: 200 OK + MatchingApplicationDto
    F->>F: 承認完了メッセージを表示
    F->>F: 応募者一覧を更新
```

## 5. 待機室参加フロー

```mermaid
sequenceDiagram
    participant P as 参加者
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース
    participant WS as WebSocket

    P->>F: 承認された募集の「待機室へ」をクリック
    F->>B: GET /api/matching/rooms/{roomId}
    B->>DB: 待機室情報を取得
    DB->>B: 待機室情報
    B->>F: 200 OK + MatchingRoomDto
    F->>F: 待機室画面を表示

    F->>WS: WebSocket接続
    WS->>F: 接続確立
    F->>WS: 参加者情報を送信
    WS->>F: 他の参加者情報を受信
    F->>F: 参加者一覧を更新

    P->>F: メッセージを入力
    F->>WS: メッセージを送信
    WS->>F: 他の参加者にメッセージを配信
    F->>F: チャット履歴を更新
```

## 6. 募集確定・チャットルーム移行フロー

```mermaid
sequenceDiagram
    participant H as ホスト
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース
    participant WS as WebSocket

    H->>F: 募集確定ボタンをクリック
    F->>B: PUT /api/matching/requests/{requestId}/status
    Note over F,B: status: CONFIRMED

    B->>B: 募集ステータスを更新
    B->>DB: 募集ステータスを保存
    B->>DB: チャットルームを作成
    B->>DB: 待機室参加者をチャットルームに移行

    B->>F: 200 OK + MatchingRequestDto
    F->>F: 確定完了メッセージを表示
    F->>F: チャットルーム画面に遷移

    F->>WS: WebSocket接続（チャットルーム）
    WS->>F: 接続確立
    F->>WS: 参加者情報を送信
    WS->>F: 他の参加者情報を受信
    F->>F: チャット参加者一覧を更新
```

## 7. エラーハンドリングフロー

```mermaid
sequenceDiagram
    participant U as ユーザー
    participant F as フロントエンド
    participant B as バックエンド

    U->>F: 操作を実行
    F->>B: APIリクエスト

    alt バリデーションエラー
        B->>B: バリデーション失敗
        B->>F: 400 Bad Request + エラーメッセージ
        F->>F: エラーメッセージを表示
    else 認証エラー
        B->>B: 認証失敗
        B->>F: 401 Unauthorized
        F->>F: ログイン画面にリダイレクト
    else 認可エラー
        B->>B: 認可失敗
        B->>F: 403 Forbidden + エラーメッセージ
        F->>F: エラーメッセージを表示
    else サーバーエラー
        B->>B: 内部エラー
        B->>F: 500 Internal Server Error
        F->>F: 一般的なエラーメッセージを表示
    end
```

## 8. 応募制限チェックフロー

```mermaid
sequenceDiagram
    participant U as ユーザー
    participant F as フロントエンド
    participant B as バックエンド
    participant DB as データベース

    U->>F: 募集一覧画面を開く
    F->>B: GET /api/matching/applications/my
    B->>DB: ユーザーの応募履歴を取得
    DB->>B: 応募履歴
    B->>F: 200 OK + MatchingApplicationDto[]
    F->>F: 応募状況をチェック

    alt 既に応募済み
        F->>F: 応募ボタンを無効化
        F->>F: 「応募済み」表示
    else 応募可能
        F->>F: 応募ボタンを有効化
    end

    U->>F: 応募ボタンをクリック
    F->>F: 応募制限を再チェック

    alt 制限に引っかかる
        F->>F: 警告メッセージを表示
        F->>F: 応募を中止
    else 応募可能
        F->>B: 応募リクエスト
        B->>B: サーバー側でも制限チェック
        B->>F: 応募結果
    end
```

## 9. リアルタイム更新フロー

```mermaid
sequenceDiagram
    participant U1 as ユーザー1
    participant U2 as ユーザー2
    participant F1 as フロントエンド1
    participant F2 as フロントエンド2
    participant B as バックエンド
    participant WS as WebSocket

    U1->>F1: 募集を作成
    F1->>B: 募集作成リクエスト
    B->>B: 募集を保存
    B->>F1: 作成完了

    F2->>B: 定期的な募集一覧更新
    B->>F2: 新しい募集を含む一覧
    F2->>F2: 募集一覧を更新

    U2->>F2: 募集に応募
    F2->>B: 応募リクエスト
    B->>B: 応募を保存
    B->>F2: 応募完了

    F1->>B: 定期的な応募状況更新
    B->>F1: 新しい応募を含む状況
    F1->>F1: 応募状況を更新

    U1->>F1: 応募を承認
    F1->>B: 承認リクエスト
    B->>B: 承認処理
    B->>WS: 承認通知をブロードキャスト
    WS->>F2: 承認通知を受信
    F2->>F2: 応募ステータスを更新
```

これらのシーケンス図は、マッチングシステムの主要なフローを視覚的に表現しています。各フローは実際の実装に基づいて作成されており、フロントエンドとバックエンドの連携、データベース操作、WebSocket通信などが含まれています。
