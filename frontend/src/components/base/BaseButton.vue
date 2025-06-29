<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="loading-spinner">⏳</span>
    <slot />
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline-primary'
  size?: 'sm' | 'md' | 'lg'
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  loading?: boolean
  block?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  disabled: false,
  loading: false,
  block: false
})

defineEmits<{
  click: [event: MouseEvent]
}>()

const buttonClasses = computed(() => [
  'btn',
  `btn-${props.variant}`,
  `btn-${props.size}`,
  {
    'btn-block': props.block,
    'btn-loading': props.loading
  }
])
</script>

<style scoped>
.btn {
  display: inline-block;
  padding: var(--space-3) var(--space-6);
  border: none;
  border-radius: var(--radius-base);
  cursor: pointer;
  text-decoration: none;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: var(--transition-base);
  text-align: center;
  min-width: fit-content;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: var(--color-primary-500);
  color: var(--color-white);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-600);
}

.btn-secondary {
  background-color: var(--color-secondary-100);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-default);
}

.btn-secondary:hover:not(:disabled) {
  background-color: var(--color-secondary-200);
}

.btn-danger {
  background-color: var(--color-error-500);
  color: var(--color-white);
}

.btn-danger:hover:not(:disabled) {
  background-color: var(--color-error-600);
}

.btn-outline-primary {
  background-color: transparent;
  color: var(--color-primary-500);
  border: 1px solid var(--color-primary-500);
}

.btn-outline-primary:hover:not(:disabled) {
  background-color: var(--color-primary-500);
  color: var(--color-white);
}

.btn-sm {
  padding: var(--space-2) var(--space-4);
  font-size: var(--font-size-sm);
}

.btn-lg {
  padding: var(--space-4) var(--space-8);
  font-size: var(--font-size-lg);
}

.btn-block {
  width: 100%;
}

.btn-loading {
  position: relative;
}

.loading-spinner {
  margin-right: var(--space-2);
}
</style> 