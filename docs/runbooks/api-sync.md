---
id: api-sync-check
title: API 仕様とフロントの同期チェック
scope: frontend, backend
env: local
duration: ~1-2min
prerequisites:
  - Node.js 18+ / npm 9+
rollback: true
impact: ローカルのみ
---

## 目的
`scripts/check-api-sync.js` を使い、API 仕様とフロントエンド実装の整合性を検証する。

## 手順
```bash
node scripts/check-api-sync.js
```

## 検証
- 終了コード 0 で正常終了し、差分なしの表示であること

## 代表的な失敗と対処
- 仕様変更により差分が検出された
  - 該当 API の DTO/型やパス定数を更新（例: `frontend/src/constants/apiPaths.ts`, `backend/src/main/kotlin/com/usjchatapp/common/constants/ApiPaths.kt`）
  - 更新後に再度本コマンドを実行

## ロールバック
- 変更が不適切な場合は Git で差分を破棄
  ```bash
  git restore -SW .
  ```

## 参照
- `scripts/check-api-sync.js`
- バックエンドのパス定数: `backend/src/main/kotlin/com/usjchatapp/common/constants/ApiPaths.kt`
- フロントの API パス: `frontend/src/constants/apiPaths.ts` 