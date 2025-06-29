<template>
  <form @submit.prevent="handleSubmit" class="profile-form">
    <BaseInput
      v-model="formData.displayName"
      label="表示名"
      type="text"
      placeholder="表示名を入力"
      required
      :disabled="loading"
    />

    <div class="form-group">
      <label for="bio" class="form-label">自己紹介</label>
      <textarea
        id="bio"
        v-model="formData.bio"
        class="form-textarea"
        placeholder="自己紹介を入力してください"
        rows="3"
        :disabled="loading"
      ></textarea>
    </div>
    
    <div class="form-row">
      <BaseInput
        v-model="formData.age"
        label="年齢"
        type="number"
        placeholder="年齢を入力"
        :min="13"
        :max="120"
        :disabled="loading"
      />

      <BaseSelect
        v-model="formData.gender"
        label="性別"
        placeholder="選択してください"
        :options="genderOptions"
        :disabled="loading"
      />
    </div>

    <div class="form-row">
      <div class="form-group">
        <label class="form-label checkbox-label">
          <input
            v-model="formData.hasUsjAnnualPass"
            type="checkbox"
            :disabled="loading"
          />
          USJ年間パスを持っています
        </label>
      </div>

      <BaseSelect
        v-model="formData.locationPrefecture"
        label="都道府県"
        placeholder="選択してください"
        :options="prefectures"
        :disabled="loading"
      />
    </div>

    <AttractionsSelector
      v-model="formData.favoriteAttractions"
      :disabled="loading"
    />

    <HobbiesSelector
      v-model="formData.hobbies"
      :disabled="loading"
    />

    <div class="form-actions">
      <BaseButton
        type="submit"
        variant="primary"
        block
        :loading="loading"
        :disabled="loading"
      >
        {{ profile ? '更新' : '作成' }}
      </BaseButton>
      <BaseButton
        v-if="isEditing"
        type="button"
        variant="secondary"
        block
        :disabled="loading"
        @click="$emit('cancel')"
      >
        キャンセル
      </BaseButton>
    </div>
  </form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { BaseInput, BaseSelect, BaseButton } from '@/components/base'
import AttractionsSelector from './AttractionsSelector.vue'
import HobbiesSelector from './HobbiesSelector.vue'
import type { UserProfile, CreateUserProfileRequest } from '@/types'

interface Props {
  profile?: UserProfile | null
  loading: boolean
  isEditing: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  submit: [data: CreateUserProfileRequest]
  cancel: []
}>()

// フォームデータ
const formData = ref<CreateUserProfileRequest>({
  displayName: '',
  profileImage: '',
  bio: '',
  age: undefined,
  gender: '',
  hasUsjAnnualPass: false,
  favoriteAttractions: [],
  locationPrefecture: '',
  hobbies: []
})

// オプション配列
const genderOptions = [
  '男性',
  '女性', 
  'その他',
  '回答しない'
]

const prefectures = [
  '北海道', '青森県', '岩手県', '宮城県', '秋田県', '山形県', '福島県',
  '茨城県', '栃木県', '群馬県', '埼玉県', '千葉県', '東京都', '神奈川県',
  '新潟県', '富山県', '石川県', '福井県', '山梨県', '長野県', '岐阜県',
  '静岡県', '愛知県', '三重県', '滋賀県', '京都府', '大阪府', '兵庫県',
  '奈良県', '和歌山県', '鳥取県', '島根県', '岡山県', '広島県', '山口県',
  '徳島県', '香川県', '愛媛県', '高知県', '福岡県', '佐賀県', '長崎県',
  '熊本県', '大分県', '宮崎県', '鹿児島県', '沖縄県'
]

// プロフィールデータでフォームを初期化
watch(() => props.profile, (newProfile) => {
  if (newProfile && props.isEditing) {
    formData.value = {
      displayName: newProfile.displayName,
      profileImage: newProfile.profileImage || '',
      bio: newProfile.bio || '',
      age: newProfile.age,
      gender: newProfile.gender || '',
      hasUsjAnnualPass: newProfile.hasUsjAnnualPass,
      favoriteAttractions: [...newProfile.favoriteAttractions],
      locationPrefecture: newProfile.locationPrefecture || '',
      hobbies: [...newProfile.hobbies]
    }
  }
}, { immediate: true })

// フォーム送信
const handleSubmit = () => {
  emit('submit', formData.value)
}
</script>

<style scoped>
.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
}

.form-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-textarea:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  margin: 0;
}

.form-actions {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style> 