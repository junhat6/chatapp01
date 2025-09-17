---
id: docker-up
title: Docker でインフラ（DB/Redis）を起動
scope: db, cache
env: local
duration: ~5min
prerequisites:
  - Docker / Docker Compose
rollback: true
impact: ローカルのみ
---

## 目的
ローカル開発時に必要な PostgreSQL と Redis を Docker で起動・停止する。

## 手順
1. 起動
   ```bash
   docker compose up -d postgres redis
   docker compose ps
   ```

2. ログ確認（必要に応じて）
   ```bash
   docker compose logs -n 200 -f | cat
   ```

## 検証
- PostgreSQL ヘルスチェック（コンテナ内）
  ```bash
  docker exec usjchatapp-postgres pg_isready -U usjchatapp -d usjchatapp
  ```
  `accepting connections` が表示される。
- Redis ヘルスチェック
  ```bash
  docker exec usjchatapp-redis redis-cli ping
  ```
  `PONG` が返る。

## 代表的な失敗と対処
- 画像ビルド/起動に失敗する
  ```bash
  docker compose down -v
  docker builder prune -af
  docker compose up -d postgres redis
  ```
- ポート衝突（5432/6379）
  - 使用中プロセスを停止、もしくは `docker-compose.yml` のポートマッピングを変更

## ロールバック
```bash
docker compose down -v
```

## 参照
- `docker-compose.yml`（サービス: `postgres`, `redis`）
- 初期化 SQL: `docker/init.sql` 