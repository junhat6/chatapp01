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
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border-left: 4px solid;
}

.alert-error {
  background-color: #ffebee;
  border-left-color: #d32f2f;
  color: #c62828;
}

.alert-success {
  background-color: #e8f5e8;
  border-left-color: #2e7d32;
  color: #1b5e20;
}

.alert-warning {
  background-color: #fff3e0;
  border-left-color: #f57c00;
  color: #ef6c00;
}

.alert-info {
  background-color: #e3f2fd;
  border-left-color: #1976d2;
  color: #0d47a1;
}

.alert-icon {
  margin-right: 0.75rem;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.alert-message {
  line-height: 1.5;
}

.alert-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  margin-left: 0.75rem;
  opacity: 0.7;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.alert-close:hover {
  opacity: 1;
}
</style> 