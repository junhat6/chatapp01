# ドキュメントビューアー 使用方法

## 概要

このドキュメントビューアーは、マッチングシステムのドキュメントをWebブラウザで閲覧できるようにするためのツールです。

## 起動方法

### 1. Docker Composeを使用（推奨）

```bash
# documentsディレクトリに移動
cd documents

# Docker Composeで起動
docker-compose up -d

# ブラウザでアクセス
# http://localhost:8100
```

### 2. 直接実行

```bash
# documentsディレクトリに移動
cd documents

# 依存関係をインストール
npm install

# サーバーを起動
npm start

# ブラウザでアクセス
# http://localhost:8100
```

## 機能

- **サイドバーナビゲーション**: 左側のサイドバーから各ドキュメントにアクセス
- **Markdown表示**: GitHub風のMarkdownレンダリング
- **シンタックスハイライト**: コードブロックの自動ハイライト
- **レスポンシブデザイン**: モバイルデバイスにも対応
- **印刷対応**: 印刷時にサイドバーを非表示

## 利用可能なドキュメント

1. **概要** (`README.md`) - ドキュメントの概要と目次
2. **実装ドキュメント** (`matching-system-implementation.md`) - システム実装の詳細
3. **シーケンス図** (`matching-system-sequence-diagrams.md`) - システムフローの図解
4. **API仕様書** (`matching-api-specification.md`) - 完全なAPI仕様

## 停止方法

### Docker Composeを使用している場合

```bash
# コンテナを停止
docker-compose down

# コンテナとイメージを削除（オプション）
docker-compose down --rmi all
```

### 直接実行している場合

```bash
# Ctrl+C でサーバーを停止
```

## トラブルシューティング

### ポート8100が使用中の場合

```bash
# 使用中のプロセスを確認
lsof -i :8100

# プロセスを終了
kill -9 <PID>
```

### Dockerコンテナが起動しない場合

```bash
# ログを確認
docker-compose logs

# コンテナを再ビルド
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 開発者向け情報

### ファイル構成

```
documents/
├── Dockerfile              # Dockerイメージ定義
├── docker-compose.yml      # Docker Compose設定
├── package.json           # Node.js依存関係
├── server.js              # Express.jsサーバー
├── start.sh               # 起動スクリプト
├── public/
│   └── styles.css         # CSSスタイル
├── README.md              # ドキュメント概要
├── matching-system-implementation.md
├── matching-system-sequence-diagrams.md
└── matching-api-specification.md
```

### カスタマイズ

- **スタイル変更**: `public/styles.css`を編集
- **サーバー設定**: `server.js`を編集
- **新しいドキュメント**: Markdownファイルを追加し、`server.js`のサイドバー生成部分を更新 