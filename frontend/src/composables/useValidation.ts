import { ref, computed, reactive, watch, nextTick } from 'vue'

// バリデーションルールの型定義
export interface ValidationRule {
  required?: boolean
  min?: number
  max?: number
  minLength?: number
  maxLength?: number
  pattern?: RegExp
  email?: boolean
  custom?: (value: any) => boolean | string
  message?: string
}

// フィールドの検証状態
export interface FieldValidation {
  value: any
  isDirty: boolean
  isTouched: boolean
  isValid: boolean
  errors: string[]
  rules: ValidationRule[]
}

// フォーム全体の検証状態
export interface FormValidation {
  isValid: boolean
  isDirty: boolean
  isTouched: boolean
  errors: Record<string, string[]>
  fields: Record<string, FieldValidation>
}

export function useValidation() {
  // フォームの検証状態
  const formState = reactive<FormValidation>({
    isValid: true,
    isDirty: false,
    isTouched: false,
    errors: {},
    fields: {}
  })

  // デフォルトエラーメッセージ
  const defaultMessages = {
    required: 'この項目は必須です',
    min: (min: number) => `${min}以上の値を入力してください`,
    max: (max: number) => `${max}以下の値を入力してください`,
    minLength: (min: number) => `${min}文字以上で入力してください`,
    maxLength: (max: number) => `${max}文字以下で入力してください`,
    email: '有効なメールアドレスを入力してください',
    pattern: '入力形式が正しくありません'
  }

  // フィールドの追加/更新
  const addField = (
    name: string,
    initialValue: any = '',
    rules: ValidationRule[] = [],
    validateOnChange = true
  ) => {
    formState.fields[name] = reactive({
      value: initialValue,
      isDirty: false,
      isTouched: false,
      isValid: true,
      errors: [],
      rules
    })

    // リアルタイムバリデーション
    if (validateOnChange) {
      watch(
        () => formState.fields[name]?.value,
        (newValue, oldValue) => {
          if (oldValue !== undefined) {
            formState.fields[name].isDirty = true
            formState.isDirty = true
            validateField(name)
          }
        },
        { immediate: false }
      )
    }

    // 初期検証
    validateField(name)
  }

  // フィールドの削除
  const removeField = (name: string) => {
    delete formState.fields[name]
    delete formState.errors[name]
    updateFormState()
  }

  // 単一フィールドの検証
  const validateField = (name: string): boolean => {
    const field = formState.fields[name]
    if (!field) return true

    const errors: string[] = []
    const value = field.value

    // 各ルールをチェック
    for (const rule of field.rules) {
      // 必須チェック
      if (rule.required && (value === null || value === undefined || value === '')) {
        errors.push(rule.message || defaultMessages.required)
        continue
      }

      // 値が空の場合、必須以外のチェックはスキップ
      if (value === null || value === undefined || value === '') {
        continue
      }

      // 最小値チェック
      if (rule.min !== undefined && Number(value) < rule.min) {
        errors.push(rule.message || defaultMessages.min(rule.min))
      }

      // 最大値チェック
      if (rule.max !== undefined && Number(value) > rule.max) {
        errors.push(rule.message || defaultMessages.max(rule.max))
      }

      // 最小文字数チェック
      if (rule.minLength !== undefined && String(value).length < rule.minLength) {
        errors.push(rule.message || defaultMessages.minLength(rule.minLength))
      }

      // 最大文字数チェック
      if (rule.maxLength !== undefined && String(value).length > rule.maxLength) {
        errors.push(rule.message || defaultMessages.maxLength(rule.maxLength))
      }

      // メールアドレスチェック
      if (rule.email) {
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailPattern.test(String(value))) {
          errors.push(rule.message || defaultMessages.email)
        }
      }

      // パターンチェック
      if (rule.pattern && !rule.pattern.test(String(value))) {
        errors.push(rule.message || defaultMessages.pattern)
      }

      // カスタムバリデーション
      if (rule.custom) {
        const result = rule.custom(value)
        if (result !== true) {
          errors.push(typeof result === 'string' ? result : (rule.message || '入力値が無効です'))
        }
      }
    }

    // フィールド状態を更新
    field.errors = errors
    field.isValid = errors.length === 0
    formState.errors[name] = errors

    updateFormState()
    return field.isValid
  }

  // 全フィールドの検証
  const validateForm = (): boolean => {
    let isValid = true
    
    for (const fieldName in formState.fields) {
      const fieldValid = validateField(fieldName)
      if (!fieldValid) {
        isValid = false
      }
    }

    return isValid
  }

  // フォーム状態の更新
  const updateFormState = () => {
    const fields = Object.values(formState.fields)
    
    formState.isValid = fields.every(field => field.isValid)
    formState.isDirty = fields.some(field => field.isDirty)
    formState.isTouched = fields.some(field => field.isTouched)
  }

  // フィールドのタッチ状態を設定
  const touchField = (name: string) => {
    if (formState.fields[name]) {
      formState.fields[name].isTouched = true
      formState.isTouched = true
    }
  }

  // フィールドの値を設定
  const setFieldValue = (name: string, value: any) => {
    if (formState.fields[name]) {
      formState.fields[name].value = value
      formState.fields[name].isDirty = true
      formState.isDirty = true
    }
  }

  // フィールドのエラーを設定
  const setFieldError = (name: string, errors: string[]) => {
    if (formState.fields[name]) {
      formState.fields[name].errors = errors
      formState.fields[name].isValid = errors.length === 0
      formState.errors[name] = errors
      updateFormState()
    }
  }

  // フォームのリセット
  const resetForm = () => {
    for (const fieldName in formState.fields) {
      const field = formState.fields[fieldName]
      field.isDirty = false
      field.isTouched = false
      field.errors = []
      field.isValid = true
    }
    
    formState.isDirty = false
    formState.isTouched = false
    formState.isValid = true
    formState.errors = {}
  }

  // フィールドのリセット
  const resetField = (name: string) => {
    const field = formState.fields[name]
    if (field) {
      field.isDirty = false
      field.isTouched = false
      field.errors = []
      field.isValid = true
      delete formState.errors[name]
      updateFormState()
    }
  }

  // フォームデータの取得
  const getFormData = () => {
    const data: Record<string, any> = {}
    for (const fieldName in formState.fields) {
      data[fieldName] = formState.fields[fieldName].value
    }
    return data
  }

  // フォームエラーの取得
  const getFirstError = (name: string): string | null => {
    const errors = formState.errors[name]
    return errors && errors.length > 0 ? errors[0] : null
  }

  // すべてのエラーを取得
  const getAllErrors = (): string[] => {
    const allErrors: string[] = []
    for (const fieldName in formState.errors) {
      allErrors.push(...formState.errors[fieldName])
    }
    return allErrors
  }

  // よく使用されるバリデーションルール
  const rules = {
    required: (message?: string): ValidationRule => ({
      required: true,
      message
    }),
    
    email: (message?: string): ValidationRule => ({
      email: true,
      message
    }),
    
    minLength: (min: number, message?: string): ValidationRule => ({
      minLength: min,
      message
    }),
    
    maxLength: (max: number, message?: string): ValidationRule => ({
      maxLength: max,
      message
    }),
    
    pattern: (pattern: RegExp, message?: string): ValidationRule => ({
      pattern,
      message
    }),
    
    custom: (validator: (value: any) => boolean | string, message?: string): ValidationRule => ({
      custom: validator,
      message
    }),

    // USJ固有のバリデーション
    displayName: (): ValidationRule[] => [
      { required: true, message: '表示名は必須です' },
      { minLength: 2, message: '表示名は2文字以上で入力してください' },
      { maxLength: 20, message: '表示名は20文字以下で入力してください' }
    ],

    age: (): ValidationRule[] => [
      { required: true, message: '年齢は必須です' },
      { min: 13, message: '13歳以上である必要があります' },
      { max: 100, message: '有効な年齢を入力してください' }
    ],

    password: (): ValidationRule[] => [
      { required: true, message: 'パスワードは必須です' },
      { minLength: 6, message: 'パスワードは6文字以上で入力してください' },
      { maxLength: 20, message: 'パスワードは20文字以下で入力してください' }
    ]
  }

  return {
    // State
    formState,
    
    // Methods
    addField,
    removeField,
    validateField,
    validateForm,
    touchField,
    setFieldValue,
    setFieldError,
    resetForm,
    resetField,
    getFormData,
    getFirstError,
    getAllErrors,
    
    // Rules
    rules,
    
    // Computed
    isValid: computed(() => formState.isValid),
    isDirty: computed(() => formState.isDirty),
    isTouched: computed(() => formState.isTouched),
    errors: computed(() => formState.errors)
  }
} 