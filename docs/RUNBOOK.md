---
id: runbook-index
title: USJ Chat App ランブック
version: 0.1.0
owner: team-usjchatapp
last_update: 2025-09-16
---

## 目的
本書は USJ Chat App の操作手順を一元化し、AI（Cursor）や人間の両方が安全・確実に実行できるようにする。各手順は 5〜10 分で完結する小粒度を原則とし、目的/前提/手順/検証/ロールバックを必ずセットで記述する。

## 書き方の原則
- 一手順＝一目的（小粒度、独立実行可能）
- 先頭に YAML メタ情報（id/scope/env/duration/prerequisites/rollback/impact）
- ファイル・ディレクトリ・関数・コマンドはバッククォートで表記（例: `backend/build.gradle.kts`）
- コマンドは非対話・再実行可能を優先（`--no-daemon`、`--yes` 等）
- 必ず「検証」と「ロールバック」を明記し、未整備の場合は「未整備」と記載
- 代表的な失敗と対処を併記

## 目次（Runbooks）
- ローカル開発の起動: `docs/runbooks/local-dev.md`
- Docker でインフラ起動: `docs/runbooks/docker.md`
- API 仕様同期チェック: `docs/runbooks/api-sync.md`

## 共通参照
- ポート
  - Backend: 8080（`backend/src/main/resources/application.yml`）
  - Frontend: 3000（`frontend/vite.config.ts`）
  - PostgreSQL: 5432（`docker-compose.yml`）
  - Redis: 6379（`docker-compose.yml`）
- 主要エンドポイント
  - Health: `GET /api/health`
- 主要スクリプト
  - `./start-dev.sh`（選択式起動: PostgreSQL Docker or H2）
- 主要ディレクトリ
  - `backend/`, `frontend/`, `docs/`
- 環境変数（例）
  - `DB_USERNAME`（既定: `usjchatapp`）
  - `DB_PASSWORD`（既定: `password`）
  - `JWT_SECRET`（既定あり。実運用では上書き必須）

## 運用ルール
- 新しい手順は `docs/runbooks/` に Markdown で追加
- コマンドは可能な限りスクリプト化し、手順書はその呼び出しのみを記載
- 手順更新とスクリプト更新はコミットを分け、履歴の可読性を担保 