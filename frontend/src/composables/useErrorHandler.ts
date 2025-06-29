import { ref } from 'vue'
import { useToast } from '@/composables/useToast'

// エラーの種類を定義
export enum ErrorType {
  NETWORK = 'network',
  VALIDATION = 'validation',
  AUTHENTICATION = 'authentication',
  AUTHORIZATION = 'authorization',
  SERVER = 'server',
  UNKNOWN = 'unknown'
}

// エラー情報の型定義
export interface AppError {
  type: ErrorType
  message: string
  details?: string[]
  code?: string | number
  timestamp: Date
}

// グローバルエラー状態
const globalErrors = ref<AppError[]>([])
const isErrorModalVisible = ref(false)

export function useErrorHandler() {
  const toast = useToast()

  // エラータイプの判定
  const determineErrorType = (error: any): ErrorType => {
    if (!error.response) {
      return ErrorType.NETWORK
    }

    const status = error.response.status
    switch (status) {
      case 400:
        return ErrorType.VALIDATION
      case 401:
        return ErrorType.AUTHENTICATION
      case 403:
        return ErrorType.AUTHORIZATION
      case 422:
        return ErrorType.VALIDATION
      case 500:
      case 502:
      case 503:
        return ErrorType.SERVER
      default:
        return ErrorType.UNKNOWN
    }
  }

  // ユーザー向けメッセージの生成
  const generateUserMessage = (errorType: ErrorType, originalMessage?: string): string => {
    const messages = {
      [ErrorType.NETWORK]: 'インターネット接続を確認してください',
      [ErrorType.VALIDATION]: '入力内容に問題があります',
      [ErrorType.AUTHENTICATION]: 'ログインが必要です',
      [ErrorType.AUTHORIZATION]: 'この操作の権限がありません',
      [ErrorType.SERVER]: 'サーバーエラーが発生しました。しばらく待ってから再試行してください',
      [ErrorType.UNKNOWN]: '予期しないエラーが発生しました'
    }

    return originalMessage || messages[errorType]
  }

  // エラーの詳細情報を抽出
  const extractErrorDetails = (error: any): string[] => {
    const details: string[] = []
    
    if (error.response?.data?.errors) {
      details.push(...error.response.data.errors)
    }
    
    if (error.response?.data?.message && !details.includes(error.response.data.message)) {
      details.push(error.response.data.message)
    }

    return details
  }

  // エラーハンドリングのメイン関数
  const handleError = (error: any, options: {
    showToast?: boolean
    logToConsole?: boolean
    addToGlobalErrors?: boolean
    customMessage?: string
  } = {}) => {
    const {
      showToast = true,
      logToConsole = true,
      addToGlobalErrors = false,
      customMessage
    } = options

    const errorType = determineErrorType(error)
    const userMessage = customMessage || generateUserMessage(errorType, error.response?.data?.message)
    const details = extractErrorDetails(error)

    const appError: AppError = {
      type: errorType,
      message: userMessage,
      details,
      code: error.response?.status,
      timestamp: new Date()
    }

    // コンソールログ
    if (logToConsole) {
      console.error('[Error Handler]', {
        type: errorType,
        error,
        appError
      })
    }

    // トーストでエラー表示
    if (showToast) {
      const toastType = errorType === ErrorType.VALIDATION ? 'warning' : 'error'
      toast.show({
        type: toastType,
        title: getErrorTitle(errorType),
        message: userMessage,
        duration: errorType === ErrorType.NETWORK ? 0 : 5000 // ネットワークエラーは手動で閉じる
      })
    }

    // グローバルエラーリストに追加
    if (addToGlobalErrors) {
      globalErrors.value.unshift(appError)
      // 最大20件まで保持
      if (globalErrors.value.length > 20) {
        globalErrors.value = globalErrors.value.slice(0, 20)
      }
    }

    return appError
  }

  // エラータイトルの取得
  const getErrorTitle = (errorType: ErrorType): string => {
    const titles = {
      [ErrorType.NETWORK]: '接続エラー',
      [ErrorType.VALIDATION]: '入力エラー',
      [ErrorType.AUTHENTICATION]: '認証エラー',
      [ErrorType.AUTHORIZATION]: '権限エラー',
      [ErrorType.SERVER]: 'サーバーエラー',
      [ErrorType.UNKNOWN]: 'エラー'
    }
    return titles[errorType]
  }

  // 特定タイプのエラーハンドラー
  const handleValidationError = (error: any, customMessage?: string) => {
    return handleError(error, {
      customMessage,
      showToast: true,
      addToGlobalErrors: false
    })
  }

  const handleNetworkError = (error: any) => {
    return handleError(error, {
      showToast: true,
      addToGlobalErrors: true
    })
  }

  const handleServerError = (error: any) => {
    return handleError(error, {
      showToast: true,
      addToGlobalErrors: true
    })
  }

  // 認証エラー（自動リダイレクト付き）
  const handleAuthError = (error: any) => {
    const appError = handleError(error, {
      showToast: true,
      addToGlobalErrors: false
    })

    // 認証エラーの場合はログインページにリダイレクト
    if (appError.type === ErrorType.AUTHENTICATION) {
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    }

    return appError
  }

  // グローバルエラーリストのクリア
  const clearGlobalErrors = () => {
    globalErrors.value = []
  }

  // 特定エラーの削除
  const removeGlobalError = (index: number) => {
    globalErrors.value.splice(index, 1)
  }

  // エラーモーダルの表示切り替え
  const showErrorModal = () => {
    isErrorModalVisible.value = true
  }

  const hideErrorModal = () => {
    isErrorModalVisible.value = false
  }

  return {
    // State
    globalErrors,
    isErrorModalVisible,
    
    // Methods
    handleError,
    handleValidationError,
    handleNetworkError,
    handleServerError,
    handleAuthError,
    clearGlobalErrors,
    removeGlobalError,
    showErrorModal,
    hideErrorModal,
    
    // Utils
    determineErrorType,
    generateUserMessage,
    getErrorTitle
  }
} 