# APIパス管理システム

フロントエンドとバックエンドのAPIパスのずれを防ぐための統一管理システムです。

## 🎯 問題の解決

### 従来の問題
- フロントエンドとバックエンドでAPIパスが食い違う
- パス変更時の同期漏れ
- 手動管理によるヒューマンエラー

### 解決方法
1. **定数による一元管理**
2. **自動同期チェック**
3. **統一的なネーミング規則**

## 📁 ファイル構成

```
backend/src/main/kotlin/com/usjchatapp/common/constants/
└── ApiPaths.kt                    # バックエンド定数

frontend/src/constants/
└── apiPaths.ts                    # フロントエンド定数

scripts/
└── check-api-sync.js              # 同期チェックスクリプト

docs/
└── API_PATH_MANAGEMENT.md         # このドキュメント
```

## 🔧 使用方法

### バックエンド（Kotlin）

```kotlin
import com.usjchatapp.common.constants.ApiPaths

@RestController
@RequestMapping(ApiPaths.Auth.BASE)
class AuthController {
    
    @PostMapping(ApiPaths.Auth.SIGNUP)
    fun signUp() { ... }
    
    @GetMapping(ApiPaths.Auth.ME)
    fun getCurrentUser() { ... }
}
```

### フロントエンド（TypeScript）

```typescript
import { AUTH_PATHS, buildApiUrl } from '@/constants/apiPaths'

// 基本的な使用
api.post(AUTH_PATHS.BASE + AUTH_PATHS.SIGNUP, userData)

// パラメータ付きパス
api.get(MATCHING_ROOM_PATHS.GET_BY_REQUEST(requestId))
```

## 🛠️ 開発ワークフロー

### 1. パス追加・変更時

1. **バックエンド**で `ApiPaths.kt` を更新
2. **フロントエンド**で `apiPaths.ts` を更新  
3. 同期チェックを実行
4. 必要に応じてAPIサービスファイルを更新

### 2. 同期チェック実行

```bash
# 手動実行
node scripts/check-api-sync.js

# 開発時の自動チェック（推奨）
npm run check-api-sync

# CI/CDパイプラインに組み込み
```

### 3. 出力例

```
🔍 APIパス同期チェックを開始します...

📂 Kotlin定義: 16個のパス
📂 TypeScript定義: 30個のパス

✅ APIパスの同期に問題はありません！
```

## 📋 ネーミング規則

### バックエンド（Kotlin）
```kotlin
object ApiPaths {
    object Auth {
        const val BASE = "$API_BASE/auth"
        const val SIGNUP = "/signup"
    }
}
```

### フロントエンド（TypeScript）
```typescript
export const AUTH_PATHS = {
  BASE: `${API_BASE}/auth`,
  SIGNUP: '/signup',
} as const;
```

### 対応表
| Kotlin | TypeScript |
|--------|------------|
| `Auth` | `AUTH_PATHS` |
| `Profile` | `PROFILE_PATHS` |
| `MatchingRequest` | `MATCHING_REQUEST_PATHS` |
| `MatchingRoom` | `MATCHING_ROOM_PATHS` |

## 🔍 同期チェック詳細

### チェック内容
1. **パス値の比較** - 文字列補間の違いを考慮した正規化比較
2. **存在チェック** - 対応するパスが存在するか
3. **情報提供** - TypeScript独自のパス（関数形式など）

### 正規化処理
- Kotlin: `$API_BASE` → TypeScript: `${API_BASE}`
- 実際の値への展開: `/api`

## 🚀 CI/CD統合

### GitHub Actions例
```yaml
name: API Path Sync Check
on: [push, pull_request]

jobs:
  check-api-sync:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Check API Path Sync
        run: node scripts/check-api-sync.js
```

## ⚡ 最適化のヒント

### 1. 開発効率向上
- IDEスニペット/テンプレート活用
- API定数の自動補完設定
- プリコミットフックでの自動チェック

### 2. エラー予防
- 定数未使用の検出（linter設定）
- 型安全性の確保（TypeScript）
- ドキュメント自動生成

### 3. 保守性向上
- バージョニング戦略
- 非互換変更の段階的移行
- レガシーパスの管理

## 📝 FAQ

### Q: 既存のハードコードされたパスをどう移行する？
A: 段階的に定数に置き換え、同期チェックで進捗を確認

### Q: WebSocketパスも管理できる？
A: はい。`WEBSOCKET_PATHS`で統一管理可能

### Q: バージョニングAPIはどう管理する？
A: `ApiPaths.V1.Auth.BASE`のようなネスト構造で対応

## 🔄 メンテナンス

- **月次**: 同期チェック結果のレビュー
- **リリース前**: 全パスの手動確認
- **リファクタリング時**: 定数抽出の機会として活用

---

これでAPIパスのずれ問題から解放され、安全で効率的な開発が可能になります！ 