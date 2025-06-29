<template>
  <div class="form-group" :class="{ 'has-error': hasError, 'is-focused': isFocused }">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="required-indicator">*</span>
    </label>
    
    <div class="input-wrapper">
      <input
        :id="inputId"
        :type="currentType"
        :value="modelValue"
        :placeholder="placeholder"
        :required="required"
        :disabled="disabled"
        :readonly="readonly"
        :min="min"
        :max="max"
        :step="step"
        :maxlength="maxLength"
        :class="inputClasses"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      
      <!-- バリデーションアイコンと機能アイコン -->
      <div v-if="showValidationIcon || showPasswordToggle || $slots.append" class="input-icons">
        <!-- バリデーションアイコン -->
        <div v-if="showValidationIcon" class="validation-icon">
          <span v-if="isValid && isTouched" class="success-icon">✓</span>
          <span v-if="hasError && isTouched" class="error-icon">⚠</span>
        </div>
        
        <!-- パスワード表示切り替えボタン -->
        <button
          v-if="showPasswordToggle"
          type="button"
          class="password-toggle"
          @click="togglePasswordVisibility"
          :aria-label="isPasswordVisible ? 'パスワードを隠す' : 'パスワードを表示'"
        >
          {{ isPasswordVisible ? '🙈' : '👁️' }}
        </button>
        
        <slot name="append" />
      </div>
    </div>
    
    <!-- エラーメッセージとヒント -->
    <div v-if="hasError || hint || showCharacterCount" class="input-message">
      <div v-if="hasError" class="error-message">
        {{ displayError }}
      </div>
      <div v-else-if="hint" class="hint-message">
        {{ hint }}
      </div>
      
      <!-- 文字数カウンター -->
      <div v-if="showCharacterCount" class="character-count" :class="{ 'over-limit': isOverCharacterLimit }">
        {{ characterCount }}{{ maxLength ? `/${maxLength}` : '' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Props {
  modelValue?: string | number
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url'
  label?: string
  placeholder?: string
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  error?: string
  hint?: string
  min?: number | string
  max?: number | string
  step?: number | string
  maxLength?: number
  showValidationIcon?: boolean
  showCharacterCount?: boolean
  validationErrors?: string[]
  isValid?: boolean
  isTouched?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  required: false,
  disabled: false,
  readonly: false,
  showValidationIcon: false,
  showCharacterCount: false,
  isValid: true,
  isTouched: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const inputId = ref(`input-${Math.random().toString(36).substr(2, 9)}`)
const isFocused = ref(false)
const isPasswordVisible = ref(false)

// パスワード表示切り替え
const showPasswordToggle = computed(() => props.type === 'password')
const currentType = computed(() => {
  if (props.type === 'password' && isPasswordVisible.value) {
    return 'text'
  }
  return props.type
})

// エラー状態の計算
const hasError = computed(() => {
  return !!(props.error || (props.validationErrors && props.validationErrors.length > 0))
})

const displayError = computed(() => {
  if (props.error) return props.error
  if (props.validationErrors && props.validationErrors.length > 0) {
    return props.validationErrors[0]
  }
  return ''
})

// 文字数カウント
const characterCount = computed(() => {
  return String(props.modelValue || '').length
})

const isOverCharacterLimit = computed(() => {
  return props.maxLength ? characterCount.value > props.maxLength : false
})

const inputClasses = computed(() => [
  'form-input',
  {
    'form-input-error': hasError.value,
    'form-input-disabled': props.disabled,
    'form-input-valid': props.isValid && props.isTouched && !hasError.value
  }
])

const togglePasswordVisibility = () => {
  isPasswordVisible.value = !isPasswordVisible.value
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = props.type === 'number' ? 
    (target.value === '' ? '' : Number(target.value)) : 
    target.value
  emit('update:modelValue', value)
}

const handleBlur = (event: FocusEvent) => {
  isFocused.value = false
  emit('blur', event)
}

const handleFocus = (event: FocusEvent) => {
  isFocused.value = true
  emit('focus', event)
}
</script>

<style scoped>
.form-group {
  margin-bottom: var(--space-4);
}

.form-group.has-error .form-label {
  color: var(--color-error-500);
}

.form-group.is-focused .form-label {
  color: var(--color-primary-500);
}

.form-label {
  display: block;
  margin-bottom: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  transition: var(--transition-fast);
}

.required-indicator {
  color: var(--color-error-500);
  margin-left: var(--space-1);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: var(--space-3);
  padding-right: var(--space-12); /* アイコン用のスペース */
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-base);
  font-size: var(--font-size-base);
  transition: var(--transition-base);
  background-color: var(--color-white);
  color: var(--color-text-primary);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.form-input-error {
  border-color: var(--color-error-500);
}

.form-input-error:focus {
  border-color: var(--color-error-500);
  box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.1);
}

.form-input-valid {
  border-color: var(--color-success-500);
}

.form-input-disabled {
  background-color: var(--color-secondary-50);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

.input-icons {
  position: absolute;
  right: var(--space-2);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  height: 100%;
  pointer-events: none;
}

.input-icons > * {
  pointer-events: auto;
}

.validation-icon {
  display: flex;
  align-items: center;
  font-size: var(--font-size-sm);
}

.success-icon {
  color: var(--color-success-500);
  font-weight: var(--font-weight-bold);
}

.error-icon {
  color: var(--color-error-500);
  font-weight: var(--font-weight-bold);
}

.password-toggle {
  background: none;
  border: none;
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: var(--transition-fast);
  font-size: var(--font-size-sm);
}

.password-toggle:hover {
  background-color: var(--color-secondary-100);
}

.input-message {
  margin-top: var(--space-1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-2);
}

.error-message {
  color: var(--color-error-500);
  font-size: var(--font-size-sm);
  flex: 1;
}

.hint-message {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  flex: 1;
}

.character-count {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

.character-count.over-limit {
  color: var(--color-error-500);
}

/* アクセシビリティの向上 */
@media (prefers-reduced-motion: reduce) {
  .form-input,
  .form-label,
  .password-toggle {
    transition: none;
  }
}

/* フォーカス時のより良い視認性 */
.form-input:focus-visible {
  box-shadow: 0 0 0 3px var(--color-primary-200);
}

.form-input-error:focus-visible {
  box-shadow: 0 0 0 3px var(--color-error-200);
}
</style> 