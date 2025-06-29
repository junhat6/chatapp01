<template>
  <div class="form-group">
    <label v-if="label" :for="selectId" class="form-label">
      {{ label }}
      <span v-if="required" class="required-indicator">*</span>
    </label>
    
    <select
      :id="selectId"
      :value="modelValue"
      :required="required"
      :disabled="disabled"
      :class="selectClasses"
      @change="handleChange"
      @blur="handleBlur"
      @focus="handleFocus"
    >
      <option v-if="placeholder" value="" disabled>
        {{ placeholder }}
      </option>
      <option
        v-for="option in options"
        :key="getOptionValue(option)"
        :value="getOptionValue(option)"
        :disabled="getOptionDisabled(option)"
      >
        {{ getOptionLabel(option) }}
      </option>
      <slot />
    </select>
    
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

interface Option {
  label: string
  value: string | number
  disabled?: boolean
}

interface Props {
  modelValue?: string | number
  label?: string
  placeholder?: string
  options?: Option[] | string[]
  required?: boolean
  disabled?: boolean
  error?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  options: () => [],
  required: false,
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()

const selectId = ref(`select-${Math.random().toString(36).substr(2, 9)}`)

const selectClasses = computed(() => [
  'form-select',
  {
    'form-select-error': props.error,
    'form-select-disabled': props.disabled
  }
])

const getOptionValue = (option: Option | string): string | number => {
  return typeof option === 'string' ? option : option.value
}

const getOptionLabel = (option: Option | string): string => {
  return typeof option === 'string' ? option : option.label
}

const getOptionDisabled = (option: Option | string): boolean => {
  return typeof option === 'string' ? false : option.disabled || false
}

const handleChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  emit('update:modelValue', target.value)
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

.form-select {
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border-default);
  border-radius: var(--radius-base);
  font-size: var(--font-size-base);
  background-color: var(--color-white);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: var(--transition-base);
}

.form-select:focus {
  outline: none;
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.form-select-error {
  border-color: var(--color-error-500);
}

.form-select-error:focus {
  border-color: var(--color-error-500);
  box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.1);
}

.form-select-disabled {
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