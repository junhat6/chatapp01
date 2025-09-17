# 🎢 USJ Chat App

ユニバーサル・スタジオ・ジャパン（USJ）内でのリアルタイムマッチングアプリです。

## 🔧 技術スタック

### バックエンド
- **言語**: Kotlin
- **フレームワーク**: Spring Boot 3.2.0
- **認証**: JWT (JSON Web Token)
- **データベース**: PostgreSQL
- **リアルタイム通信**: WebSocket (予定)
- **ビルドツール**: Gradle

### フロントエンド
- **言語**: TypeScript
- **フレームワーク**: Vue 3
- **ビルドツール**: Vite
- **状態管理**: Pinia
- **ルーティング**: Vue Router
- **HTTP クライアント**: Axios

## 📁 プロジェクト構造

```
chatapp01/
├── backend/                     # Spring Boot (Kotlin) バックエンド
│   ├── src/main/kotlin/com/usjchatapp/
│   │   ├── domain/             # ドメイン層
│   │   │   ├── entity/         # エンティティ
│   │   │   └── repository/     # リポジトリインターフェース
│   │   ├── application/        # アプリケーション層
│   │   │   ├── dto/           # データ転送オブジェクト
│   │   │   └── service/       # サービスクラス
│   │   ├── infrastructure/     # インフラストラクチャ層
│   │   │   ├── config/        # 設定クラス
│   │   │   └── security/      # セキュリティ関連
│   │   └── presentation/       # プレゼンテーション層
│   │       └── controller/    # REST API コントローラー
│   ├── build.gradle.kts       # Gradle ビルド設定
│   └── src/main/resources/
│       └── application.yml    # アプリケーション設定
└── frontend/                   # Vue 3 (TypeScript) フロントエンド
    ├── src/
    │   ├── views/             # ページコンポーネント
    │   ├── stores/            # Pinia ストア
    │   ├── services/          # API サービス
    │   ├── types/             # TypeScript 型定義
    │   └── router/            # Vue Router 設定
    ├── package.json           # npm 依存関係
    └── vite.config.ts         # Vite 設定
```

## 🚀 セットアップ & 実行手順（PostgreSQL で起動）

### 前提条件
- Java 17以上
- Node.js 18以上 & npm/yarn
- Docker と Docker Compose（必須）

### 1. インフラ起動（PostgreSQL + Redis）
```bash
# プロジェクトルートで実行
docker-compose up -d
```

- PostgreSQL は `localhost:5432` に公開され、初期データベース/ユーザーは `usjchatapp`、パスワードは `password` です（`docker-compose.yml` に定義済み）。
- Redis は `localhost:6379` に公開されます。

### 2. バックエンド起動
```bash
cd backend
./gradlew bootRun
```
- バックエンドは `http://localhost:8080` で起動します。
- `backend/src/main/resources/application.yml` はデフォルトでローカルの PostgreSQL に接続する設定になっています（環境変数の設定は不要）。

### 3. フロントエンド起動
```bash
cd frontend

# 依存関係をインストール
npm ci || npm install

# 開発サーバーを起動
npm run dev
```
- フロントエンドは `http://localhost:3000` で起動します。

## 📚 ランブック（操作手順書）
- 総則・目次: `docs/RUNBOOK.md`
- ローカル開発の起動: `docs/runbooks/local-dev.md`
- Docker でインフラ起動: `docs/runbooks/docker.md`
- API 仕様同期チェック: `docs/runbooks/api-sync.md`

### ランブック検証（ローカル）
```bash
node scripts/validate-runbooks.mjs
# または
bash scripts/validate-runbooks.sh
```

### ランブック検証（CI）
- GitHub Actions: `.github/workflows/runbook-validate.yml`
- 対象変更: `docs/**/*.md` および検証スクリプト

## 🌐 API エンドポイント

### 認証関連
- `POST /api/auth/signup` - ユーザー新規登録
- `POST /api/auth/signin` - ユーザーログイン
- `GET /api/auth/me` - 現在のユーザー情報取得

### ヘルスチェック
- `GET /api/health` - サーバー稼働状況確認

## 💻 現在の実装状況

### ✅ 実装済み機能
- ユーザー登録・ログイン機能（JWT認証）
- プロフィール表示
- フロントエンド認証状態管理
- レスポンシブなUI/UX
- セキュアなCORS設定
- バリデーション機能

### 🚧 今後の実装予定
- WebSocketによるリアルタイムチャット
- チャットルーム作成・参加機能
- 位置情報ベースのマッチング
- グループチャット機能
- フレンド機能
- Redis導入（セッション管理・キャッシュ）

## 🔐 セキュリティ機能

- JWT トークンベースの認証
- パスワードハッシュ化（BCrypt）
- CORS設定
- リクエストバリデーション
- セキュアなHTTPヘッダー設定

## 🎯 使用方法

1. アプリケーションを起動
2. `http://localhost:3000` にアクセス
3. 「新規登録」でアカウント作成
4. ログイン後、ダッシュボードで機能を確認
5. 今後のチャット機能実装をお待ちください！

## 📝 環境変数

バックエンドの環境変数（`application.yml` で設定可能）：

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:usjchatapp}
    password: ${DB_PASSWORD:password}
  security:
    jwt:
      secret: ${JWT_SECRET:mySecretKey123456789012345678901234567890}
      expiration: 86400000
```

## 🐛 トラブルシューティング

### データベース接続エラー
- `docker-compose up -d` が完了していることを確認
- コンテナのヘルスチェックが `healthy` になっていることを確認
- データベース名、ユーザー名、パスワードが正しいことを確認

### フロントエンド起動エラー
- Node.js のバージョンが18以上であることを確認
- `npm ci` または `npm install` を実行

### CORS エラー
- バックエンドとフロントエンドが異なるポートで動作していることを確認
- CORS設定が正しく行われていることを確認

## 🤝 開発・貢献

このプロジェクトはクリーンアーキテクチャの原則に従って構成されています：

- **ドメイン層**: ビジネスロジックとエンティティ
- **アプリケーション層**: ユースケースとサービス
- **インフラストラクチャ層**: データベース、外部API
- **プレゼンテーション層**: REST API、コントローラー

コード品質とベストプラクティスを意識した開発を心がけています。

---

**USJ Chat App** - USJでの出会いをもっと楽しく！ 🎢✨ 