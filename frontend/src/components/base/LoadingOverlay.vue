<template>
  <teleport to="body">
    <transition name="loading-overlay">
      <div
        v-if="isVisible"
        class="loading-overlay"
        :class="overlayClasses"
      >
        <div class="loading-content">
          <!-- メインローディングスピナー -->
          <div class="loading-spinner" :class="spinnerClasses">
            <svg viewBox="0 0 50 50" class="spinner-svg">
              <circle
                cx="25"
                cy="25"
                r="20"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-dasharray="31.416"
                stroke-dashoffset="31.416"
                class="spinner-circle"
              />
            </svg>
          </div>

          <!-- ローディングメッセージ -->
          <div v-if="message" class="loading-message">
            {{ message }}
          </div>

          <!-- 進捗バー -->
          <div v-if="progress !== undefined" class="loading-progress">
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: `${progress}%` }"
              />
            </div>
            <div class="progress-text">
              {{ Math.round(progress) }}%
            </div>
          </div>

          <!-- キャンセルボタン -->
          <button
            v-if="cancelable"
            class="loading-cancel"
            @click="handleCancel"
          >
            キャンセル
          </button>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useLoading, LoadingType } from '@/composables/useLoading'

const {
  globalLoadings,
  isGlobalLoading,
  cancelLoading,
  getLatestLoading
} = useLoading()

// 最新のグローバルローディング状態を取得
const latestLoading = computed(() => getLatestLoading(LoadingType.GLOBAL))

// 表示状態
const isVisible = computed(() => isGlobalLoading.value)

// ローディング情報
const message = computed(() => latestLoading.value?.message)
const progress = computed(() => latestLoading.value?.progress)
const cancelable = computed(() => latestLoading.value?.cancelable ?? false)

// オーバーレイのクラス
const overlayClasses = computed(() => [
  {
    'loading-overlay-blur': true,
    'loading-overlay-cancelable': cancelable.value
  }
])

// スピナーのクラス
const spinnerClasses = computed(() => [
  {
    'spinner-large': !progress.value,
    'spinner-small': !!progress.value
  }
])

// キャンセル処理
const handleCancel = () => {
  if (latestLoading.value) {
    cancelLoading(latestLoading.value.id)
  }
}
</script>

<style scoped>
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  z-index: var(--z-loading, 1080);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
}

.loading-overlay-blur {
  backdrop-filter: blur(8px);
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  max-width: 300px;
  width: 100%;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner-large {
  width: 64px;
  height: 64px;
}

.spinner-small {
  width: 48px;
  height: 48px;
}

.spinner-svg {
  width: 100%;
  height: 100%;
  animation: spin 2s linear infinite;
  color: var(--color-primary-500);
}

.spinner-circle {
  animation: dash 1.5s ease-in-out infinite;
}

.loading-message {
  text-align: center;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  line-height: var(--line-height-normal);
}

.loading-progress {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  align-items: center;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background-color: var(--color-secondary-100);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(
    90deg,
    var(--color-primary-500),
    var(--color-primary-400)
  );
  border-radius: var(--radius-full);
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: progress-shine 2s ease-in-out infinite;
}

.progress-text {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
}

.loading-cancel {
  padding: var(--space-2) var(--space-4);
  background-color: var(--color-white);
  border: 2px solid var(--color-border-default);
  border-radius: var(--radius-base);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: var(--transition-fast);
}

.loading-cancel:hover {
  background-color: var(--color-secondary-50);
  border-color: var(--color-border-hover);
}

.loading-cancel:active {
  transform: translateY(1px);
}

/* Transition animations */
.loading-overlay-enter-active {
  transition: all var(--transition-base);
}

.loading-overlay-leave-active {
  transition: all var(--transition-base);
}

.loading-overlay-enter-from {
  opacity: 0;
  backdrop-filter: blur(0px);
}

.loading-overlay-leave-to {
  opacity: 0;
  backdrop-filter: blur(0px);
}

.loading-overlay-enter-from .loading-content {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

.loading-overlay-leave-to .loading-content {
  transform: scale(0.9) translateY(20px);
  opacity: 0;
}

/* Keyframe animations */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

@keyframes progress-shine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .loading-content {
    max-width: 250px;
    gap: var(--space-3);
  }
  
  .spinner-large {
    width: 48px;
    height: 48px;
  }
  
  .spinner-small {
    width: 36px;
    height: 36px;
  }
  
  .loading-message {
    font-size: var(--font-size-sm);
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  .spinner-svg,
  .spinner-circle,
  .progress-fill::after {
    animation: none;
  }
  
  .loading-overlay-enter-active,
  .loading-overlay-leave-active {
    transition: opacity var(--transition-base);
  }
  
  .loading-overlay-enter-from .loading-content,
  .loading-overlay-leave-to .loading-content {
    transform: none;
  }
}

/* Dark mode support */
@media (prefers-color-scheme: dark) {
  .loading-overlay {
    background-color: rgba(17, 24, 39, 0.9);
  }
  
  .loading-cancel {
    background-color: var(--color-gray-800, #1f2937);
    color: var(--color-gray-100, #f3f4f6);
    border-color: var(--color-gray-600, #4b5563);
  }
  
  .loading-cancel:hover {
    background-color: var(--color-gray-700, #374151);
  }
}
</style> 