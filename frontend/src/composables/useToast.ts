import { ref } from 'vue'

// トーストの型定義
export interface Toast {
  id: string
  type: 'success' | 'error' | 'warning' | 'info'
  title?: string
  message: string
  duration?: number
  persistent?: boolean
  actions?: ToastAction[]
}

export interface ToastAction {
  label: string
  action: () => void
  variant?: 'primary' | 'secondary'
}

// トーストオプション
export interface ToastOptions {
  type: Toast['type']
  title?: string
  message: string
  duration?: number
  persistent?: boolean
  actions?: ToastAction[]
}

// グローバルトースト状態
const toasts = ref<Toast[]>([])
const maxToasts = 5

export function useToast() {
  // トーストの生成
  const generateToastId = (): string => {
    return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

  // トーストの表示
  const show = (options: ToastOptions): string => {
    const id = generateToastId()
    const duration = options.duration ?? (options.type === 'error' ? 7000 : 4000)

    const toast: Toast = {
      id,
      type: options.type,
      title: options.title,
      message: options.message,
      duration,
      persistent: options.persistent ?? false,
      actions: options.actions
    }

    // 新しいトーストを先頭に追加
    toasts.value.unshift(toast)

    // 最大表示数を超えた場合、古いものを削除
    if (toasts.value.length > maxToasts) {
      toasts.value = toasts.value.slice(0, maxToasts)
    }

    // 自動削除（persistentでない場合）
    if (!toast.persistent && duration > 0) {
      setTimeout(() => {
        remove(id)
      }, duration)
    }

    return id
  }

  // 特定タイプのトーストヘルパー
  const success = (message: string, options?: Partial<ToastOptions>): string => {
    return show({
      type: 'success',
      message,
      title: 'Success',
      ...options
    })
  }

  const error = (message: string, options?: Partial<ToastOptions>): string => {
    return show({
      type: 'error',
      message,
      title: 'Error',
      duration: 7000,
      ...options
    })
  }

  const warning = (message: string, options?: Partial<ToastOptions>): string => {
    return show({
      type: 'warning',
      message,
      title: 'Warning',
      duration: 6000,
      ...options
    })
  }

  const info = (message: string, options?: Partial<ToastOptions>): string => {
    return show({
      type: 'info',
      message,
      title: 'Info',
      ...options
    })
  }

  // トーストの削除
  const remove = (id: string): void => {
    const index = toasts.value.findIndex(toast => toast.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  // すべてのトーストをクリア
  const clear = (): void => {
    toasts.value = []
  }

  // 特定タイプのトーストをクリア
  const clearByType = (type: Toast['type']): void => {
    toasts.value = toasts.value.filter(toast => toast.type !== type)
  }

  // トーストの更新
  const update = (id: string, updates: Partial<Toast>): void => {
    const toast = toasts.value.find(t => t.id === id)
    if (toast) {
      Object.assign(toast, updates)
    }
  }

  // トーストの存在確認
  const exists = (id: string): boolean => {
    return toasts.value.some(toast => toast.id === id)
  }

  // 重複チェック（同じメッセージのトーストが既に存在するか）
  const isDuplicate = (message: string): boolean => {
    return toasts.value.some(toast => toast.message === message)
  }

  // 重複を避けてトーストを表示
  const showUnique = (options: ToastOptions): string | null => {
    if (isDuplicate(options.message)) {
      return null
    }
    return show(options)
  }

  return {
    // State
    toasts,
    
    // Methods
    show,
    success,
    error,
    warning,
    info,
    remove,
    clear,
    clearByType,
    update,
    exists,
    isDuplicate,
    showUnique
  }
} 