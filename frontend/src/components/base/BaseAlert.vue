<template>
  <div v-if="show" :class="alertClasses">
    <div class="alert-icon">
      {{ alertIcon }}
    </div>
    <div class="alert-content">
      <div v-if="title" class="alert-title">
        {{ title }}
      </div>
      <div class="alert-message">
        <slot>{{ message }}</slot>
      </div>
    </div>
    <button v-if="closable" @click="handleClose" class="alert-close">
      ×
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'error' | 'success' | 'warning' | 'info'
  message?: string
  title?: string
  closable?: boolean
  show?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  closable: false,
  show: true
})

const emit = defineEmits<{
  close: []
}>()

const alertClasses = computed(() => [
  'alert',
  `alert-${props.type}`
])

const alertIcon = computed(() => {
  const icons = {
    error: '❌',
    success: '✅',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[props.type]
})

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.alert {
  display: flex;
  align-items: flex-start;
  padding: var(--space-4);
  border-radius: var(--radius-base);
  margin-bottom: var(--space-4);
  border-left: 4px solid;
}

.alert-error {
  background-color: var(--color-error-50);
  border-left-color: var(--color-error-500);
  color: var(--color-error-600);
}

.alert-success {
  background-color: var(--color-success-50);
  border-left-color: var(--color-success-500);
  color: var(--color-success-600);
}

.alert-warning {
  background-color: var(--color-warning-50);
  border-left-color: var(--color-warning-500);
  color: var(--color-warning-600);
}

.alert-info {
  background-color: var(--color-primary-50);
  border-left-color: var(--color-primary-500);
  color: var(--color-primary-700);
}

.alert-icon {
  margin-right: var(--space-3);
  font-size: var(--font-size-xl);
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-1);
}

.alert-message {
  line-height: var(--line-height-normal);
}

.alert-close {
  background: none;
  border: none;
  font-size: var(--font-size-2xl);
  cursor: pointer;
  padding: 0;
  margin-left: var(--space-3);
  opacity: 0.7;
  transition: var(--transition-fast);
  flex-shrink: 0;
}

.alert-close:hover {
  opacity: 1;
}
</style> 