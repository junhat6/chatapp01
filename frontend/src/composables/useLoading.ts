import { ref, computed } from 'vue'

// ローディングタイプの定義
export enum LoadingType {
  GLOBAL = 'global',
  COMPONENT = 'component',
  SKELETON = 'skeleton',
  BUTTON = 'button',
  OVERLAY = 'overlay'
}

// ローディング状態の型定義
export interface LoadingState {
  id: string
  type: LoadingType
  message?: string
  progress?: number
  cancelable?: boolean
  onCancel?: () => void
}

// グローバルローディング状態
const globalLoadings = ref<LoadingState[]>([])
const isAnyLoading = computed(() => globalLoadings.value.length > 0)
const isGlobalLoading = computed(() => 
  globalLoadings.value.some(loading => loading.type === LoadingType.GLOBAL)
)

export function useLoading() {
  // ローディングIDの生成
  const generateLoadingId = (): string => {
    return `loading-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  }

  // ローディング開始
  const startLoading = (options: {
    type?: LoadingType
    message?: string
    progress?: number
    cancelable?: boolean
    onCancel?: () => void
    global?: boolean
  } = {}): string => {
    const {
      type = LoadingType.COMPONENT,
      message,
      progress,
      cancelable = false,
      onCancel,
      global = false
    } = options

    const id = generateLoadingId()
    const loadingState: LoadingState = {
      id,
      type: global ? LoadingType.GLOBAL : type,
      message,
      progress,
      cancelable,
      onCancel
    }

    if (global || type === LoadingType.GLOBAL) {
      globalLoadings.value.push(loadingState)
    }

    return id
  }

  // ローディング停止
  const stopLoading = (id: string): void => {
    const index = globalLoadings.value.findIndex(loading => loading.id === id)
    if (index > -1) {
      globalLoadings.value.splice(index, 1)
    }
  }

  // すべてのローディングを停止
  const stopAllLoading = (): void => {
    globalLoadings.value = []
  }

  // 特定タイプのローディングを停止
  const stopLoadingByType = (type: LoadingType): void => {
    globalLoadings.value = globalLoadings.value.filter(loading => loading.type !== type)
  }

  // ローディング進捗の更新
  const updateProgress = (id: string, progress: number): void => {
    const loading = globalLoadings.value.find(l => l.id === id)
    if (loading) {
      loading.progress = progress
    }
  }

  // ローディングメッセージの更新
  const updateMessage = (id: string, message: string): void => {
    const loading = globalLoadings.value.find(l => l.id === id)
    if (loading) {
      loading.message = message
    }
  }

  // 非同期処理のローディングラッパー
  const withLoading = async <T>(
    asyncFn: () => Promise<T>,
    options: {
      type?: LoadingType
      message?: string
      global?: boolean
      errorHandler?: (error: any) => void
    } = {}
  ): Promise<T> => {
    const loadingId = startLoading(options)
    
    try {
      const result = await asyncFn()
      return result
    } catch (error) {
      if (options.errorHandler) {
        options.errorHandler(error)
      } else {
        throw error
      }
      throw error
    } finally {
      stopLoading(loadingId)
    }
  }

  // ローディング状態のチェック
  const isLoading = (id?: string): boolean => {
    if (id) {
      return globalLoadings.value.some(loading => loading.id === id)
    }
    return isAnyLoading.value
  }

  // 特定タイプのローディング状態チェック
  const isLoadingByType = (type: LoadingType): boolean => {
    return globalLoadings.value.some(loading => loading.type === type)
  }

  // ローディングキャンセル
  const cancelLoading = (id: string): void => {
    const loading = globalLoadings.value.find(l => l.id === id)
    if (loading && loading.cancelable && loading.onCancel) {
      loading.onCancel()
      stopLoading(id)
    }
  }

  // ローディングの存在確認
  const exists = (id: string): boolean => {
    return globalLoadings.value.some(loading => loading.id === id)
  }

  // 現在のローディング数取得
  const getLoadingCount = (type?: LoadingType): number => {
    if (type) {
      return globalLoadings.value.filter(loading => loading.type === type).length
    }
    return globalLoadings.value.length
  }

  // 最新のローディング状態取得
  const getLatestLoading = (type?: LoadingType): LoadingState | null => {
    const loadings = type 
      ? globalLoadings.value.filter(loading => loading.type === type)
      : globalLoadings.value
    
    return loadings.length > 0 ? loadings[loadings.length - 1] : null
  }

  // よく使用されるローディングヘルパー
  const showGlobalLoading = (message?: string): string => {
    return startLoading({
      type: LoadingType.GLOBAL,
      message: message || 'Loading...',
      global: true
    })
  }

  const showSkeletonLoading = (): string => {
    return startLoading({
      type: LoadingType.SKELETON
    })
  }

  const showOverlayLoading = (message?: string, cancelable = false, onCancel?: () => void): string => {
    return startLoading({
      type: LoadingType.OVERLAY,
      message,
      cancelable,
      onCancel
    })
  }

  return {
    // State
    globalLoadings,
    isAnyLoading,
    isGlobalLoading,
    
    // Methods
    startLoading,
    stopLoading,
    stopAllLoading,
    stopLoadingByType,
    updateProgress,
    updateMessage,
    withLoading,
    isLoading,
    isLoadingByType,
    cancelLoading,
    exists,
    getLoadingCount,
    getLatestLoading,
    
    // Helpers
    showGlobalLoading,
    showSkeletonLoading,
    showOverlayLoading
  }
} 