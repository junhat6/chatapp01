---
id: local-dev-start
title: ローカル開発環境の起動
scope: backend, frontend
env: local
duration: ~5-10min
prerequisites:
  - Java 17+
  - Node.js 18+ / npm 9+
  - Docker（PostgreSQL/Redisを使う場合）
rollback: true
impact: ローカルのみ
---

## 目的
バックエンド（Spring Boot）とフロントエンド（Vite + Vue）をローカルで起動する。

## 手順
1. 依存のインストール
   - Backend: 実行に必要な Gradle Wrapper は同梱済み
     ```bash
     cd backend
     ./gradlew --version | cat
     ```
   - Frontend: Node モジュールをインストール
     ```bash
     cd frontend
     npm ci
     ```

2. バックエンドの起動（PostgreSQL/Redis を Docker で利用）
   ```bash
   docker compose up -d postgres redis
   cd backend
   ./gradlew bootRun --no-daemon
   ```

3. フロントエンドの起動
   ```bash
   cd frontend
   npm run dev -- --host
   ```

## 検証
- Backend ヘルスチェック（200 OK）
  ```bash
  curl -f http://localhost:8080/api/health
  ```
- Frontend（ブラウザでアクセス）
  - `http://localhost:3000/` にアクセスし、トップが表示される

## 代表的な失敗と対処
- Java バージョン相違: `java -version` を確認し、17+ に切替
- ポート衝突: 8080/3000/5432/6379 使用中プロセスを停止
- 依存未解決: `npm ci` や `./gradlew --refresh-dependencies` を再実行

## ロールバック
- 起動プロセスの停止
  ```bash
  lsof -i :8080 -sTCP:LISTEN -n -P | awk 'NR>1{print $2}' | xargs -r kill -9
  lsof -i :3000 -sTCP:LISTEN -n -P | awk 'NR>1{print $2}' | xargs -r kill -9
  docker compose down -v
  ```

## 参考
- `./start-dev.sh` は対話式の選択により PostgreSQL（Docker）または H2 での起動を補助する。 