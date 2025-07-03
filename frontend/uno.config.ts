import { defineConfig, presetUno, presetAttributify, presetIcons } from 'unocss'
import { presetTypography } from '@unocss/preset-typography'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetIcons({
      collections: {
        carbon: () => import('@iconify-json/carbon/icons.json').then(i => i.default as any),
        fluent: () => import('@iconify-json/fluent-emoji/icons.json').then(i => i.default as any),
      }
    }),
    presetTypography(),
  ],
  theme: {
    colors: {
      // シンプルなブランドカラー
      primary: {
        50: '#f0f9ff',
        100: '#e0f2fe',
        200: '#bae6fd',
        300: '#7dd3fc',
        400: '#38bdf8',
        500: '#0ea5e9', // メインブルー
        600: '#0284c7',
        700: '#0369a1',
        800: '#075985',
        900: '#0c4a6e',
      },
      // ソフトなアクセントカラー
      accent: {
        50: '#fdf2f8',
        100: '#fce7f3',
        200: '#fbcfe8',
        300: '#f9a8d4',
        400: '#f472b6',
        500: '#ec4899', // ソフトピンク
        600: '#db2777',
        700: '#be185d',
        800: '#9d174d',
        900: '#831843',
      },
      // サブカラー
      secondary: {
        50: '#f8fafc',
        100: '#f1f5f9',
        200: '#e2e8f0',
        300: '#cbd5e1',
        400: '#94a3b8',
        500: '#64748b',
        600: '#475569',
        700: '#334155',
        800: '#1e293b',
        900: '#0f172a',
      },
      // 成功色
      success: {
        50: '#f0fdf4',
        100: '#dcfce7',
        200: '#bbf7d0',
        300: '#86efac',
        400: '#4ade80',
        500: '#22c55e',
        600: '#16a34a',
        700: '#15803d',
        800: '#166534',
        900: '#14532d',
      },
      // 警告色
      warning: {
        50: '#fffbeb',
        100: '#fef3c7',
        200: '#fde68a',
        300: '#fcd34d',
        400: '#fbbf24',
        500: '#f59e0b',
        600: '#d97706',
        700: '#b45309',
        800: '#92400e',
        900: '#78350f',
      }
    },
    // 控えめなアニメーション
    animation: {
      'fade-in': 'fade-in 0.3s ease-out',
      'slide-up': 'slide-up 0.4s ease-out',
      'pulse-subtle': 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
    },
    // フォント
    fontFamily: {
      'display': ['Inter', 'system-ui', 'sans-serif'],
      'body': ['Inter', 'system-ui', 'sans-serif'],
      'fun': ['Comic Sans MS', 'cursive', 'system-ui'],
    },
    // ボックスシャドウ
    boxShadow: {
      'magic': '0 4px 14px 0 rgba(217, 70, 239, 0.39)',
      'sunshine': '0 4px 14px 0 rgba(250, 204, 21, 0.39)',
      'adventure': '0 4px 14px 0 rgba(34, 197, 94, 0.39)',
      'glow': '0 0 20px rgba(59, 130, 246, 0.5)',
      'float': '0 8px 25px rgba(0, 0, 0, 0.15)',
    }
  },
  shortcuts: [
    // シンプルなボタンスタイル
    ['btn-primary', 'px-6 py-3 bg-primary-500 text-white rounded-lg font-medium shadow-sm hover:bg-primary-600 hover:shadow-md transition-all duration-200'],
    ['btn-secondary', 'px-6 py-3 bg-white text-gray-700 border border-gray-300 rounded-lg font-medium shadow-sm hover:bg-gray-50 hover:shadow-md transition-all duration-200'],
    ['btn-accent', 'px-6 py-3 bg-accent-500 text-white rounded-lg font-medium shadow-sm hover:bg-accent-600 hover:shadow-md transition-all duration-200'],
    ['btn-success', 'px-6 py-3 bg-success-500 text-white rounded-lg font-medium shadow-sm hover:bg-success-600 hover:shadow-md transition-all duration-200'],
    
    // カードスタイル
    ['card-simple', 'bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow duration-200 overflow-hidden border border-gray-100'],
    ['card-feature', 'bg-white rounded-xl shadow-md border border-gray-100 overflow-hidden'],
    
    // テキストスタイル
    ['text-gradient-primary', 'bg-gradient-to-r from-primary-600 to-primary-500 bg-clip-text text-transparent'],
    ['text-muted', 'text-gray-600'],
    ['text-subtle', 'text-gray-500'],
    
    // 入力フィールド
    ['input-simple', 'px-4 py-3 border border-gray-300 rounded-lg focus:border-primary-500 focus:ring-1 focus:ring-primary-500 focus:outline-none transition-all duration-200'],
    
    // コンテナ
    ['container-app', 'max-w-6xl mx-auto px-4 sm:px-6 lg:px-8'],
    
    // フレックスレイアウト
    ['flex-center', 'flex items-center justify-center'],
    ['flex-between', 'flex items-center justify-between'],
  ],
  rules: [
    // シンプルなアニメーションキーフレーム
    [/^animate-(.+)$/, ([, name]) => {
      const animations = {
        'fade-in': '@keyframes fade-in { 0% { opacity: 0; } 100% { opacity: 1; } }',
        'slide-up': '@keyframes slide-up { 0% { opacity: 0; transform: translateY(10px); } 100% { opacity: 1; transform: translateY(0); } }',
      }
      return animations[name] || ''
    }]
  ]
}) 