/**
 * 日時関連のユーティリティ関数
 */

/**
 * 指定された日時が現在時刻より過去かどうかを判定
 * @param dateTimeString ISO 8601形式の日時文字列
 * @returns 期限切れの場合true
 */
export const isExpired = (dateTimeString: string): boolean => {
  if (!dateTimeString) return true
  
  try {
    const targetDate = new Date(dateTimeString)
    const now = new Date()
    return targetDate < now
  } catch (error) {
    console.warn('Invalid date format:', dateTimeString)
    return true // 無効な日付は期限切れとして扱う
  }
}

/**
 * 募集の期限切れ状態を取得
 * @param dateTimeString ISO 8601形式の日時文字列
 * @returns 期限切れ状態
 */
export type ExpirationStatus = 'active' | 'expired' | 'old'

export const getExpirationStatus = (dateTimeString: string): ExpirationStatus => {
  if (!dateTimeString) return 'old'
  
  try {
    const targetDate = new Date(dateTimeString)
    const now = new Date()
    const diffHours = (now.getTime() - targetDate.getTime()) / (1000 * 60 * 60)
    
    if (diffHours <= 0) return 'active'      // 未来の時刻
    if (diffHours <= 24) return 'expired'    // 過去24時間以内
    return 'old'                             // 24時間以上経過
  } catch (error) {
    console.warn('Invalid date format:', dateTimeString)
    return 'old'
  }
}

/**
 * 期限切れまでの残り時間を取得
 * @param dateTimeString ISO 8601形式の日時文字列
 * @returns 残り時間の文字列表現
 */
export const getTimeUntilExpiration = (dateTimeString: string): string => {
  if (!dateTimeString) return '期限切れ'
  
  try {
    const targetDate = new Date(dateTimeString)
    const now = new Date()
    const diffMs = targetDate.getTime() - now.getTime()
    
    if (diffMs <= 0) return '期限切れ'
    
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
    
    if (diffHours > 24) {
      const diffDays = Math.floor(diffHours / 24)
      return `${diffDays}日後`
    } else if (diffHours > 0) {
      return `${diffHours}時間後`
    } else {
      return `${diffMinutes}分後`
    }
  } catch (error) {
    console.warn('Invalid date format:', dateTimeString)
    return '期限切れ'
  }
}

/**
 * 日時の表示用フォーマット（既存のformatDateTime関数と同様の機能）
 * @param dateTimeString ISO 8601形式の日時文字列
 * @returns フォーマットされた日時文字列
 */
export const formatDateTime = (dateTimeString: string): string => {
  if (!dateTimeString) return ''
  
  try {
    const date = new Date(dateTimeString)
    const month = date.getMonth() + 1
    const day = date.getDate()
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `${month}/${day} ${hours}:${minutes}`
  } catch (error) {
    console.warn('Invalid date format:', dateTimeString)
    return '無効な日時'
  }
} 