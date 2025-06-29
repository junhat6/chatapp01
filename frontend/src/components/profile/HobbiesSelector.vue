<template>
  <div class="form-group">
    <label class="form-label">趣味</label>
    <div class="hobby-input-section">
      <div class="input-with-button">
        <BaseInput
          v-model="newHobby"
          placeholder="趣味を入力"
          :disabled="disabled"
          @keyup.enter="addHobby"
        />
        <BaseButton
          type="button"
          variant="secondary"
          size="sm"
          :disabled="disabled || !newHobby.trim()"
          @click="addHobby"
        >
          追加
        </BaseButton>
      </div>
      
      <div v-if="modelValue.length > 0" class="tags">
        <span v-for="hobby in modelValue" :key="hobby" class="tag hobby-tag">
          ⭐ {{ hobby }}
          <button
            type="button"
            @click="removeHobby(hobby)"
            class="tag-remove"
            :disabled="disabled"
          >
            ×
          </button>
        </span>
      </div>
      
      <div v-else class="no-selections">
        まだ趣味が登録されていません
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { BaseInput, BaseButton } from '@/components/base'

interface Props {
  modelValue: string[]
  disabled?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const newHobby = ref('')

// 趣味追加
const addHobby = () => {
  const hobby = newHobby.value.trim()
  if (hobby && !props.modelValue.includes(hobby)) {
    const updated = [...props.modelValue, hobby]
    emit('update:modelValue', updated)
    newHobby.value = ''
  }
}

// 趣味削除
const removeHobby = (hobby: string) => {
  const updated = props.modelValue.filter(item => item !== hobby)
  emit('update:modelValue', updated)
}
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.hobby-input-section {
  space-y: 1rem;
}

.input-with-button {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}

.input-with-button :deep(.form-group) {
  margin-bottom: 0;
  flex: 1;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 1rem;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.hobby-tag {
  background-color: #f3e5f5;
  color: #7b1fa2;
  border: 1px solid #ce93d8;
}

.tag-remove {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  padding: 0;
  margin-left: 0.25rem;
  font-size: 1rem;
  line-height: 1;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.tag-remove:hover:not(:disabled) {
  opacity: 1;
}

.tag-remove:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.no-selections {
  color: #999;
  font-style: italic;
  padding: 1rem;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-top: 1rem;
}
</style> 