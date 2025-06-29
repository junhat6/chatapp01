<template>
  <div :class="cardClasses">
    <header v-if="$slots.header" class="card-header">
      <slot name="header" />
    </header>
    
    <div class="card-content">
      <slot />
    </div>
    
    <footer v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  padding?: 'sm' | 'md' | 'lg' | 'none'
  shadow?: 'sm' | 'md' | 'lg' | 'none'
  hover?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  padding: 'md',
  shadow: 'md',
  hover: false
})

const cardClasses = computed(() => [
  'card',
  `card-padding-${props.padding}`,
  `card-shadow-${props.shadow}`,
  {
    'card-hover': props.hover
  }
])
</script>

<style scoped>
.card {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: var(--transition-base);
}

.card-padding-none {
  padding: 0;
}

.card-padding-sm {
  padding: var(--space-4);
}

.card-padding-md {
  padding: var(--space-8);
}

.card-padding-lg {
  padding: var(--space-12);
}

.card-shadow-none {
  box-shadow: none;
  border: 1px solid var(--color-border-default);
}

.card-shadow-sm {
  box-shadow: var(--shadow-sm);
}

.card-shadow-md {
  box-shadow: var(--shadow-md);
}

.card-shadow-lg {
  box-shadow: var(--shadow-lg);
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.card-header {
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-border-muted);
  margin-bottom: var(--space-4);
}

.card-footer {
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border-muted);
  margin-top: var(--space-4);
}

/* パディングがnoneの場合のヘッダー・フッター調整 */
.card-padding-none .card-header,
.card-padding-none .card-footer {
  padding-left: var(--space-4);
  padding-right: var(--space-4);
}

.card-padding-none .card-content {
  padding: 0 var(--space-4);
}
</style> 