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
  background: white;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.2s;
}

.card-padding-none {
  padding: 0;
}

.card-padding-sm {
  padding: 1rem;
}

.card-padding-md {
  padding: 2rem;
}

.card-padding-lg {
  padding: 3rem;
}

.card-shadow-none {
  box-shadow: none;
  border: 1px solid #e0e0e0;
}

.card-shadow-sm {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-shadow-md {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.card-shadow-lg {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.card-hover:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding-bottom: 1rem;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 1rem;
}

.card-footer {
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
  margin-top: 1rem;
}

/* パディングがnoneの場合のヘッダー・フッター調整 */
.card-padding-none .card-header,
.card-padding-none .card-footer {
  padding-left: 1rem;
  padding-right: 1rem;
}

.card-padding-none .card-content {
  padding: 0 1rem;
}
</style> 