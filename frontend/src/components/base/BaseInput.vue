<template>
  <div class="form-group">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="required-indicator">*</span>
    </label>
    
    <div class="input-wrapper">
      <input
        :id="inputId"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :required="required"
        :disabled="disabled"
        :readonly="readonly"
        :min="min"
        :max="max"
        :step="step"
        :class="inputClasses"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      <slot name="append" />
    </div>
    
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-if="hint && !error" class="hint-message">
      {{ hint }}
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
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  required: false,
  disabled: false,
  readonly: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const inputId = ref(`input-${Math.random().toString(36).substr(2, 9)}`)

const inputClasses = computed(() => [
  'form-input',
  {
    'form-input-error': props.error,
    'form-input-disabled': props.disabled
  }
])

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = props.type === 'number' ? 
    (target.value === '' ? '' : Number(target.value)) : 
    target.value
  emit('update:modelValue', value)
}

const handleBlur = (event: FocusEvent) => {
  emit('blur', event)
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}
</script>

<style scoped>
.form-group {
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  margin-bottom: var(--space-2);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
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

.form-input-disabled {
  background-color: var(--color-secondary-50);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

.error-message {
  color: var(--color-error-500);
  font-size: var(--font-size-sm);
  margin-top: var(--space-1);
}

.hint-message {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  margin-top: var(--space-1);
}
</style> 