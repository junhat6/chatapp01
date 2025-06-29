<template>
  <div class="form-group">
    <label class="form-label">好きなアトラクション</label>
    <div class="attraction-input-section">
      <BaseSelect
        v-model="selectedAttraction"
        placeholder="アトラクションを選択してください"
        :options="availableOptions"
        :disabled="disabled"
        @update:model-value="(value) => addAttractionFromSelect(String(value))"
      />
      
      <div v-if="modelValue.length > 0" class="tags">
        <span v-for="attraction in modelValue" :key="attraction" class="tag attraction-tag">
          🎢 {{ attraction }}
          <button
            type="button"
            @click="removeAttraction(attraction)"
            class="tag-remove"
            :disabled="disabled"
          >
            ×
          </button>
        </span>
      </div>
      
      <div v-else class="no-selections">
        まだアトラクションが選択されていません
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { BaseSelect } from '@/components/base'
import { profileApi } from '@/services/profileApi'

interface Props {
  modelValue: string[]
  disabled?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const selectedAttraction = ref('')
const availableAttractions = ref<string[]>([])

// 利用可能なオプション（既に選択されているものは除外）
const availableOptions = computed(() => {
  return availableAttractions.value
    .filter(attraction => !props.modelValue.includes(attraction))
    .map(attraction => ({
      label: attraction,
      value: attraction
    }))
})

// セレクトボックスからアトラクション追加
const addAttractionFromSelect = (attraction: string) => {
  if (attraction && !props.modelValue.includes(attraction)) {
    const updated = [...props.modelValue, attraction]
    emit('update:modelValue', updated)
    selectedAttraction.value = ''
  }
}

// アトラクション削除
const removeAttraction = (attraction: string) => {
  const updated = props.modelValue.filter(item => item !== attraction)
  emit('update:modelValue', updated)
}

// アトラクション一覧を取得
const fetchAvailableAttractions = async () => {
  try {
    const response = await profileApi.getAvailableAttractions()
    if (response.data.success) {
      availableAttractions.value = response.data.data || []
    }
  } catch (error) {
    console.error('アトラクション一覧の取得に失敗しました:', error)
    // フォールバック用のデフォルトアトラクション
    availableAttractions.value = [
      'ハリー・ポッター・アンド・ザ・フォービドゥン・ジャーニー',
      'ザ・フライング・ダイナソー',
      'ハリウッド・ドリーム・ザ・ライド',
      'スペース・ファンタジー・ザ・ライド',
      'ジュラシック・パーク・ザ・ライド',
      'アメージング・アドベンチャー・オブ・スパイダーマン・ザ・ライド 4K3D',
      'バックドラフト',
      'ターミネーター 2:3-D',
      'シュレック 4-D アドベンチャー',
      'ミニオン・ハチャメチャ・ライド',
      'ヨッシーアドベンチャー',
      'マリオカート〜クッパの挑戦状〜'
    ]
  }
}

onMounted(() => {
  fetchAvailableAttractions()
})
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

.attraction-input-section {
  space-y: 1rem;
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

.attraction-tag {
  background-color: #e3f2fd;
  color: #1976d2;
  border: 1px solid #bbdefb;
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