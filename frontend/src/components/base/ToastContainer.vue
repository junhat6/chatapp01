<template>
  <teleport to="body">
    <div class="toast-container">
      <transition-group name="toast" tag="div" class="toast-list">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="toastClasses(toast)"
          class="toast"
        >
          <div class="toast-icon">
            {{ getToastIcon(toast.type) }}
          </div>
          
          <div class="toast-content">
            <div v-if="toast.title" class="toast-title">
              {{ toast.title }}
            </div>
            <div class="toast-message">
              {{ toast.message }}
            </div>
            
            <div v-if="toast.actions?.length" class="toast-actions">
              <button
                v-for="action in toast.actions"
                :key="action.label"
                :class="actionClasses(action)"
                class="toast-action"
                @click="action.action"
              >
                {{ action.label }}
              </button>
            </div>
          </div>
          
          <button
            class="toast-close"
            @click="remove(toast.id)"
            aria-label="閉じる"
          >
            ×
          </button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup lang="ts">
import { useToast } from '@/composables/useToast'
import type { Toast, ToastAction } from '@/composables/useToast'

const { toasts, remove } = useToast()

// トーストのクラス計算
const toastClasses = (toast: Toast) => [
  'toast',
  `toast-${toast.type}`,
  {
    'toast-persistent': toast.persistent
  }
]

// アクションボタンのクラス計算
const actionClasses = (action: ToastAction) => [
  'toast-action',
  `toast-action-${action.variant || 'secondary'}`
]

// トーストアイコンの取得
const getToastIcon = (type: Toast['type']): string => {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[type]
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: var(--space-4);
  right: var(--space-4);
  z-index: var(--z-toast, 1070);
  max-width: 400px;
  width: 100%;
  pointer-events: none;
}

.toast-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  border-left: 4px solid;
  pointer-events: auto;
  min-width: 300px;
  position: relative;
}

.toast-success {
  border-left-color: var(--color-success-500);
}

.toast-error {
  border-left-color: var(--color-error-500);
}

.toast-warning {
  border-left-color: var(--color-warning-500);
}

.toast-info {
  border-left-color: var(--color-primary-500);
}

.toast-icon {
  font-size: var(--font-size-lg);
  flex-shrink: 0;
  margin-top: var(--space-0_5);
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-1);
  font-size: var(--font-size-sm);
}

.toast-message {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  word-wrap: break-word;
}

.toast-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.toast-action {
  padding: var(--space-1) var(--space-3);
  border: none;
  border-radius: var(--radius-base);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: var(--transition-fast);
}

.toast-action-primary {
  background-color: var(--color-primary-500);
  color: var(--color-white);
}

.toast-action-primary:hover {
  background-color: var(--color-primary-600);
}

.toast-action-secondary {
  background-color: var(--color-secondary-100);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border-default);
}

.toast-action-secondary:hover {
  background-color: var(--color-secondary-200);
}

.toast-close {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  background: none;
  border: none;
  font-size: var(--font-size-lg);
  color: var(--color-text-muted);
  cursor: pointer;
  padding: var(--space-1);
  line-height: 1;
  transition: var(--transition-fast);
  border-radius: var(--radius-base);
}

.toast-close:hover {
  color: var(--color-text-primary);
  background-color: var(--color-secondary-100);
}

/* Persistent toast styling */
.toast-persistent {
  border-left-width: 6px;
}

.toast-persistent .toast-close::after {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 6px;
  height: 6px;
  background-color: var(--color-primary-500);
  border-radius: var(--radius-full);
}

/* Transition animations */
.toast-enter-active {
  transition: all var(--transition-base);
}

.toast-leave-active {
  transition: all var(--transition-base);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-move {
  transition: transform var(--transition-base);
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .toast-container {
    top: var(--space-2);
    right: var(--space-2);
    left: var(--space-2);
    max-width: none;
  }
  
  .toast {
    min-width: auto;
    padding: var(--space-3);
  }
  
  .toast-message {
    font-size: var(--font-size-sm);
  }
  
  .toast-actions {
    flex-direction: column;
  }
  
  .toast-action {
    width: 100%;
    justify-content: center;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .toast-enter-active,
  .toast-leave-active,
  .toast-move {
    transition: none;
  }
}

/* Dark mode support (if needed in future) */
@media (prefers-color-scheme: dark) {
  .toast {
    background: var(--color-gray-800, #1f2937);
    color: var(--color-gray-100, #f3f4f6);
  }
  
  .toast-title {
    color: var(--color-gray-100, #f3f4f6);
  }
  
  .toast-message {
    color: var(--color-gray-300, #d1d5db);
  }
}
</style> 