# USJ Chat デザインシステム

USJ Chat アプリケーション用の統一されたデザインシステムです。

## 📁 ファイル構成

```
frontend/src/styles/
├── design-system.css  # CSS変数とベースシステム
├── utilities.css      # ユーティリティクラス
└── README.md          # このファイル
```

## 🎨 カラーパレット

### Primary Colors (ブルー系)

```css
--color-primary-50   #e3f2fd  /* 最も薄い */
--color-primary-500  #1976d2  /* メインカラー */
--color-primary-600  #1565c0  /* ホバー用 */
--color-primary-700  #0d47a1  /* 濃い */
```

### Status Colors

```css
--color-error-500    #d32f2f   /* エラー */
--color-success-500  #2e7d32   /* 成功 */
--color-warning-500  #f57c00   /* 警告 */
```

### Neutral Colors (グレー系)

```css
--color-gray-800     #1f2937   /* 濃いテキスト */
--color-gray-600     #4b5563   /* 通常テキスト */
--color-gray-500     #6b7280   /* ミューテッドテキスト */
--color-gray-200     #e5e7eb   /* 境界線 */
--color-gray-50      #f9fafb   /* 背景 */
```

## 📏 スペーシング

```css
--space-1   0.25rem   /* 4px */
--space-2   0.5rem    /* 8px */
--space-3   0.75rem   /* 12px */
--space-4   1rem      /* 16px */
--space-6   1.5rem    /* 24px */
--space-8   2rem      /* 32px */
--space-12  3rem      /* 48px */
```

## 🔤 タイポグラフィ

### Font Sizes

```css
--font-size-sm       0.875rem  /* 14px */
--font-size-base     1rem      /* 16px */
--font-size-lg       1.125rem  /* 18px */
--font-size-xl       1.25rem   /* 20px */
--font-size-2xl      1.5rem    /* 24px */
```

### Font Weights

```css
--font-weight-normal    400
--font-weight-medium    500
--font-weight-semibold  600
--font-weight-bold      700
```

## 🏗️ 使用方法

### 1. CSS 変数の使用

コンポーネント内で CSS 変数を直接使用：

```css
.my-component {
  color: var(--color-text-primary);
  background: var(--color-primary-500);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}
```

### 2. ユーティリティクラスの使用

テンプレート内でユーティリティクラスを使用：

```vue
<template>
  <div class="flex items-center justify-between p-4 mb-6">
    <h1 class="text-2xl font-bold text-primary">タイトル</h1>
    <button class="px-4 py-2 bg-primary text-white rounded-lg">ボタン</button>
  </div>
</template>
```

### 3. レスポンシブデザイン

ブレークポイントを使用したレスポンシブクラス：

```vue
<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <div class="p-4 bg-white rounded-lg shadow">カード</div>
  </div>
</template>
```

## 🧩 コンポーネントとの統合

### BaseButton の例

```vue
<style scoped>
.btn {
  padding: var(--space-3) var(--space-6);
  border-radius: var(--radius-base);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-base);
}

.btn-primary {
  background-color: var(--color-primary-500);
  color: var(--color-white);
}

.btn-primary:hover {
  background-color: var(--color-primary-600);
}
</style>
```

## 📱 レスポンシブブレークポイント

```css
--breakpoint-sm:  640px   /* スマートフォン (大) */
--breakpoint-md:  768px   /* タブレット */
--breakpoint-lg:  1024px  /* デスクトップ (小) */
--breakpoint-xl:  1280px  /* デスクトップ (大) */
```

### レスポンシブユーティリティ

```html
<!-- モバイルでは縦並び、タブレット以上で横並び -->
<div class="flex flex-col md:flex-row">
  <div class="w-full md:w-1/2">コンテンツ1</div>
  <div class="w-full md:w-1/2">コンテンツ2</div>
</div>

<!-- モバイルでは非表示、デスクトップで表示 -->
<div class="hidden lg:block">デスクトップ専用コンテンツ</div>
```

## 🎯 ベストプラクティス

### 1. CSS 変数を優先

ハードコードされた値の代わりに CSS 変数を使用：

```css
/* ❌ 悪い例 */
.component {
  padding: 16px;
  color: #1976d2;
}

/* ✅ 良い例 */
.component {
  padding: var(--space-4);
  color: var(--color-primary-500);
}
```

### 2. ユーティリティクラスの活用

簡単なスタイリングはユーティリティクラスを使用：

```vue
<!-- ✅ 良い例 -->
<div
  class="flex items-center justify-between p-4 mb-4 bg-white rounded-lg shadow-md"
>
  <!-- コンテンツ -->
</div>
```

### 3. セマンティックカラーの使用

具体的な色名ではなくセマンティックカラーを使用：

```css
/* ❌ 悪い例 */
.error-text {
  color: var(--color-red-500);
}

/* ✅ 良い例 */
.error-text {
  color: var(--color-error-500);
}
```

### 4. 一貫したスペーシング

デザインシステムのスペーシングスケールを使用：

```css
/* ✅ 一貫したスペーシング */
.container {
  padding: var(--space-4); /* 16px */
  margin-bottom: var(--space-6); /* 24px */
  gap: var(--space-2); /* 8px */
}
```

## 🔧 カスタマイズ

新しい CSS 変数を追加する場合は `design-system.css` に追加：

```css
:root {
  /* 新しいカラー */
  --color-brand-purple: #8b5cf6;

  /* 新しいスペーシング */
  --space-14: 3.5rem; /* 56px */

  /* 新しいシャドウ */
  --shadow-inner: inset 0 2px 4px 0 rgba(0, 0, 0, 0.06);
}
```

## 📖 関連資料

- [Vue.js Style Guide](https://vuejs.org/style-guide/)
- [CSS Custom Properties (MDN)](https://developer.mozilla.org/en-US/docs/Web/CSS/Using_CSS_custom_properties)
- [Tailwind CSS (参考)](https://tailwindcss.com/docs)

---

このデザインシステムにより、USJ Chat アプリケーション全体で一貫したデザインとユーザーエクスペリエンスを提供できます。
