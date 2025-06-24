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

## 🚀 セットアップ & 実行手順

### 前提条件
- Java 17以上
- Node.js 18以上 & npm/yarn
- Docker（PostgreSQL使用時のみ）

### 🎯 簡単起動（推奨）

```bash
# プロジェクトルートで実行
./start-dev.sh
```

起動時に以下のオプションを選択できます：
1. **PostgreSQL（Docker）** - 本番環境と同じデータベース（Dockerが必要）
2. **H2インメモリ** - セットアップ不要、即座に起動

### 📊 データベースオプション詳細

#### オプション1: PostgreSQL（Docker）- 本番相当
```bash
# PostgreSQLをDockerで起動
docker-compose up -d postgres

# バックエンド起動
cd backend && ./gradlew bootRun
```

#### オプション2: H2インメモリ - 開発専用
```bash
# H2データベースでバックエンド起動（セットアップ不要）
cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

**H2データベースコンソール**: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- ユーザー名: `sa`
- パスワード: （空欄）

#### 手動PostgreSQL設定（従来方法）

PostgreSQLデータベースとユーザーを作成：

```sql
-- PostgreSQL に接続して以下を実行
CREATE DATABASE usjchatapp;
CREATE USER usjchatapp WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE usjchatapp TO usjchatapp;
```

バックエンドは `http://localhost:8080` で起動します。

### 3. フロントエンド起動

```bash
cd frontend

# 依存関係をインストール
npm install
# または
yarn install

# 開発サーバーを起動
npm run dev
# または
yarn dev
```

フロントエンドは `http://localhost:3000` で起動します。

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
- PostgreSQLが起動していることを確認
- データベース名、ユーザー名、パスワードが正しいことを確認

### フロントエンド起動エラー
- Node.js のバージョンが18以上であることを確認
- `npm install` または `yarn install` を実行

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